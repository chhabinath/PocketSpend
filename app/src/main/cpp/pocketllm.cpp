#include <jni.h>
#include <string>
#include <vector>
#include <cstring>

#include "llama.h"

extern "C"
JNIEXPORT jstring JNICALL
Java_com_chhabinath_pocketspend_ai_LocalLLM_generate(
        JNIEnv* env,
        jobject /* thiz */,
        jstring jModelPath,
        jstring jPrompt) {

    // ============================================================
    // Validate Java strings
    // ============================================================

    if (jModelPath == nullptr || jPrompt == nullptr) {
        return env->NewStringUTF(
                "ERROR: Model path or prompt is null"
        );
    }

    const char* modelPath =
            env->GetStringUTFChars(jModelPath, nullptr);

    const char* prompt =
            env->GetStringUTFChars(jPrompt, nullptr);

    if (modelPath == nullptr || prompt == nullptr) {

        if (modelPath != nullptr) {
            env->ReleaseStringUTFChars(
                    jModelPath,
                    modelPath
            );
        }

        if (prompt != nullptr) {
            env->ReleaseStringUTFChars(
                    jPrompt,
                    prompt
            );
        }

        return env->NewStringUTF(
                "ERROR: Failed to read Java strings"
        );
    }

    std::string result;

    // ============================================================
    // Initialize llama backend
    // ============================================================

    llama_backend_init();

    // ============================================================
    // Load model
    // ============================================================

    llama_model_params model_params =
            llama_model_default_params();

    llama_model* model =
            llama_model_load_from_file(
                    modelPath,
                    model_params
            );

    if (model == nullptr) {

        result = "ERROR: Failed to load model";

        env->ReleaseStringUTFChars(
                jModelPath,
                modelPath
        );

        env->ReleaseStringUTFChars(
                jPrompt,
                prompt
        );

        return env->NewStringUTF(
                result.c_str()
        );
    }

    // ============================================================
    // Create context
    // ============================================================

    llama_context_params ctx_params =
            llama_context_default_params();

    // Poco F1 friendly
    ctx_params.n_ctx = 2048;
    ctx_params.n_batch = 512;

    llama_context* ctx =
            llama_init_from_model(
                    model,
                    ctx_params
            );

    if (ctx == nullptr) {

        result = "ERROR: Failed to create llama context";

        llama_model_free(model);

        env->ReleaseStringUTFChars(
                jModelPath,
                modelPath
        );

        env->ReleaseStringUTFChars(
                jPrompt,
                prompt
        );

        return env->NewStringUTF(
                result.c_str()
        );
    }

    // ============================================================
    // Get vocabulary
    // ============================================================

    const llama_vocab* vocab =
            llama_model_get_vocab(model);

    if (vocab == nullptr) {

        result = "ERROR: Failed to get vocabulary";

        llama_free(ctx);
        llama_model_free(model);

        env->ReleaseStringUTFChars(
                jModelPath,
                modelPath
        );

        env->ReleaseStringUTFChars(
                jPrompt,
                prompt
        );

        return env->NewStringUTF(
                result.c_str()
        );
    }

    // ============================================================
    // Get model chat template
    // ============================================================

    const char* chatTemplate =
            llama_model_chat_template(
                    model,
                    nullptr
            );

    // ============================================================
    // Create chat messages
    // ============================================================

    llama_chat_message messages[2];

    messages[0].role = "system";
    messages[0].content =
            "You are PocketSpend AI, a private offline "
            "personal finance assistant. "
            "Give short, useful and clear answers.";

    messages[1].role = "user";
    messages[1].content = prompt;

    // ============================================================
    // Build formatted prompt
    // ============================================================

    std::string formattedPrompt;

    if (chatTemplate != nullptr) {

        // First ask llama.cpp how much memory is required.
        int32_t requiredSize =
                llama_chat_apply_template(
                        chatTemplate,
                        messages,
                        2,
                        true,
                        nullptr,
                        0
                );

        if (requiredSize <= 0) {

            result =
                    "ERROR: Failed to create chat template";

            llama_free(ctx);
            llama_model_free(model);

            env->ReleaseStringUTFChars(
                    jModelPath,
                    modelPath
            );

            env->ReleaseStringUTFChars(
                    jPrompt,
                    prompt
            );

            return env->NewStringUTF(
                    result.c_str()
            );
        }

        // Allocate enough space.
        std::vector<char> buffer(
                static_cast<size_t>(requiredSize) + 1
        );

        int32_t actualSize =
                llama_chat_apply_template(
                        chatTemplate,
                        messages,
                        2,
                        true,
                        buffer.data(),
                        static_cast<int32_t>(buffer.size())
                );

        if (actualSize <= 0) {

            result =
                    "ERROR: Failed to apply chat template";

            llama_free(ctx);
            llama_model_free(model);

            env->ReleaseStringUTFChars(
                    jModelPath,
                    modelPath
            );

            env->ReleaseStringUTFChars(
                    jPrompt,
                    prompt
            );

            return env->NewStringUTF(
                    result.c_str()
            );
        }

        formattedPrompt.assign(
                buffer.data(),
                static_cast<size_t>(actualSize)
        );

    } else {

        // --------------------------------------------------------
        // Fallback if model doesn't contain a chat template
        // --------------------------------------------------------

        formattedPrompt =
                "User: " +
                std::string(prompt) +
                "\nAssistant:";
    }

    // ============================================================
    // Tokenize formatted prompt
    // ============================================================

    const int promptLength =
            static_cast<int>(
                    formattedPrompt.size()
            );

    if (promptLength <= 0) {

        result =
                "ERROR: Formatted prompt is empty";

        llama_free(ctx);
        llama_model_free(model);

        env->ReleaseStringUTFChars(
                jModelPath,
                modelPath
        );

        env->ReleaseStringUTFChars(
                jPrompt,
                prompt
        );

        return env->NewStringUTF(
                result.c_str()
        );
    }

    // ============================================================
    // First tokenization call
    // ============================================================

    int nTokens =
            llama_tokenize(
                    vocab,
                    formattedPrompt.c_str(),
                    promptLength,
                    nullptr,
                    0,
                    true,
                    true
            );

    // llama.cpp can return negative required size.
    if (nTokens < 0) {
        nTokens = -nTokens;
    }

    if (nTokens <= 0) {

        result =
                "ERROR: Failed to determine token count";

        llama_free(ctx);
        llama_model_free(model);

        env->ReleaseStringUTFChars(
                jModelPath,
                modelPath
        );

        env->ReleaseStringUTFChars(
                jPrompt,
                prompt
        );

        return env->NewStringUTF(
                result.c_str()
        );
    }

    // ============================================================
    // Check context
    // ============================================================

    if (nTokens >= 2048) {

        result =
                "ERROR: Prompt too long: " +
                std::to_string(nTokens) +
                " tokens";

        llama_free(ctx);
        llama_model_free(model);

        env->ReleaseStringUTFChars(
                jModelPath,
                modelPath
        );

        env->ReleaseStringUTFChars(
                jPrompt,
                prompt
        );

        return env->NewStringUTF(
                result.c_str()
        );
    }

    // ============================================================
    // Allocate token buffer
    // ============================================================

    std::vector<llama_token> tokens(
            static_cast<size_t>(nTokens)
    );

    // ============================================================
    // Tokenize into actual buffer
    // ============================================================

    int actualTokens =
            llama_tokenize(
                    vocab,
                    formattedPrompt.c_str(),
                    promptLength,
                    tokens.data(),
                    nTokens,
                    true,
                    true
            );

    if (actualTokens < 0) {

        // Buffer wasn't large enough.
        int requiredTokens = -actualTokens;

        tokens.resize(
                static_cast<size_t>(requiredTokens)
        );

        actualTokens =
                llama_tokenize(
                        vocab,
                        formattedPrompt.c_str(),
                        promptLength,
                        tokens.data(),
                        requiredTokens,
                        true,
                        true
                );
    }

    if (actualTokens <= 0) {

        result =
                "ERROR: Failed to tokenize prompt";

        llama_free(ctx);
        llama_model_free(model);

        env->ReleaseStringUTFChars(
                jModelPath,
                modelPath
        );

        env->ReleaseStringUTFChars(
                jPrompt,
                prompt
        );

        return env->NewStringUTF(
                result.c_str()
        );
    }

    tokens.resize(
            static_cast<size_t>(actualTokens)
    );

    // ============================================================
    // Create batch
    // ============================================================

    llama_batch batch =
            llama_batch_init(
                    static_cast<int32_t>(
                            tokens.size()
                    ),
                    0,
                    1
            );

    if (batch.token == nullptr) {

        result =
                "ERROR: Failed to allocate llama batch";

        llama_free(ctx);
        llama_model_free(model);

        env->ReleaseStringUTFChars(
                jModelPath,
                modelPath
        );

        env->ReleaseStringUTFChars(
                jPrompt,
                prompt
        );

        return env->NewStringUTF(
                result.c_str()
        );
    }

    // ============================================================
    // Put prompt tokens into batch
    // ============================================================

    for (size_t i = 0; i < tokens.size(); ++i) {

        batch.token[i] =
                tokens[i];

        batch.pos[i] =
                static_cast<llama_pos>(i);

        batch.n_seq_id[i] = 1;

        batch.seq_id[i][0] = 0;

        batch.logits[i] = false;
    }

    // Need logits from final prompt token.
    batch.logits[
            tokens.size() - 1
    ] = true;

    batch.n_tokens =
            static_cast<int32_t>(
                    tokens.size()
            );

    // ============================================================
    // Decode prompt
    // ============================================================

    int decodeResult =
            llama_decode(
                    ctx,
                    batch
            );

    if (decodeResult != 0) {

        result =
                "ERROR: llama_decode failed: " +
                std::to_string(
                        decodeResult
                );

        llama_batch_free(batch);
        llama_free(ctx);
        llama_model_free(model);

        env->ReleaseStringUTFChars(
                jModelPath,
                modelPath
        );

        env->ReleaseStringUTFChars(
                jPrompt,
                prompt
        );

        return env->NewStringUTF(
                result.c_str()
        );
    }

    // ============================================================
    // Create sampler
    // ============================================================

    llama_sampler* sampler =
            llama_sampler_init_greedy();

    if (sampler == nullptr) {

        result =
                "ERROR: Failed to create sampler";

        llama_batch_free(batch);
        llama_free(ctx);
        llama_model_free(model);

        env->ReleaseStringUTFChars(
                jModelPath,
                modelPath
        );

        env->ReleaseStringUTFChars(
                jPrompt,
                prompt
        );

        return env->NewStringUTF(
                result.c_str()
        );
    }

    // ============================================================
    // Generate response
    // ============================================================

    const int maxTokens = 128;

    for (int i = 0; i < maxTokens; ++i) {

        // --------------------------------------------------------
        // Sample
        // --------------------------------------------------------

        llama_token token =
                llama_sampler_sample(
                        sampler,
                        ctx,
                        -1
                );

        // --------------------------------------------------------
        // End of generation
        // --------------------------------------------------------

        if (llama_vocab_is_eog(
                vocab,
                token
        )) {
            break;
        }

        // --------------------------------------------------------
        // Convert token to text
        // --------------------------------------------------------

        char buffer[1024];

        int nChars =
                llama_token_to_piece(
                        vocab,
                        token,
                        buffer,
                        sizeof(buffer),
                        0,
                        true
                );

        if (nChars > 0) {

            result.append(
                    buffer,
                    static_cast<size_t>(
                            nChars
                    )
            );
        }

        // --------------------------------------------------------
        // Accept token
        // --------------------------------------------------------

        llama_sampler_accept(
                sampler,
                token
        );

        // --------------------------------------------------------
        // Prepare next batch
        // --------------------------------------------------------

        batch.n_tokens = 1;

        batch.token[0] = token;

        batch.pos[0] =
                static_cast<llama_pos>(
                        tokens.size() + i
                );

        batch.n_seq_id[0] = 1;

        batch.seq_id[0][0] = 0;

        batch.logits[0] = true;

        // --------------------------------------------------------
        // Decode generated token
        // --------------------------------------------------------

        decodeResult =
                llama_decode(
                        ctx,
                        batch
                );

        if (decodeResult != 0) {

            result +=
                    "\n[decode error: " +
                    std::to_string(
                            decodeResult
                    ) +
                    "]";

            break;
        }
    }

    // ============================================================
    // Cleanup
    // ============================================================

    llama_sampler_free(
            sampler
    );

    llama_batch_free(
            batch
    );

    llama_free(
            ctx
    );

    llama_model_free(
            model
    );

    // ============================================================
    // Release Java strings
    // ============================================================

    env->ReleaseStringUTFChars(
            jModelPath,
            modelPath
    );

    env->ReleaseStringUTFChars(
            jPrompt,
            prompt
    );

    // ============================================================
    // Return result
    // ============================================================

    if (result.empty()) {

        result =
                "ERROR: Model generated empty response";
    }

    return env->NewStringUTF(
            result.c_str()
    );
}