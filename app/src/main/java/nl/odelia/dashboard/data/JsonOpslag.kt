package nl.odelia.dashboard.data

import android.content.Context
import android.util.AtomicFile
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import nl.odelia.dashboard.logica.CyclusData
import nl.odelia.dashboard.logica.CyclusJson
import nl.odelia.dashboard.logica.WensenData
import nl.odelia.dashboard.logica.WensenJson

/**
 * Eén JSON-bestandje op de telefoon, net als [LerenOpslag].
 * Los van de database, zodat die niet hoeft te veranderen. Gaat nooit online.
 */
class JsonOpslag<T>(
    bestand: File,
    leeg: T,
    private val lees: (String) -> T,
    private val schrijf: (T) -> String,
) {
    private val bestand = AtomicFile(bestand)
    private val werk = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val slot = Mutex()
    private val _data = MutableStateFlow(leeg)
    val data: StateFlow<T> = _data.asStateFlow()

    private val geladen = werk.async {
        val tekst = try {
            String(this@JsonOpslag.bestand.readFully(), Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
        if (tekst != null) _data.value = lees(tekst)
    }

    /** De gegevens zoals ze nu zijn (wacht tot het bestand gelezen is). Voor de meldingen. */
    suspend fun nu(): T {
        geladen.await()
        return _data.value
    }

    /** Pas iets aan en bewaar het meteen. Eén wijziging tegelijk. Daarna [na] (bijv. wekkers opnieuw zetten). */
    fun wijzig(wat: (T) -> T, na: suspend () -> Unit = {}) {
        werk.launch {
            geladen.await()
            val veranderd = slot.withLock {
                val nieuw = wat(_data.value)
                if (nieuw == _data.value) return@withLock false
                _data.value = nieuw
                bewaar(nieuw)
                true
            }
            if (veranderd) na()
        }
    }

    private fun bewaar(d: T) {
        val uit = try {
            bestand.startWrite()
        } catch (_: Exception) {
            return
        }
        try {
            uit.write(schrijf(d).toByteArray(Charsets.UTF_8))
            bestand.finishWrite(uit)
        } catch (_: Exception) {
            bestand.failWrite(uit)
        }
    }
}

/** De losse bestandjes. Eén keer per app, zodat app en meldingen hetzelfde zien. */
object Bestanden {
    @Volatile private var cyclus: JsonOpslag<CyclusData>? = null
    @Volatile private var wensen: JsonOpslag<WensenData>? = null

    /**
     * Cyclus is gevoelig: in `noBackupFilesDir`, zodat Android het niet in de
     * Google-back-up (cloud) zet. Het blijft alleen op deze telefoon.
     */
    fun cyclus(context: Context): JsonOpslag<CyclusData> = cyclus ?: synchronized(this) {
        cyclus ?: JsonOpslag(File(context.applicationContext.noBackupFilesDir, "cyclus.json"), CyclusData(), CyclusJson::lees, CyclusJson::schrijf)
            .also { cyclus = it }
    }

    fun wensen(context: Context): JsonOpslag<WensenData> = wensen ?: synchronized(this) {
        wensen ?: JsonOpslag(File(context.applicationContext.filesDir, "wensen.json"), WensenData(), WensenJson::lees, WensenJson::schrijf)
            .also { wensen = it }
    }
}
