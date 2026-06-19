package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.HistoryDao
import com.example.data.model.HistoryEntity
import com.example.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TutorRepository(private val historyDao: HistoryDao) {

    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()
    val bookmarkedHistory: Flow<List<HistoryEntity>> = historyDao.getBookmarkedHistory()

    suspend fun insertHistory(item: HistoryEntity): Long {
        return historyDao.insertHistory(item)
    }

    suspend fun updateBookmark(id: Int, isBookmarked: Boolean) {
        historyDao.updateBookmarkStatus(id, isBookmarked)
    }

    suspend fun deleteHistory(id: Int) {
        historyDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        historyDao.clearAllHistory()
    }

    suspend fun fetchExplanationFromAI(englishTerm: String, englishDefinition: String): TutorResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("API Key is missing or not configured. Please enter your GEMINI_API_KEY securely in the AI Studio Secrets panel.")
        }

        // Construct the prompt
        val prompt = """
            Technical term: $englishTerm
            Technical definition or description: $englishDefinition
        """.trimIndent()

        val systemPrompt = """
            You are "കൂട്ടുകാരൻ AI" (Koottukaaran AI), a warm, encouraging, and friendly local Malayalam tutor for 10th-grade Malayalam-medium school students in Kerala.
            Your goal is to explain complex scientific, mathematical, or technical concepts or definitions in a highly simplified, engaging, and clear manner written strictly in native Malayalam script (മലയാളം).

            Rules:
            1. NEVER use literal, robotic dictionary translations. Translate the inner meaning, keeping Malayalam sentence structures extremely simple and clear.
            2. You MUST invent a creative, fun, local Kerala-based analogy to explain the concept. Use elements familiar to school kids in Kerala, such as KSRTC buses, climbing coconut trees, chakka (jackfruits), local sevens football matches, Thrissur Pooram, school kalolsavam, unniyappam, payasam, tea stall (ചായക്കട) chats, or boat races (വള്ളംകളി).
            3. Strictly use Malayalam script (മലയാളം). Do NOT type Romanized English words like "manasilaayo" or "padikku".
            4. Address the student with warmth. Use encouraging Kerala-style words of endearment, such as "മക്കളുടെ" (children), "പ്രിയ കൂട്ടുകാരൻ" (dear friend), or "മക്കളേ" (children). Be extremely supportive and motivating.
            5. You must output your complete response in structured JSON format matching the schema properties:
                {
                  "englishTerm": "The English term requested",
                  "definition": "A brief simplified definition in English",
                  "explanation": "A very simple, clear explanation in Malayalam script",
                  "analogyTitle": "A catchy Malayalam title for the Kerala analogy used",
                  "analogyDetails": "The fun Kerala analogy explained in detail in Malayalam script",
                  "encouragement": "A warm, encouraging Malayalam tutor sign-off for the child"
                }

            Do not append any markdown formatting wrappers such as ```json or ``` except the raw JSON content itself. If the definition is invalid or not technical, gently explain that in a friendly, encouraging Malayalam manner within the fields.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.7f
            )
        )

        val response = RetrofitClient.geminiService.generateContent(apiKey, request)
        val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw IllegalStateException("API returned an empty response. Let's try again in a bit!")

        // Parse JSON using Moshi
        val cleanedJson = cleanJsonResponse(responseText)
        val adapter = RetrofitClient.moshiParser.adapter(TutorResponse::class.java)
        
        adapter.fromJson(cleanedJson) ?: throw IllegalStateException("Failed to understand the explanation response form.")
    }

    /**
     * Cleans up markdown JSON wrappers from the response text if Gemini returned them
     */
    private fun cleanJsonResponse(rawText: String): String {
        var text = rawText.trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json")
        } else if (text.startsWith("```")) {
            text = text.removePrefix("```")
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```")
        }
        return text.trim()
    }
}
