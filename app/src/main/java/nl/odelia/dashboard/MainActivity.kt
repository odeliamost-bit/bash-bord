package nl.odelia.dashboard

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDateTime
import kotlinx.coroutines.delay
import nl.odelia.dashboard.meldingen.Herinneringen
import nl.odelia.dashboard.meldingen.Meldingen
import nl.odelia.dashboard.onthoud.Bron
import nl.odelia.dashboard.onthoud.Concept
import nl.odelia.dashboard.onthoud.OnthoudStaat
import nl.odelia.dashboard.ui.DashboardApp
import nl.odelia.dashboard.ui.Verzoek
import nl.odelia.dashboard.ui.theme.DashboardThema
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.uren.MailActies

class MainActivity : ComponentActivity() {
    private lateinit var voorlezer: Voorlezer
    private val model: DashboardModel by viewModels()

    /** Iets van buiten de app (gedeelde tekst, widget, melding) dat meteen een scherm moet openen. */
    private var verzoek by mutableStateOf<Verzoek?>(null)

    /** Wat er moet gebeuren met de tekst die net is ingesproken. */
    private var naInspreken: ((String) -> Unit)? = null
    private val spraak = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { uitslag ->
        val tekst = uitslag.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (uitslag.resultCode == RESULT_OK && !tekst.isNullOrBlank()) naInspreken?.invoke(tekst)
        naInspreken = null
    }
    private val meldingenToestemming = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Achtergrond loopt door achter de statusbalk; donkere icoontjes op de lichte kleur.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        voorlezer = Voorlezer(this)
        Meldingen.maakKanalen(this)
        Herinneringen.plan(this)
        vraagMeldingenToestemming()
        if (savedInstanceState == null) verzoek = verzoekUit(intent)
        val onthoudActies = model.onthoudActies(::spreekIn)
        val mailActies = MailActies(openMail = ::openMail, deel = ::deel, zetAdres = model::zetKimAdres)
        setContent {
            DashboardThema {
                val gegevens by model.klokGegevens.collectAsStateWithLifecycle()
                val onthoud by model.onthoudGegevens.collectAsStateWithLifecycle()
                val weekdoel by model.weekdoel.collectAsStateWithLifecycle()
                val kimAdres by model.kimAdres.collectAsStateWithLifecycle()
                val nu by klokTik()
                val klok = gegevens?.staat(nu, weekdoel)
                val o = onthoud
                if (klok == null || o == null) {
                    // Database laadt nog (duurt heel kort).
                    Box(Modifier.fillMaxSize().background(Kleur.Achtergrond))
                } else {
                    DashboardApp(
                        klok = klok,
                        klokActies = model.klokActies,
                        onthoud = OnthoudStaat(o.first, o.second, nu.toLocalDate()),
                        onthoudActies = onthoudActies,
                        mailActies = mailActies,
                        kimAdres = kimAdres,
                        onVoorlezen = voorlezer::spreek,
                        verzoek = verzoek,
                        onVerzoekGezien = { verzoek = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        verzoekUit(intent)?.let { verzoek = it }
    }

    /** Gedeelde tekst uit een andere app, de knoppen van de Onthoud-widget, of de urenmail-melding. */
    private fun verzoekUit(intent: Intent?): Verzoek? = when (intent?.action) {
        Intent.ACTION_SEND -> {
            val tekst = listOfNotNull(intent.getStringExtra(Intent.EXTRA_SUBJECT), intent.getStringExtra(Intent.EXTRA_TEXT))
                .filter { it.isNotBlank() }.distinct().joinToString("\n")
            if (tekst.isBlank()) null else Verzoek.Onthoud(Concept(tekst = tekst.trim(), bron = Bron.Gedeeld))
        }
        ACTIE_ONTHOUD -> Verzoek.Onthoud(Concept())
        ACTIE_INSPREKEN -> Verzoek.Onthoud(Concept(inspreken = true))
        ACTIE_URENMAIL -> Verzoek.Urenmail
        else -> null
    }

    /**
     * Gmail openen met aan, onderwerp en tekst al ingevuld (een concept).
     * VERSTUURT NIETS: Odelia drukt zelf op verzenden. Geen Gmail? Dan een andere mail-app.
     */
    private fun openMail(aan: String, onderwerp: String, tekst: String) {
        val adres = Uri.encode(aan.trim(), "@.+-_")
        val mail = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$adres?subject=${Uri.encode(onderwerp)}&body=${Uri.encode(tekst)}"))
            .putExtra(Intent.EXTRA_SUBJECT, onderwerp)
            .putExtra(Intent.EXTRA_TEXT, tekst)
        if (aan.isNotBlank()) mail.putExtra(Intent.EXTRA_EMAIL, arrayOf(aan.trim()))
        try {
            startActivity(Intent(mail).setPackage("com.google.android.gm"))
        } catch (_: ActivityNotFoundException) {
            try {
                startActivity(Intent.createChooser(mail, "Mail klaarzetten met…"))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(this, "Er is geen mail-app gevonden. Gebruik Kopiëren.", Toast.LENGTH_LONG).show()
            }
        }
    }

    /** Het "Delen"-menu van Android (zonder deze app zelf erin). */
    private fun deel(onderwerp: String, tekst: String) {
        val delen = Intent(Intent.ACTION_SEND).setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT, onderwerp)
            .putExtra(Intent.EXTRA_TEXT, tekst)
        startActivity(
            Intent.createChooser(delen, "Urenmail delen")
                .putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, arrayOf(ComponentName(this, MainActivity::class.java))),
        )
    }

    /** Opent de spraakherkenning van de telefoon (Nederlands). */
    private fun spreekIn(klaar: (String) -> Unit) {
        naInspreken = klaar
        val vraag = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "nl-NL")
            .putExtra(RecognizerIntent.EXTRA_PROMPT, "Zeg wat je wilt onthouden")
        try {
            spraak.launch(vraag)
        } catch (_: ActivityNotFoundException) {
            naInspreken = null
            Toast.makeText(this, "Inspreken lukt niet: er is geen spraakherkenning op deze telefoon.", Toast.LENGTH_LONG).show()
        }
    }

    /** Android 13+: één keer vragen of de app meldingen mag tonen (voor de herinneringen). */
    private fun vraagMeldingenToestemming() {
        if (Build.VERSION.SDK_INT >= 33 && !Meldingen.magTonen(this)) {
            meldingenToestemming.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onDestroy() {
        voorlezer.stop()
        super.onDestroy()
    }

    companion object {
        /** Opent "Onthoud dit" (van de widget). */
        const val ACTIE_ONTHOUD = "nl.odelia.dashboard.ONTHOUD"

        /** Opent "Onthoud dit" en begint meteen met inspreken (🎤 van de widget). */
        const val ACTIE_INSPREKEN = "nl.odelia.dashboard.INSPREKEN"

        /** Opent de urenmail (van de melding op vrijdag). */
        const val ACTIE_URENMAIL = "nl.odelia.dashboard.OPEN_URENMAIL"
    }
}

/** De tijd van nu, elke 30 seconden bijgewerkt (voor "al 5½ uur gewerkt"). */
@Composable
private fun klokTik() = produceState(LocalDateTime.now()) {
    while (true) {
        delay(30_000)
        value = LocalDateTime.now()
    }
}
