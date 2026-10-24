package nl.odelia.dashboard.ui

import nl.odelia.dashboard.onthoud.Concept

/** Iets van buiten de app (delen, widget, melding) dat meteen een scherm moet openen. */
sealed interface Verzoek {
    /** "Onthoud dit", eventueel met tekst of meteen inspreken. */
    data class Onthoud(val concept: Concept) : Verzoek

    /** De urenmail (van de melding op vrijdag). */
    data object Urenmail : Verzoek
}
