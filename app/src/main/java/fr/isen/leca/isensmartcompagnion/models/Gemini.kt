package fr.isen.leca.isensmartcompagnion.models

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.TextPart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Gemini {
    private const val API_KEY = "AIzaSyA0AacnQylwobsyVkj-UPv-fQNThz3QGbA"

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = API_KEY
    )

    suspend fun getResponse(prompt: String): String {
        val trimmedPrompt = prompt.trim() // Supprime les espaces inutiles

        if (trimmedPrompt.isEmpty()) {
            return "Veuillez poser une question valide."
        }

        val contextPrompt = """
            Tu es un assistant intelligent conçu pour guider les étudiants dans leur école d'ingénieur.
            Tu peux répondre aux questions sur l'école, les événements, l'agenda des cours et d'autres sujets académiques.
            Sois précis et utile.

            Question de l'utilisateur : $trimmedPrompt
        """.trimIndent()

        return try {
            withContext(Dispatchers.IO) {
                val response = model.generateContent(
                    Content(parts = listOf(TextPart(contextPrompt)))
                )
                response.text?.trim() ?: "Je n'ai pas de réponse pour cette question."
            }
        } catch (e: Exception) {
            Log.e("Gemini", "Erreur : ${e.message}", e)
            "Une erreur est survenue. Vérifie ta connexion internet."
        }
    }
}
