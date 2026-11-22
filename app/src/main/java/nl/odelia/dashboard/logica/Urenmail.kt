package nl.odelia.dashboard.logica

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.IsoFields
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
        appendLine("Hallo Kim,")
        appendLine()
        val week = dagen.firstOrNull()?.datum?.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        appendLine(if (week != null) "Hierbij mijn gewerkte uren van week $week:" else "Hierbij mijn gewerkte uren:")
        if (opmerking.isNotBlank()) appendLine(opmerking.trim())
        appendLine()
        // Geen afsluiting ("Met vriendelijke groet"): Gmail zet Odelia's handtekening er zelf onder.
        append(tabel(dagen))
    }

    /**
     * Eén regel per dag (ma–vr): datum, begin – eind = uren. "vrij" of "ziek" als tekst.
     * Geen uitgelijnde kolommen: Gmail toont mail in een letter waar niet elke letter even breed is,
     * dan staan streepjes en | nooit recht onder elkaar (getest door Odelia, nov 2026).
     */
    fun tabel(dagen: List<Werkdag>): String = buildString {
        dagen.forEach { d ->
            val datum = "${korteDagNaam(d.datum)} ${d.datum.dayOfMonth}-${d.datum.monthValue}"
            val wat = when (d.soort) {
                DagSoort.Vrij -> "vrij"
                DagSoort.Ziek -> "ziek"
                DagSoort.Leeg -> "–"
                DagSoort.Gewerkt ->
                    "${d.begin?.let(::alsKlok) ?: "?"} – ${d.eind?.let(::alsKlok) ?: "?"} = ${decimaal(d.uren)} uur"
            }
            appendLine("$datum: $wat")
        }
        appendLine()
        append("Totaal: ${decimaal(totaal(dagen))} uur")
    }

    /** Totaal = optelsom van de afgeronde dag-uren, zodat het klopt met wat in de tabel staat. */
    fun totaal(dagen: List<Werkdag>): Float =
        dagen.sumOf { Math.round(it.uren * 100.0).toLong() } / 100f

    /** 7.25 → "7,25" */
    fun decimaal(uren: Float): String = String.format(Locale.forLanguageTag("nl-NL"), "%.2f", uren)

    private fun korteDagNaam(datum: LocalDate) = listOf("ma", "di", "wo", "do", "vr", "za", "zo")[datum.dayOfWeek.value - 1]
}
