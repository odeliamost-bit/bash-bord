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
import nl.odelia.dashboard.data.LerenOpslag
import nl.odelia.dashboard.data.Opslag
import nl.odelia.dashboard.data.PlekOpslag
import nl.odelia.dashboard.logica.AdviesData
import nl.odelia.dashboard.logica.DagSoort
import nl.odelia.dashboard.logica.GeldData
import nl.odelia.dashboard.logica.KlokStaat
import nl.odelia.dashboard.logica.LerenData
import nl.odelia.dashboard.logica.Onthoud
import nl.odelia.dashboard.logica.Plek
import nl.odelia.dashboard.logica.WensenData
import nl.odelia.dashboard.meldingen.Planner
import nl.odelia.dashboard.logica.Zeilweer
import nl.odelia.dashboard.online.Online
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
        combine(basis, leren.data, cyclus.data, wensen.data) { s, l, c, w -> s.copy(leren = l, cyclus = c, wensen = w) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppStaat(KlokStaat.leeg(LocalDateTime.now())))
        combine(basis, leren.data, geld.data) { s, l, g -> s.copy(leren = l, geld = g) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppStaat(KlokStaat.leeg(LocalDateTime.now())))
        combine(basis, leren.data, fotos.data) { s, l, f -> s.copy(leren = l, fotos = f) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppStaat(KlokStaat.leeg(LocalDateTime.now())))
        combine(basis, leren.data, advies.data) { s, l, a -> s.copy(leren = l, advies = a) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppStaat(KlokStaat.leeg(LocalDateTime.now())))

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
    override fun wijzigGeld(wat: (GeldData) -> GeldData) = geld.wijzig(wat)
    override fun wijzigAdvies(wat: (AdviesData) -> AdviesData) = advies.wijzig(wat)

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

    override fun onCleared() {
        voorlezer.stop()
    }
}
