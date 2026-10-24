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
import nl.odelia.dashboard.data.OnthoudOpslag
import nl.odelia.dashboard.onthoud.Afspraak
import nl.odelia.dashboard.onthoud.OnthoudActies
import nl.odelia.dashboard.onthoud.Persoon
import nl.odelia.dashboard.uren.KlokActies

/** Houdt de gegevens vast zolang de app open is, ook als je het scherm draait. */
class DashboardModel(app: Application) : AndroidViewModel(app) {
    private val klok = KlokOpslag(app)
    private val onthoud = OnthoudOpslag(app)
    private val instellingen = Instellingen.van(app)

    /** `null` zolang de database nog laadt. */
    val klokGegevens: StateFlow<KlokGegevens?> =
        klok.gegevens().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val weekdoel: StateFlow<Float> = instellingen.weekdoel
    val kimAdres: StateFlow<String> = instellingen.kimAdres
    fun zetKimAdres(adres: String) = instellingen.zetKimAdres(adres)

    /** Afspraken en personen; `null` zolang de database nog laadt. */
    val onthoudGegevens: StateFlow<Pair<List<Afspraak>, List<Persoon>>?> =
        onthoud.gegevens().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Onthoud-acties; inspreken kan alleen de Activity (die opent de spraakherkenning). */
    fun onthoudActies(inspreken: ((String) -> Unit) -> Unit) = OnthoudActies(
        bewaar = { a -> viewModelScope.launch { onthoud.bewaar(a) } },
        zetKlaar = { id, klaar -> viewModelScope.launch { onthoud.zetKlaar(id, klaar) } },
        verwijder = { id -> viewModelScope.launch { onthoud.verwijder(id) } },
        nieuwePersoon = { naam, groep, rol, klaar -> viewModelScope.launch { klaar(onthoud.nieuwePersoon(naam, groep, rol)) } },
        inspreken = inspreken,
    )

    val klokActies = KlokActies(
        wissel = { viewModelScope.launch { klok.wissel() } },
        bewaar = { blok -> viewModelScope.launch { klok.bewaar(blok) } },
        verwijder = { id -> viewModelScope.launch { klok.verwijder(id) } },
        zetSoort = { datum, soort -> viewModelScope.launch { klok.zetSoort(datum, soort) } },
        zetWeekdoel = instellingen::zetWeekdoel,
    )
}
