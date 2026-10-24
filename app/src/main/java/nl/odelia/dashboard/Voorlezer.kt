package nl.odelia.dashboard

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** Leest tekst hardop voor met de Nederlandse stem van de telefoon. */
class Voorlezer(context: Context) : TextToSpeech.OnInitListener {
    private val stem = TextToSpeech(context.applicationContext, this)
    private var klaar = false

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        val taal = stem.setLanguage(Locale.forLanguageTag("nl-NL"))
        klaar = taal != TextToSpeech.LANG_MISSING_DATA && taal != TextToSpeech.LANG_NOT_SUPPORTED
    }

    /** Geeft `false` als er (nog) geen Nederlandse stem klaarstaat. */
    fun spreek(tekst: String): Boolean {
        if (!klaar) return false
        stem.speak(tekst, TextToSpeech.QUEUE_FLUSH, null, "voorlezen")
        return true
    }

    fun stop() {
        stem.shutdown()
    }
}
