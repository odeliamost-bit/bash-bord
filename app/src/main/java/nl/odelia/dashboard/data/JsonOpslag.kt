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
import nl.odelia.dashboard.logica.InstellingenData
import nl.odelia.dashboard.logica.InstellingenJson
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

    /** Voor de sync: na elke eigen wijziging. */
    @Volatile var naWijziging: (() -> Unit)? = null

    /** Alles als tekst, voor de sync. */
    suspend fun alsTekst(): String {
        geladen.await()
        return slot.withLock { schrijf(_data.value) }
    }

    /** Gegevens van een ander apparaat erin zetten (zonder [naWijziging]). */
    suspend fun vervang(wat: (T) -> T) {
        geladen.await()
        slot.withLock {
            val nieuw = wat(_data.value)
            _data.value = nieuw
            bewaar(nieuw)
        }
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
            if (veranderd) {
                na()
                naWijziging?.invoke()
            }
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
    @Volatile private var instellingen: JsonOpslag<InstellingenData>? = null
    @Volatile private var beleggen: JsonOpslag<nl.odelia.dashboard.logica.BeleggenData>? = null

    @Volatile private var uurloon: JsonOpslag<nl.odelia.dashboard.logica.UurloonData>? = null
    @Volatile private var bank: JsonOpslag<nl.odelia.dashboard.logica.BankData>? = null

    /** Geld uit ING-meldingen (aan/uit, onthouden winkels, wat nog wacht). Alleen op dit apparaat: niet in de sync, niet in een back-up. */
    fun bank(context: Context): JsonOpslag<nl.odelia.dashboard.logica.BankData> = bank ?: synchronized(this) {
        bank ?: JsonOpslag(File(context.applicationContext.noBackupFilesDir, "bank.json"), nl.odelia.dashboard.logica.BankData(),
            nl.odelia.dashboard.logica.BankJson::lees, nl.odelia.dashboard.logica.BankJson::schrijf)
            .also { bank = it }
    }

    /** Uurloon: alleen op dit apparaat. Niet in de Google-back-up, niet in de sync en niet in de eigen back-up. */
    fun uurloon(context: Context): JsonOpslag<nl.odelia.dashboard.logica.UurloonData> = uurloon ?: synchronized(this) {
        uurloon ?: JsonOpslag(File(context.applicationContext.noBackupFilesDir, "uurloon.json"), nl.odelia.dashboard.logica.UurloonData(),
            nl.odelia.dashboard.logica.UurloonJson::lees, nl.odelia.dashboard.logica.UurloonJson::schrijf)
            .also { uurloon = it }
    }

    /** Beleggen: alleen op deze telefoon, niet in de Google-back-up en niet in de sync. */
    fun beleggen(context: Context): JsonOpslag<nl.odelia.dashboard.logica.BeleggenData> = beleggen ?: synchronized(this) {
        beleggen ?: JsonOpslag(File(context.applicationContext.noBackupFilesDir, "beleggen.json"), nl.odelia.dashboard.logica.BeleggenData(),
            nl.odelia.dashboard.logica.BeleggenJson::lees, nl.odelia.dashboard.logica.BeleggenJson::schrijf)
            .also { beleggen = it }
    }

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

    @Volatile private var logboek: JsonOpslag<nl.odelia.dashboard.logica.LogboekData>? = null
    @Volatile private var apiKosten: JsonOpslag<nl.odelia.dashboard.logica.ApiKostenData>? = null

    /** Logboek "laatst gebeurd". Alleen op dit apparaat: niet in de sync, niet in een back-up. */
    fun logboek(context: Context): JsonOpslag<nl.odelia.dashboard.logica.LogboekData> = logboek ?: synchronized(this) {
        logboek ?: JsonOpslag(File(context.applicationContext.noBackupFilesDir, "logboek.json"), nl.odelia.dashboard.logica.LogboekData(),
            nl.odelia.dashboard.logica.Logboek::lees, nl.odelia.dashboard.logica.Logboek::schrijf)
            .also { logboek = it }
    }

    /** Schrijf één regel in het logboek. */
    fun log(context: Context, icoon: String, tekst: String) =
        logboek(context).wijzig({ nl.odelia.dashboard.logica.Logboek.voegToe(it, nl.odelia.dashboard.logica.LogRegel(java.time.LocalDateTime.now(), icoon, tekst)) })

    @Volatile private var poort: JsonOpslag<nl.odelia.dashboard.logica.PoortData>? = null

    /** Poort-check: aan/uit en de plek van de poort. Alleen op dit apparaat: niet in de sync, niet in een back-up. */
    fun poort(context: Context): JsonOpslag<nl.odelia.dashboard.logica.PoortData> = poort ?: synchronized(this) {
        poort ?: JsonOpslag(File(context.applicationContext.noBackupFilesDir, "poort.json"), nl.odelia.dashboard.logica.PoortData(),
            nl.odelia.dashboard.logica.Poort::lees, nl.odelia.dashboard.logica.Poort::schrijf)
            .also { poort = it }
    }

    /** Claude-kosten deze maand (zelf ingevuld) + limiet. Alleen op dit apparaat. */
    fun apiKosten(context: Context): JsonOpslag<nl.odelia.dashboard.logica.ApiKostenData> = apiKosten ?: synchronized(this) {
        apiKosten ?: JsonOpslag(File(context.applicationContext.noBackupFilesDir, "apikosten.json"), nl.odelia.dashboard.logica.ApiKostenData(),
            nl.odelia.dashboard.logica.ApiKosten::lees, nl.odelia.dashboard.logica.ApiKosten::schrijf)
            .also { apiKosten = it }
    }

    /** Wat Odelia zelf instelt (tijden, tegels, grote tekst …). Op het apparaat, niet online. */
    fun instellingen(context: Context): JsonOpslag<InstellingenData> = instellingen ?: synchronized(this) {
        instellingen ?: JsonOpslag(File(context.applicationContext.filesDir, "instellingen.json"), InstellingenData(), InstellingenJson::lees, InstellingenJson::schrijf)
            .also { instellingen = it }
    }
}
