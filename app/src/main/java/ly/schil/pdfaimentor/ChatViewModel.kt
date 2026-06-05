package ly.schil.pdfaimentor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Author { USER, MODEL, ERROR }

data class ChatMessage(
    val author: Author,
    val text: String,
)

/**
 * Holds the chat state and talks to Gemini.
 *
 * A ViewModel survives configuration changes (e.g. screen rotation) — the
 * Activity gets destroyed/recreated, the ViewModel and its state live on.
 */
class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // startChat() keeps the conversation history, so follow-up questions work.
    private val chat by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
        ).startChat()
    }

    init {
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            _messages.value = listOf(
                ChatMessage(
                    Author.ERROR,
                    "No Gemini API key configured.\n\n" +
                        "Add  geminiApiKey=YOUR_KEY  to local.properties and rebuild.",
                ),
            )
        }
    }

    fun send(text: String) {
        val prompt = text.trim()
        if (prompt.isEmpty() || _isLoading.value) return

        _messages.update { it + ChatMessage(Author.USER, prompt) }
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = chat.sendMessage(prompt)
                _messages.update {
                    it + ChatMessage(Author.MODEL, response.text ?: "(empty response)")
                }
            } catch (e: Exception) {
                _messages.update {
                    it + ChatMessage(Author.ERROR, e.message ?: "Unknown error")
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}
