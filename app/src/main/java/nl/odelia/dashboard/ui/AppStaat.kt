package nl.odelia.dashboard.ui

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import nl.odelia.dashboard.logica.DagSoort
import nl.odelia.dashboard.logica.KlokStaat
import nl.odelia.dashboard.logica.Onthoud
import nl.odelia.dashboard.logica.Periode
import nl.odelia.dashboard.logica.Plek
import nl.odelia.dashboard.logica.Weerbericht

/** Alles wat de schermen laten zien. Komt uit de opslag op de telefoon. */
data class AppStaat(
    val klok: KlokStaat,
    val perioden: List<Periode> = emptyList(),
    val soorten: Map<LocalDate, DagSoort> = emptyMap(),
    val onthoud: List<Onthoud> = emptyList(),
    val zeilweer: ZeilweerStaat = ZeilweerStaat(),
) {
    val nu: LocalDateTime get() = klok.nu
}

/** Zeilweer: de gekozen plek, het laatst opgehaalde weer, en of de app nu bezig is. */
data class ZeilweerStaat(
    val plek: Plek? = null,
    val bericht: Weerbericht? = null,
    val plekken: List<Plek> = emptyList(),
    val bezig: Boolean = false,
    val fout: String? = null,
)

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

    /** Open een https-link in Chrome of Google Maps (de app zelf gaat dan niet online). */
    fun openLink(link: String): Boolean = false

    // Zeilweer. Deze gaan wél online: alleen aanroepen nadat Odelia "ja" heeft gezegd.
    fun haalZeilweer() {}
    fun zoekPlek(naam: String) {}
    fun kiesPlek(plek: Plek?) {}
}

/** Opdracht van buitenaf: een melding, widget of andere app opent een bepaald scherm. */
data class Opdracht(val scherm: String, val tekst: String? = null)
