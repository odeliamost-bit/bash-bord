package nl.odelia.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.odelia.dashboard.data.Backup
import nl.odelia.dashboard.data.Bestanden
import nl.odelia.dashboard.data.GeldOpslag
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.asImageBitmap
import nl.odelia.dashboard.data.FotoOpslag
import nl.odelia.dashboard.data.LerenOpslag
import nl.odelia.dashboard.data.Opslag
import nl.odelia.dashboard.data.PlekOpslag
import nl.odelia.dashboard.logica.CyclusData
import nl.odelia.dashboard.data.AdviesOpslag
import nl.odelia.dashboard.logica.AdviesData
import nl.odelia.dashboard.data.ReceptenOpslag
import nl.odelia.dashboard.logica.DagSoort
import nl.odelia.dashboard.logica.GeldData
import nl.odelia.dashboard.logica.InstellingenData
import nl.odelia.dashboard.logica.KlokStaat
import nl.odelia.dashboard.logica.KompasImport
import nl.odelia.dashboard.logica.LerenData
import nl.odelia.dashboard.logica.Onthoud
import nl.odelia.dashboard.logica.Plek
import nl.odelia.dashboard.logica.WensenData
import nl.odelia.dashboard.meldingen.Planner
import nl.odelia.dashboard.logica.ReceptenData
import nl.odelia.dashboard.logica.Zeilweer
import nl.odelia.dashboard.online.Online
import nl.odelia.dashboard.online.Sync
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.ZeilweerStaat

/** Haalt alles uit de opslag en voert de acties van de schermen uit. */
class DashboardModel(app: Application) : AndroidViewModel(app), AppActies {
    private val opslag = Opslag.van(app)
    private val voorlezer = Voorlezer(app)
    private val leren = LerenOpslag.van(app)
    private val cyclus = Bestanden.cyclus(app)
    private val wensen = Bestanden.wensen(app)
    private val geld = GeldOpslag.van(app)
    private val fotos = FotoOpslag.van(app)
    private val advies = AdviesOpslag.van(app)
    private val recepten = ReceptenOpslag.van(app)
    private val instellingen = Bestanden.instellingen(app)

    /** Elke 20 seconden de tijd, zodat "al 5½ uur gewerkt" vanzelf meeloopt. */
    private val tik = flow {
        while (true) {
            emit(LocalDateTime.now())
            delay(20_000)
        }
    }

    private val zeilweer = MutableStateFlow(ZeilweerStaat(plek = PlekOpslag.lees(app)))

    private val basis =
        combine(opslag.perioden(), opslag.dagSoorten(), opslag.onthoud(), tik, zeilweer) { perioden, soorten, onthoud, nu, weer ->
            AppStaat(KlokStaat.maak(perioden, soorten, nu), perioden, soorten, onthoud, weer)
        }

    val staat: StateFlow<AppStaat> =
        combine(
            combine(basis, leren.data, cyclus.data, wensen.data, geld.data) { s, l, c, w, g -> s.copy(leren = l, cyclus = c, wensen = w, geld = g) },
            fotos.data, advies.data, recepten.data, Sync.staat,
        ) { s, f, a, r, y -> s.copy(fotos = f, advies = a, recepten = r, sync = y) }
            .combine(instellingen.data) { s, i -> s.copy(klok = s.klok.copy(weekdoel = i.weekdoel), instellingen = i) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppStaat(KlokStaat.leeg(LocalDateTime.now())))

    private fun doe(werk: suspend () -> Unit) {
        viewModelScope.launch {
            werk()
            Acties.naWijziging(getApplication<Application>())
        }
    }

    override fun wisselKlok() {
        viewModelScope.launch { Acties.wisselKlok(getApplication<Application>()) }
    }

    override fun zetDag(datum: LocalDate, soort: DagSoort, begin: LocalTime?, eind: LocalTime?) =
        doe { opslag.zetDag(datum, soort, begin, eind) }

    override fun sluitVergeten(eind: LocalDateTime) = doe { opslag.sluitVergeten(eind) }

    override fun voorlezen(tekst: String): Boolean = voorlezer.spreek(tekst)

    override fun bewaar(onthoud: Onthoud) = doe { opslag.bewaar(onthoud) }

    override fun verwijder(id: String) {
        fotos.zet(id, null)
        doe { opslag.verwijder(id) }
    }

    override fun zetFoto(id: String, pad: String?) = fotos.zet(id, pad)

    override fun gooiFotoWeg(pad: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) { fotos.gooiWeg(pad) }
    }

    override fun laadFoto(pad: String) = fotos.laad(pad)?.asImageBitmap()

    /** Camera-foto of gekozen foto opslaan in de app (niet op de hoofdlijn). */
    fun bewaarFoto(beeld: Bitmap, klaar: (String?) -> Unit) {
        viewModelScope.launch {
            klaar(kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { fotos.bewaarBitmap(beeld) })
        }
    }

    fun bewaarFoto(uri: Uri, klaar: (String?) -> Unit) {
        viewModelScope.launch {
            klaar(kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { fotos.bewaarUri(uri) })
        }
    }

    override fun wijzigLeren(wat: (LerenData) -> LerenData) = leren.wijzig(wat)

    override fun wijzigCyclus(wat: (CyclusData) -> CyclusData) =
        cyclus.wijzig(wat) { Planner.plan(getApplication<Application>()) }

    override fun wijzigWensen(wat: (WensenData) -> WensenData) = wensen.wijzig(wat)
    override fun wijzigInstellingen(wat: (InstellingenData) -> InstellingenData) =
        instellingen.wijzig(wat) { Acties.naWijziging(getApplication<Application>()) }

    override fun wijzigGeld(wat: (GeldData) -> GeldData) = geld.wijzig(wat)

    override fun vraagSyncCode(email: String) = Sync.vraagCode(email)
    override fun logInSync(code: String) = Sync.logIn(code)
    override fun logUitSync() = Sync.logUit()
    override fun syncNu() = Sync.nu()
    override fun wijzigAdvies(wat: (AdviesData) -> AdviesData) = advies.wijzig(wat)
    override fun wijzigRecepten(wat: (ReceptenData) -> ReceptenData) = recepten.wijzig(wat)


    // ---- Zeilweer (gaat online; de schermen vragen eerst toestemming) ----

    override fun haalZeilweer() {
        val plek = zeilweer.value.plek ?: return
        online("Het weer ophalen lukte niet. Is er internet?") {
            val bericht = Zeilweer.leesWeer(Online.haal(Zeilweer.weerLink(plek)), plek, LocalDateTime.now())
            zeilweer.update { it.copy(bericht = bericht) }
        }
    }

    override fun zoekPlek(naam: String) {
        if (naam.isBlank()) return
        online("Zoeken lukte niet. Is er internet?") {
            val plekken = Zeilweer.leesPlekken(Online.haal(Zeilweer.plekLink(naam)))
            zeilweer.update { it.copy(plekken = plekken, fout = if (plekken.isEmpty()) "Niks gevonden. Probeer een andere naam." else null) }
        }
    }

    override fun kiesPlek(plek: Plek?) {
        PlekOpslag.bewaar(getApplication<Application>(), plek)
        zeilweer.value = ZeilweerStaat(plek = plek)
    }

    private fun online(fout: String, werk: suspend () -> Unit) {
        zeilweer.update { it.copy(bezig = true, fout = null) }
        viewModelScope.launch {
            try {
                werk()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                zeilweer.update { it.copy(fout = fout) }
            } finally {
                zeilweer.update { it.copy(bezig = false) }
            }
        }
    }

    /** Back-up naar het gekozen bestand schrijven. */
    fun schrijfBackup(uri: Uri, klaar: (String) -> Unit) {
        val app = getApplication<Application>()
        viewModelScope.launch {
            val tekst = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val n = app.contentResolver.openOutputStream(uri, "wt")?.use { Backup.maak(app, it) }
                    if (n == null) "Back-up lukte niet." else "Back-up gemaakt ✅"
                } catch (_: Exception) {
                    "Back-up lukte niet."
                }
            }
            klaar(tekst)
        }
    }

    /** Back-up terugzetten. [klaar] krijgt `null` als het gelukt is (dan moet de app opnieuw starten). */
    fun zetTerug(uri: Uri, klaar: (String?) -> Unit) {
        val app = getApplication<Application>()
        viewModelScope.launch {
            val fout = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    app.contentResolver.openInputStream(uri)?.use { Backup.zetTerug(app, it) } ?: "Bestand openen lukte niet."
                } catch (_: Exception) {
                    "Bestand openen lukte niet."
                }
            }
            klaar(fout)
        }
    }

    /** Kompas-back-up inlezen. Bestaande items (zelfde id) blijven zoals ze zijn. */
    fun importeerKompas(uri: Uri, klaar: (String) -> Unit) {
        val app = getApplication<Application>()
        viewModelScope.launch {
            val inhoud = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    app.contentResolver.openInputStream(uri)?.use { s ->
                        val bytes = s.readNBytesVeilig(MAX_KOMPAS) ?: return@use null
                        KompasImport.lees(String(bytes, Charsets.UTF_8), System.currentTimeMillis())
                    }
                } catch (_: Exception) {
                    null
                }
            }
            if (inhoud == null) {
                klaar("Dit is geen Kompas-back-up.")
                return@launch
            }
            val bestaand = opslag.onthoudNu().map { it.id }.toSet()
            val nieuweOnthoud = inhoud.onthoud.filterNot { it.id in bestaand }
            nieuweOnthoud.forEach { opslag.bewaar(it) }
            val wensIds = wensen.nu().lijst.map { it.id }.toSet()
            val nieuweWensen = inhoud.wensen.filterNot { it.id in wensIds }
            if (nieuweWensen.isNotEmpty()) wensen.wijzig({ d -> nieuweWensen.fold(d) { acc, w -> acc.bewaar(w) } })
            Acties.naWijziging(app)
            klaar(
                if (nieuweOnthoud.isEmpty() && nieuweWensen.isEmpty()) "Alles stond er al in."
                else "Ingelezen: ${nieuweOnthoud.size} in Onthoud, ${nieuweWensen.size} wensen ✅",
            )
        }
    }

    private fun java.io.InputStream.readNBytesVeilig(max: Int): ByteArray? {
        val uit = java.io.ByteArrayOutputStream()
        val buf = ByteArray(64 * 1024)
        while (true) {
            val n = read(buf)
            if (n < 0) break
            uit.write(buf, 0, n)
            if (uit.size() > max) return null
        }
        return uit.toByteArray()
    }

    override fun onCleared() {
        voorlezer.stop()
    }

    private companion object {
        /** Een Kompas-back-up is tekst; groter dan 10 MB is het niet goed. */
        const val MAX_KOMPAS = 10 * 1024 * 1024
    }
}
