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
import nl.odelia.dashboard.data.KluisOpslag
import nl.odelia.dashboard.data.LerenOpslag
import nl.odelia.dashboard.data.Opslag
import nl.odelia.dashboard.data.PlekOpslag
import nl.odelia.dashboard.data.ReceptenOpslag
import nl.odelia.dashboard.logica.DagSoort
import nl.odelia.dashboard.logica.KlokStaat
import nl.odelia.dashboard.logica.KluisStaat
import nl.odelia.dashboard.logica.LerenData
import nl.odelia.dashboard.logica.Onthoud
import nl.odelia.dashboard.logica.Plek
import nl.odelia.dashboard.logica.ReceptenData
import nl.odelia.dashboard.logica.WachtwoordenData
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
    private val recepten = ReceptenOpslag.van(app)
    private val kluisOpslag = KluisOpslag.van(app)
    private val kluis = MutableStateFlow(KluisStaat())

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
        combine(basis, leren.data, kluis, recepten.data) { s, l, k, r -> s.copy(leren = l, kluis = k, recepten = r) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppStaat(KlokStaat.leeg(LocalDateTime.now())))

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

    override fun verwijder(id: String) = doe { opslag.verwijder(id) }

    override fun wijzigLeren(wat: (LerenData) -> LerenData) = leren.wijzig(wat)

    override fun wijzigRecepten(wat: (ReceptenData) -> ReceptenData) = recepten.wijzig(wat)

    // ---- Wachtwoorden (versleuteld; ontgrendelen doet MainActivity met de telefoon zelf) ----

    /** Na gelukt ontgrendelen: ontsleutelen en laten zien. */
    fun kluisOpen() {
        viewModelScope.launch {
            kluis.value = try {
                KluisStaat(open = true, data = kluisOpslag.lees())
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                KluisStaat(fout = "Openen lukte niet. Probeer het nog een keer.")
            }
        }
    }

    fun kluisFout(tekst: String?) {
        kluis.value = KluisStaat(fout = tekst)
    }

    override fun opSlot() {
        if (kluis.value != KluisStaat()) kluis.value = KluisStaat()
    }

    override fun wijzigWachtwoorden(wat: (WachtwoordenData) -> WachtwoordenData) {
        val nu = kluis.value
        if (!nu.open) return
        val nieuw = wat(nu.data)
        kluis.value = nu.copy(data = nieuw)
        viewModelScope.launch {
            try {
                kluisOpslag.bewaar(nieuw)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                kluis.update { it.copy(fout = "Bewaren lukte niet.") }
            }
        }
    }

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
