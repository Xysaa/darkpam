package com.example.nutriscan.data.repository

import com.example.nutriscan.data.remote.api.GeminiService
import com.example.nutriscan.data.remote.api.SystemPrompts
import com.example.nutriscan.domain.model.Product
import com.example.nutriscan.domain.model.UserProfile
import com.example.nutriscan.domain.repository.AIRepository
import com.example.nutriscan.domain.repository.WritingStyle

class AIRepositoryImpl(
    private val geminiService: GeminiService
) : AIRepository {

    override suspend fun summarize(text: String): Result<String> {
        val prompt = "Rangkum teks berikut:\n\n$text"
        return geminiService.generateContent(prompt, SystemPrompts.SUMMARIZER)
    }

    override suspend fun generateIdeas(topic: String): Result<List<String>> {
        val prompt = "Berikan 5 ide kreatif untuk topik: $topic"
        return geminiService.generateContent(prompt, SystemPrompts.IDEA_GENERATOR)
            .map { response ->
                response.lines()
                    .filter { it.isNotBlank() }
                    .map { it.replace(Regex("^\\d+\\.\\s*"), "").trim() }
                    .filter { it.isNotBlank() }
            }
    }

    override suspend fun improveWriting(text: String, style: WritingStyle): Result<String> {
        val styleInstruction = when (style) {
            WritingStyle.FORMAL   -> "Gunakan gaya formal dan profesional."
            WritingStyle.CASUAL   -> "Gunakan gaya santai dan friendly."
            WritingStyle.ACADEMIC -> "Gunakan gaya akademik dan ilmiah."
            WritingStyle.CREATIVE -> "Gunakan gaya kreatif dan menarik."
            WritingStyle.NEUTRAL  -> "Gunakan gaya netral."
        }
        val prompt = "$styleInstruction\n\nPerbaiki tulisan berikut:\n\n$text"
        return geminiService.generateContent(prompt, SystemPrompts.WRITING_IMPROVER)
    }

    override suspend fun translate(text: String, targetLanguage: String): Result<String> {
        val prompt = "Terjemahkan ke bahasa $targetLanguage:\n\n$text"
        return geminiService.generateContent(prompt, SystemPrompts.TRANSLATOR)
    }

    override suspend fun chat(message: String): Result<String> {
        return geminiService.generateContent(prompt = message)
    }

    override suspend fun suggestTitle(content: String): Result<String> {
        val prompt = "Berikan saran judul untuk konten berikut:\n\n$content"
        return geminiService.generateContent(prompt, SystemPrompts.TITLE_SUGGESTER)
            .map { it.trim().removeSurrounding("\"") }
    }

    override suspend fun analyzeNutrition(product: Product, user: UserProfile): Result<String> {
        val n          = product.nutrimentsPerServing
        val conditions = if (user.healthConditions.isEmpty()) "tidak ada" else
            user.healthConditions.joinToString(", ") { it.displayName }

        val prompt = """
            Produk   : ${product.displayName}${if (product.brand.isNotBlank()) " (${product.brand})" else ""}
            Per sajian ${product.servingSize.toInt()}g:
            - Kalori       : ${n.calories.toInt()} kcal
            - Lemak        : ${"%.1f".format(n.fat)} g
            - Lemak Jenuh  : ${"%.1f".format(n.saturatedFat)} g
            - Karbohidrat  : ${"%.1f".format(n.carbs)} g
            - Gula         : ${"%.1f".format(n.sugar)} g
            - Natrium      : ${n.sodium.toInt()} mg
            - Protein      : ${"%.1f".format(n.protein)} g

            Pengguna:
            - Nama          : ${user.name}
            - Usia          : ${user.age} tahun
            - BMI           : ${"%.1f".format(user.bmi)} (${user.bmiCategory})
            - Kondisi kesehatan: $conditions

            Berikan saran apakah produk ini aman dikonsumsi untuk pengguna ini.
        """.trimIndent()

        return geminiService.generateContent(prompt, SystemPrompts.NUTRITION_ADVISOR)
    }
}
