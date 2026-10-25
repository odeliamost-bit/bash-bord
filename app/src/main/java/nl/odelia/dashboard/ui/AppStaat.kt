package nl.odelia.dashboard.ui

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import nl.odelia.dashboard.logica.DagSoort
import nl.odelia.dashboard.logica.KlokStaat
import nl.odelia.dashboard.logica.Onthoud
import nl.odelia.dashboard.logica.Periode

/** Alles wat de schermen laten zien. Komt uit de opslag op de telefoon. */
data class AppStaat(
    val klok: KlokStaat,
    val perioden: List<Periode> = emptyList(),
    val soorten: Map<LocalDate, DagSoort> = emptyMap(),
    val onthoud: List<Onthoud> = emptyList(),
) {
    val nu: LocalDateTime get() = klok.nu
}

/** Alles wat de schermen kunnen laten gebeuren. Standaard doet alles niks (handig voor voorbeelden). */
interface AppActies {
    fun wisselKlok() {}
    fun zetDag(datum: LocalDate, soort: DagSoort, begin: LocalTime?, eind: LocalTime?) {}
    fun sluitVergeten(eind: LocalDateTime) {}

    /** Leest tekst voor. Geeft `false` als dat (nog) niet lukt. */
    fun voorlezen(tekst: String): Boolean = false

    /** Start inspreken; [klaar] krijgt de herkende tekst. Geeft `false` als inspreken niet kan. */
    fun inspreken(klaar: (String) -> Unit): Boolean = false

    fun bewaar(onthoud: Onthoud) {}
    fun verwijder(id: String) {}

    /** Open Gmail (of een andere mail-app) met een nieuwe mail. Verstuurt niks. */
    fun openMail(onderwerp: String, tekst: String): Boolean = false
}

/** Opdracht van buitenaf: een melding, widget of andere app opent een bepaald scherm. */
data class Opdracht(val scherm: String, val tekst: String? = null)
