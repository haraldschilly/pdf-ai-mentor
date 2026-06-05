package ly.schil.pdfaimentor

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Author { USER, MODEL, ERROR }

data class ChatMessage(
    val author: Author,
    val text: String,
    /** Cropped PDF region attached to this message (the core idea). */
    val image: Bitmap? = null,
)

/** Used when the user sends a selection without typing a question. */
private const val DEFAULT_SELECTION_PROMPT =
    "Explain the selected part of this document in simple terms. " +
        "If it contains formulas, walk through them step by step."

private const val SELECTION_CONTEXT =
    "The attached image is a region of an academic paper / document that " +
        "the reader selected because it is unclear to them.\n\n"

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

    fun send(text: String, image: Bitmap? = null) {
        val typed = text.trim()
        if ((typed.isEmpty() && image == null) || _isLoading.value) return

        val prompt = typed.ifEmpty { DEFAULT_SELECTION_PROMPT }
        _messages.update { it + ChatMessage(Author.USER, prompt, image) }
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = chat.sendMessage(
                    content {
                        if (image != null) {
                            image(image)
                            text(SELECTION_CONTEXT + prompt)
                        } else {
                            text(prompt)
                        }
                    },
                )
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
