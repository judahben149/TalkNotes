package com.judahben149.talknotes

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class VoiceToTextParser(private val application: Application): RecognitionListener {

    private val _state = MutableStateFlow(VoiceToTextParserState())
    val state = _state.asStateFlow()

    private val recognizer = SpeechRecognizer.createSpeechRecognizer(application)

    fun startListening(languageCode: String = "en") {
        _state.update { VoiceToTextParserState() }

        if (!SpeechRecognizer.isRecognitionAvailable(application)) {
            _state.update {
                it.copy(
                    errorMessage = "Voice recognition not available"
                )
            }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                languageCode
            )
        }
        recognizer.setRecognitionListener(this)
        recognizer.startListening(intent)

        _state.update { it.copy(isSpeaking = true) }
    }

    fun stopListening() {
        _state.update { it.copy(isSpeaking = false) }

        recognizer.stopListening()
    }

    override fun onReadyForSpeech(p0: Bundle?) {
        _state.update { it.copy(errorMessage = null) }
    }

    override fun onBeginningOfSpeech() = Unit

    override fun onRmsChanged(p0: Float) = Unit

    override fun onBufferReceived(p0: ByteArray?) = Unit

    override fun onEndOfSpeech() {
        _state.update { it.copy(isSpeaking = false) }
    }

    override fun onError(error: Int) {
        if (error == SpeechRecognizer.ERROR_CLIENT) return

        _state.update { it.copy(errorMessage = "Error: $error") }
    }

    override fun onResults(results: Bundle?) {
        results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.getOrNull(0)
            ?.let {  result ->
                _state.update { it.copy(spokenText = result) }
            }
    }

    override fun onPartialResults(p0: Bundle?) = Unit

    override fun onEvent(p0: Int, p1: Bundle?) = Unit
}

data class VoiceToTextParserState(
    val spokenText: String = "",
    val isSpeaking: Boolean = false,
    val errorMessage: String? = null
)