package nl.odelia.dashboard.widget

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import nl.odelia.dashboard.online.Vertaler

/**
 * Onzichtbaar schermpje achter de widget-knop "Vertalen":
 * 1. de eerste keer: toestemming "over andere apps" vragen (één keer);
 * 2. het spraak-venster van Google openen;
 * 3. de tekst naar [VertaalBalk] geven en meteen weer dicht. Het dashboard zelf opent niet.
 */
class SpreekVertaal : ComponentActivity() {
    private val spreek = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { uitslag ->
        val tekst = uitslag.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.trim()
        if (uitslag.resultCode == RESULT_OK && !tekst.isNullOrEmpty()) VertaalBalk.vertaal(this, tekst)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) return // scherm gedraaid: het spraak-venster loopt al

        val opslag = getSharedPreferences("vertaal_balk", MODE_PRIVATE)
        if (!VertaalBalk.mag(this) && !opslag.getBoolean("toestemming_gevraagd", false)) {
            opslag.edit().putBoolean("toestemming_gevraagd", true).apply()
            Toast.makeText(this, "Zet \"Bash-bord\" aan bij \"Over andere apps\". Tik daarna weer op Vertalen.", Toast.LENGTH_LONG).show()
            runCatching {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            }
            finish()
            return
        }
        if (!Vertaler.ingesteld) {
            VertaalBalk.toon(this, "🔧 Vertalen via Claude is nog niet ingesteld (backend ontbreekt).")
            finish()
            return
        }
        val vraag = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "nl-NL")
            .putExtra(RecognizerIntent.EXTRA_PROMPT, "Zeg wat je wilt vertalen")
        try {
            spreek.launch(vraag)
        } catch (_: ActivityNotFoundException) {
            VertaalBalk.toon(this, "🎤 Inspreken werkt niet op dit toestel.")
            finish()
        }
    }
}
