package nl.odelia.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.odelia.dashboard.data.Opslag
import nl.odelia.dashboard.logica.DagSoort
import nl.odelia.dashboard.logica.KlokStaat
import nl.odelia.dashboard.logica.Onthoud
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat

/** Haalt alles uit de opslag en voert de acties van de schermen uit. */
class DashboardModel(app: Application) : AndroidViewModel(app), AppActies {
    private val opslag = Opslag.van(app)
    private val voorlezer = Voorlezer(app)

    /** Elke 20 seconden de tijd, zodat "al 5½ uur gewerkt" vanzelf meeloopt. */
    private val tik = flow {
        while (true) {
            emit(LocalDateTime.now())
            delay(20_000)
        }
    }

    val staat: StateFlow<AppStaat> =
        combine(opslag.perioden(), opslag.dagSoorten(), opslag.onthoud(), tik) { perioden, soorten, onthoud, nu ->
            AppStaat(KlokStaat.maak(perioden, soorten, nu), perioden, soorten, onthoud)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppStaat(KlokStaat.leeg(LocalDateTime.now())))

    private fun doe(werk: suspend () -> Unit) {
        viewModelScope.launch {
            werk()
            Acties.naWijziging(getApplication())
        }
    }

    override fun wisselKlok() {
        viewModelScope.launch { Acties.wisselKlok(getApplication()) }
    }

    override fun zetDag(datum: LocalDate, soort: DagSoort, begin: LocalTime?, eind: LocalTime?) =
        doe { opslag.zetDag(datum, soort, begin, eind) }

    override fun sluitVergeten(eind: LocalDateTime) = doe { opslag.sluitVergeten(eind) }

    override fun voorlezen(tekst: String): Boolean = voorlezer.spreek(tekst)

    override fun bewaar(onthoud: Onthoud) = doe { opslag.bewaar(onthoud) }

    override fun verwijder(id: String) = doe { opslag.verwijder(id) }

    override fun onCleared() {
        voorlezer.stop()
    }
}
