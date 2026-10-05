package com.chhabinath.pocketspend.ai

import android.content.Context
import java.io.File

object ModelManager {

    private const val MODEL_NAME =
        "qwen2.5-0.5b-instruct-q4_k_m.gguf"

    fun getModelPath(context: Context): String {

        val modelFile = File(
            context.filesDir,
            MODEL_NAME
        )

        if (!modelFile.exists()) {

            context.assets.open(MODEL_NAME).use { input ->

                modelFile.outputStream().use { output ->

                    input.copyTo(
                        output,
                        bufferSize = 1024 * 1024
                    )
                }
            }
        }

        return modelFile.absolutePath
    }
}