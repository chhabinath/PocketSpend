package com.chhabinath.pocketspend.ai

import org.json.JSONObject

data class ParsedExpense(
    val merchant: String,
    val amount: Double,
    val category: String,
    val date: String
)

object ExpenseParser {

    fun parse(
        modelPath: String,
        ocrText: String
    ): ParsedExpense? {

        val prompt = """
            You are an expense extraction system.

            Extract expense information from the receipt OCR text below.

            Return ONLY valid JSON.
            Do not write explanations.
            Do not use markdown.
            Do not use code fences.

            Required JSON format:

            {
              "merchant": "string",
              "amount": 0.0,
              "category": "string",
              "date": "YYYY-MM-DD"
            }

            Rules:

            - merchant = store or business name
            - amount = final amount paid
            - category should be one of:
              Groceries
              Food
              Shopping
              Transport
              Bills
              Healthcare
              Entertainment
              Other

            - date must use YYYY-MM-DD
            - If a field cannot be determined, use an empty string.
            - amount must be a number.

            OCR TEXT:

            $ocrText
        """.trimIndent()

        val response = LocalLLM.generate(
            modelPath,
            prompt
        )

        return parseJson(response)
    }

    private fun parseJson(
        response: String
    ): ParsedExpense? {

        return try {

            val cleaned = response
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val json = JSONObject(cleaned)

            ParsedExpense(
                merchant = json.optString("merchant"),
                amount = json.optDouble("amount", 0.0),
                category = json.optString("category"),
                date = json.optString("date")
            )

        } catch (e: Exception) {

            null
        }
    }
}