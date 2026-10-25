package nl.odelia.dashboard.logica

import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

/** De wekelijkse urenmail aan Kim (Randstad). De app zet alleen een concept klaar; versturen doet Odelia zelf. */
object Urenmail {
    const val ONDERWERP = "Urenregistratie"

    /** Over welke week gaat de mail? Vrijdag t/m zondag: deze week. Maandag t/m donderdag: vorige week. */
    fun maandag(vandaag: LocalDate): LocalDate {
        val dezeWeek = Uren.maandagVan(vandaag)
        return if (vandaag.dayOfWeek >= DayOfWeek.FRIDAY) dezeWeek else dezeWeek.minusWeeks(1)
    }

    fun tekst(dagen: List<Werkdag>, opmerking: String): String = buildString {
        appendLine("Hoi Kim,")
        appendLine()
        appendLine("Hier de uren van afgelopen week.")
        if (opmerking.isNotBlank()) appendLine(opmerking.trim())
        appendLine()
        appendLine("Met hartelijke groet,")
        appendLine("Odelia van der Most")
        appendLine()
        append(tabel(dagen))
    }

    /** Tabel ma–vr: datum, begin, eind, uren. "vrij" of "ziek" als tekst. */
    fun tabel(dagen: List<Werkdag>): String = buildString {
        appendLine("Datum | Begin | Eind | Uren")
        dagen.forEach { d ->
            val datum = "${korteDagNaam(d.datum)} ${d.datum.dayOfMonth}-${d.datum.monthValue}"
            when (d.soort) {
                DagSoort.Vrij -> appendLine("$datum | vrij")
                DagSoort.Ziek -> appendLine("$datum | ziek")
                DagSoort.Leeg -> appendLine("$datum | –")
                DagSoort.Gewerkt -> appendLine(
                    "$datum | ${d.begin?.let(::alsKlok) ?: "–"} | ${d.eind?.let(::alsKlok) ?: "–"} | ${decimaal(d.uren)}",
                )
            }
        }
        append("Totaal | | | ${decimaal(dagen.sumOf { it.uren.toDouble() }.toFloat())}")
    }

    /** 7.25 → "7,25" */
    fun decimaal(uren: Float): String = String.format(Locale.forLanguageTag("nl-NL"), "%.2f", uren)

    private fun korteDagNaam(datum: LocalDate) = listOf("ma", "di", "wo", "do", "vr", "za", "zo")[datum.dayOfWeek.value - 1]
}
