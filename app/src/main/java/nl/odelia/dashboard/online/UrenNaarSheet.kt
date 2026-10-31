package nl.odelia.dashboard.online

import android.content.Context
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.odelia.dashboard.BuildConfig
import nl.odelia.dashboard.data.Bestanden
import nl.odelia.dashboard.data.Opslag
import nl.odelia.dashboard.logica.UrenSheet
import org.json.JSONArray
import org.json.JSONObject

/**
 * Zet afgeronde werkdagen in Odelia's Google-spreadsheet (gekozen door Odelia, okt 2026).
 *
 * Gebeurt bij het openen van de app en na elke wijziging (uitklokken, dag aanpassen, widget).
 * Alleen als het is ingesteld (GitHub-geheimen SHEET_URL en SHEET_TOKEN) en aan staat in Instellingen.
 * Welke dagen al klaar zijn, staat in `noBackupFilesDir/sheet.json` (alleen op dit apparaat).
 * Lukt het niet (geen internet)? Dan de volgende keer opnieuw, tot 14 dagen terug.
 */
object UrenNaarSheet {
    private const val BESTAND = "sheet.json"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val slot = Mutex()

    val ingesteld: Boolean get() = BuildConfig.SHEET_URL.isNotBlank() && BuildConfig.SHEET_TOKEN.isNotBlank()

    fun probeer(context: Context) {
        if (!ingesteld) return
        val app = context.applicationContext
        scope.launch {
            try {
                stuur(app)
            } catch (_: Exception) {
                // Geen internet of het script antwoordt raar: de volgende keer opnieuw.
            }
        }
    }

    private suspend fun stuur(app: Context) = slot.withLock {
        if (!Bestanden.instellingen(app).nu().urenNaarSheet) return@withLock
        val nu = LocalDateTime.now()
        val bestand = File(app.noBackupFilesDir, BESTAND)
        val klaar = lees(bestand)
        val opslag = Opslag.van(app)
        val dagen = UrenSheet.teSturen(opslag.perioden().first(), opslag.dagSoorten().first(), nu, klaar)
        if (dagen.isEmpty()) return@withLock

        val antwoord = UrenSheet.leesAntwoord(
            Sheet.stuur(BuildConfig.SHEET_URL.trim(), UrenSheet.bericht(BuildConfig.SHEET_TOKEN.trim(), dagen)),
        )
        if (antwoord.ok) schrijf(bestand, UrenSheet.opruimen(klaar + antwoord.klaar, nu.toLocalDate()))
    }

    private fun lees(bestand: File): Set<LocalDate> = try {
        val a = JSONObject(bestand.readText()).getJSONArray("klaar")
        (0 until a.length()).mapNotNull { runCatching { LocalDate.parse(a.getString(it)) }.getOrNull() }.toSet()
    } catch (_: Exception) {
        emptySet()
    }

    private fun schrijf(bestand: File, klaar: Set<LocalDate>) {
        val tijdelijk = File(bestand.parentFile, "$BESTAND.nieuw")
        tijdelijk.writeText(JSONObject().put("klaar", JSONArray(klaar.sorted().map { it.toString() })).toString())
        tijdelijk.renameTo(bestand)
    }
}
