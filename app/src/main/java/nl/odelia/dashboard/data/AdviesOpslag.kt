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
import nl.odelia.dashboard.logica.AdviesData
import nl.odelia.dashboard.logica.AdviesJson

/**
 * Bewaart Advies (je ⭐-favorieten) in één bestandje op de telefoon.
 * Een los bestand, zodat de database van uren en afspraken niet hoeft te veranderen.
 * Gaat versleuteld mee in de sync (nov 2026), zie `online/Sync.kt`.
 */
class AdviesOpslag private constructor(context: Context) {
    private val bestand = AtomicFile(File(context.filesDir, "advies.json"))
    private val werk = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val slot = Mutex()
    private val _data = MutableStateFlow(AdviesData())
    val data: StateFlow<AdviesData> = _data.asStateFlow()

    private val geladen = werk.async {
        val tekst = try {
            String(bestand.readFully(), Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
        if (tekst != null) _data.value = AdviesJson.lees(tekst)
    }

    /** Voor de sync: na elke eigen wijziging. */
    @Volatile var naWijziging: (() -> Unit)? = null

    suspend fun alsTekst(): String {
        geladen.await()
        return slot.withLock { AdviesJson.schrijf(_data.value) }
    }

    /** Gegevens van een ander apparaat erin zetten (zonder [naWijziging]). */
    suspend fun vervang(tekst: String) {
        geladen.await()
        val nieuw = AdviesJson.lees(tekst)
        slot.withLock {
            _data.value = nieuw
            schrijf(nieuw)
        }
    }

    /** Pas iets aan en bewaar het meteen. Eén wijziging tegelijk, in de goede volgorde. */
    fun wijzig(wat: (AdviesData) -> AdviesData) {
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

    private fun schrijf(d: AdviesData) {
        val uit = try {
            bestand.startWrite()
        } catch (_: Exception) {
            return
        }
        try {
            uit.write(AdviesJson.schrijf(d).toByteArray(Charsets.UTF_8))
            bestand.finishWrite(uit)
        } catch (_: Exception) {
            bestand.failWrite(uit)
        }
    }

    companion object {
        @Volatile private var instantie: AdviesOpslag? = null

        fun van(context: Context): AdviesOpslag = instantie ?: synchronized(this) {
            instantie ?: AdviesOpslag(context.applicationContext).also { instantie = it }
        }
    }
}
