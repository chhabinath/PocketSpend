package com.chhabinath.pocketspend.ai

object LocalLLM {

    init {
        System.loadLibrary("pocketllm")
    }

    external fun generate(
        modelPath: String,
        prompt: String
    ): String
}