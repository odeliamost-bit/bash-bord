package nl.odelia.dashboard.uren

import java.time.LocalDate

/** Wat je op de schermen met klokken kunt doen. */
class KlokActies(
    /** In als ze uit is, uit als ze in is. */
    val wissel: () -> Unit,
    /** Nieuw of verbeterd blok bewaren. */
    val bewaar: (Blok) -> Unit,
    val verwijder: (blokId: String) -> Unit,
    val zetSoort: (LocalDate, DagSoort) -> Unit,
    val zetWeekdoel: (Float) -> Unit,
) {
    companion object {
        /** Doet niets (voor voorbeelden). */
        val niets = KlokActies({}, {}, {}, { _, _ -> }, {})
    }
}
