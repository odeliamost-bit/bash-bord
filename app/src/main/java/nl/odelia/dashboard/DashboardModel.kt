package nl.odelia.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.odelia.dashboard.data.Instellingen
import nl.odelia.dashboard.data.KlokGegevens
import nl.odelia.dashboard.data.KlokOpslag
import nl.odelia.dashboard.uren.KlokActies

/** Houdt de gegevens vast zolang de app open is, ook als je het scherm draait. */
class DashboardModel(app: Application) : AndroidViewModel(app) {
    private val klok = KlokOpslag(app)
    private val instellingen = Instellingen.van(app)

    /** `null` zolang de database nog laadt. */
    val klokGegevens: StateFlow<KlokGegevens?> =
        klok.gegevens().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val weekdoel: StateFlow<Float> = instellingen.weekdoel

    val klokActies = KlokActies(
        wissel = { viewModelScope.launch { klok.wissel() } },
        bewaar = { blok -> viewModelScope.launch { klok.bewaar(blok) } },
        verwijder = { id -> viewModelScope.launch { klok.verwijder(id) } },
        zetSoort = { datum, soort -> viewModelScope.launch { klok.zetSoort(datum, soort) } },
        zetWeekdoel = instellingen::zetWeekdoel,
    )
}
