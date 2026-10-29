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
import nl.odelia.dashboard.logica.ReceptenData
import nl.odelia.dashboard.logica.ReceptenJson
import nl.odelia.dashboard.logica.ReceptenStart

/** Bewaart recepten en ideeën in `recepten.json` op de telefoon. De eerste keer: de recepten uit het ontwerp. */
class ReceptenOpslag private constructor(context: Context) {
    private val bestand = AtomicFile(File(context.filesDir, "recepten.json"))
    private val werk = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val slot = Mutex()
    private val _data = MutableStateFlow(ReceptenData(ReceptenStart.recepten))
    val data: StateFlow<ReceptenData> = _data.asStateFlow()

    private val geladen = werk.async {
        val tekst = try {
            String(bestand.readFully(), Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
        if (tekst != null) _data.value = ReceptenJson.lees(tekst)
    }

    fun wijzig(wat: (ReceptenData) -> ReceptenData) {
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
        return slot.withLock { ReceptenJson.schrijf(_data.value) }
    }

    /** Gegevens van een ander apparaat: vervangt alles (zonder [naWijziging]). */
    suspend fun vervang(tekst: String) {
        geladen.await()
        slot.withLock {
            val nieuw = ReceptenJson.lees(tekst)
            _data.value = nieuw
            schrijf(nieuw)
        }
    }

    private fun schrijf(d: ReceptenData) {
        val uit = try {
            bestand.startWrite()
        } catch (_: Exception) {
            return
        }
        try {
            uit.write(ReceptenJson.schrijf(d).toByteArray(Charsets.UTF_8))
            bestand.finishWrite(uit)
        } catch (_: Exception) {
            bestand.failWrite(uit)
        }
    }

    companion object {
        @Volatile private var instantie: ReceptenOpslag? = null

        fun van(context: Context): ReceptenOpslag = instantie ?: synchronized(this) {
            instantie ?: ReceptenOpslag(context.applicationContext).also { instantie = it }
        }
    }
}
