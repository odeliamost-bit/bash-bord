package nl.odelia.dashboard

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import nl.odelia.dashboard.meldingen.Meldingen
import nl.odelia.dashboard.meldingen.Planner
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.DashboardApp
import nl.odelia.dashboard.ui.Opdracht
import nl.odelia.dashboard.ui.theme.DashboardThema

class MainActivity : ComponentActivity() {
    /** Wat een melding, widget of andere app wil openen. */
    private val opdracht = mutableStateOf<Opdracht?>(null)

    private var naInspreken: ((String) -> Unit)? = null
    private val inspreken = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultaat ->
        val tekst = resultaat.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (resultaat.resultCode == RESULT_OK && !tekst.isNullOrBlank()) naInspreken?.invoke(tekst)
        naInspreken = null
    }
    private val vraagMeldingen = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Achtergrond loopt door achter de statusbalk; donkere icoontjes op de lichte kleur.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        Meldingen.maakKanalen(this)
        vraagToestemmingVoorMeldingen()
        lifecycleScope.launch { Planner.plan(applicationContext) }
        lees(intent)

        setContent {
            val model: DashboardModel = viewModel()
            val staat by model.staat.collectAsStateWithLifecycle()
            val acties = remember(model) {
                object : AppActies by model {
                    override fun inspreken(klaar: (String) -> Unit) = spreekIn(klaar)
                    override fun openMail(onderwerp: String, tekst: String) = openMailApp(onderwerp, tekst)
                }
            }
            DashboardThema {
                DashboardApp(staat, acties, opdracht.value, onOpdrachtGedaan = { opdracht.value = null })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        lees(intent)
    }

    /** Gedeelde tekst uit een andere app, of een scherm uit een melding/widget. */
    private fun lees(intent: Intent?) {
        intent ?: return
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            // Niet te lang: gedeelde tekst komt van een andere app.
            opdracht.value = Opdracht("toevoegen", intent.getStringExtra(Intent.EXTRA_TEXT)?.take(MAX_GEDEELD))
        } else {
            intent.getStringExtra(Meldingen.EXTRA_SCHERM)?.let { opdracht.value = Opdracht(it) }
        }
    }

    private companion object {
        const val MAX_GEDEELD = 5_000
    }

    private fun vraagToestemmingVoorMeldingen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            vraagMeldingen.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun spreekIn(klaar: (String) -> Unit): Boolean {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "nl-NL")
            .putExtra(RecognizerIntent.EXTRA_PROMPT, "Zeg wat je wilt onthouden")
        return try {
            naInspreken = klaar
            inspreken.launch(intent)
            true
        } catch (_: ActivityNotFoundException) {
            naInspreken = null
            false
        }
    }

    /** Zet een nieuwe mail klaar in Gmail (of laat kiezen). Versturen doet Odelia zelf. */
    private fun openMailApp(onderwerp: String, tekst: String): Boolean {
        val mail = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT, onderwerp)
            .putExtra(Intent.EXTRA_TEXT, tekst)
        return try {
            startActivity(Intent(mail).setPackage("com.google.android.gm"))
            true
        } catch (_: ActivityNotFoundException) {
            try {
                startActivity(Intent.createChooser(mail, "Mail openen met"))
                true
            } catch (_: ActivityNotFoundException) {
                false
            }
        }
    }
}
