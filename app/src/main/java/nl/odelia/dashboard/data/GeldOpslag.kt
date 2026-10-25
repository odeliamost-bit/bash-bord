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
import nl.odelia.dashboard.logica.GeldData
import nl.odelia.dashboard.logica.GeldJson

/**
 * Bewaart Geld (potjes, uitgaven, tags) in één bestandje op de telefoon.
 * Een los bestand, zodat de database van uren en afspraken niet hoeft te veranderen.
 * Gaat nooit online.
 */
class GeldOpslag private constructor(context: Context) {
    private val bestand = AtomicFile(File(context.filesDir, "geld.json"))
    private val werk = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val slot = Mutex()
    private val _data = MutableStateFlow(GeldData())
    val data: StateFlow<GeldData> = _data.asStateFlow()

    private val geladen = werk.async {
        val tekst = try {
            String(bestand.readFully(), Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
        if (tekst != null) _data.value = GeldJson.lees(tekst)
    }

    /** Pas iets aan en bewaar het meteen. Eén wijziging tegelijk, in de goede volgorde. */
    fun wijzig(wat: (GeldData) -> GeldData) {
        werk.launch {
            geladen.await()
            slot.withLock {
                val nieuw = wat(_data.value)
                if (nieuw == _data.value) return@withLock
                _data.value = nieuw
                schrijf(nieuw)
            }
        }
    }

    private fun schrijf(d: GeldData) {
        val uit = try {
            bestand.startWrite()
        } catch (_: Exception) {
            return
        }
        try {
            uit.write(GeldJson.schrijf(d).toByteArray(Charsets.UTF_8))
            bestand.finishWrite(uit)
        } catch (_: Exception) {
            bestand.failWrite(uit)
        }
    }

    companion object {
        @Volatile private var instantie: GeldOpslag? = null

        fun van(context: Context): GeldOpslag = instantie ?: synchronized(this) {
            instantie ?: GeldOpslag(context.applicationContext).also { instantie = it }
        }
    }
}
