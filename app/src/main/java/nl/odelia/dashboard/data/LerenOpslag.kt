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
import nl.odelia.dashboard.logica.LerenData
import nl.odelia.dashboard.logica.LerenJson

/**
 * Bewaart Leren (bakjes, quizscores, eigen kaarten) in één bestandje op de telefoon.
 * Een los bestand, zodat de database van uren en afspraken niet hoeft te veranderen.
 * Gaat mee met de sync tussen apparaten (zie online/Sync.kt), alleen als Odelia is ingelogd.
 */
class LerenOpslag private constructor(context: Context) {
    private val bestand = AtomicFile(File(context.filesDir, "leren.json"))
    private val werk = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val slot = Mutex()
    private val _data = MutableStateFlow(LerenData())
    val data: StateFlow<LerenData> = _data.asStateFlow()

    private val geladen = werk.async {
        val tekst = try {
            String(bestand.readFully(), Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
        if (tekst != null) _data.value = LerenJson.lees(tekst)
    }

    /** Pas iets aan en bewaar het meteen. Eén wijziging tegelijk, in de goede volgorde. */
    fun wijzig(wat: (LerenData) -> LerenData) {
        werk.launch {
            geladen.await()
            val veranderd = slot.withLock {
                val nieuw = wat(_data.value)
                if (nieuw == _data.value) return@withLock false
                _data.value = nieuw
                schrijf(nieuw)
                true
            }
            if (veranderd) naWijziging?.invoke()
        }
    }

    /** Voor de sync: na elke eigen wijziging. */
    @Volatile var naWijziging: (() -> Unit)? = null

    /** Alles als tekst, voor de sync. */
    suspend fun alsTekst(): String {
        geladen.await()
        return slot.withLock { LerenJson.schrijf(_data.value) }
    }

    /** Gegevens van een ander apparaat: vervangt alles (zonder [naWijziging]). */
    suspend fun vervang(tekst: String) {
        geladen.await()
        slot.withLock {
            val nieuw = LerenJson.lees(tekst)
            _data.value = nieuw
            schrijf(nieuw)
        }
    }

    private fun schrijf(d: LerenData) {
        val uit = try {
            bestand.startWrite()
        } catch (_: Exception) {
            return
        }
        try {
            uit.write(LerenJson.schrijf(d).toByteArray(Charsets.UTF_8))
            bestand.finishWrite(uit)
        } catch (_: Exception) {
            bestand.failWrite(uit)
        }
    }

    companion object {
        @Volatile private var instantie: LerenOpslag? = null

        fun van(context: Context): LerenOpslag = instantie ?: synchronized(this) {
            instantie ?: LerenOpslag(context.applicationContext).also { instantie = it }
        }
    }
}
