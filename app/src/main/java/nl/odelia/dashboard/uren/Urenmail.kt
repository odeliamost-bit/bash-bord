package nl.odelia.dashboard.uren

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.roundToInt

/**
 * De wekelijkse urenmail voor Kim (Randstad). Alleen een CONCEPT: de app verstuurt nooit zelf.
 * Tekst uit CLAUDE.md, met daaronder per dag (ma–vr): datum, begin, eind, uren, of "vrij"/"ziek".
 */
object Urenmail {
    const val ONDERWERP = "Urenregistratie"

    /** Vrijdag, zoveel na het uitklokken: seintje dat de urenmail klaarstaat. */
    val NA_UITKLOKKEN: Duration = Duration.ofMinutes(30)

    /** Eén regel van het overzicht. [uren] is leeg bij vrij, ziek of als er niet geklokt is. */
    data class Regel(val datum: LocalDate, val begin: LocalTime?, val eind: LocalTime?, val uren: Float?, val soort: DagSoort)

    /** Welke week standaard: vanaf donderdag deze week, eerder in de week de vorige week. */
    fun standaardWeek(vandaag: LocalDate): LocalDate {
        val maandag = Uren.maandag(vandaag)
        return if (vandaag.dayOfWeek >= DayOfWeek.THURSDAY) maandag else maandag.minusWeeks(1)
    }

    fun regels(maandag: LocalDate, staat: KlokStaat): List<Regel> = (0L until 5L).map { plus ->
        val dag = staat.dag(maandag.plusDays(plus))
        val uren = if (dag.soort == DagSoort.Werk && dag.blokken.isNotEmpty()) staat.uren(dag) else null
        Regel(dag.datum, dag.begin, dag.eind, uren, dag.soort)
    }

    fun tekst(regels: List<Regel>, opmerking: String): String = buildString {
        append("Hoi Kim,\n\n")
        append("Hier de uren van afgelopen week.\n")
        if (opmerking.isNotBlank()) append(opmerking.trim()).append("\n")
        append("\nMet hartelijke groet,\nOdelia van der Most\n\n")
        regels.forEach { append(regel(it)).append("\n") }
        append("Totaal: ${totaal(regels)}\n")
    }

    /** "22:20 uur", met vrije en zieke dagen erachter: "22:20 uur · 1 dag vrij · 1 dag ziek" (keuze van Odelia). */
    fun totaal(regels: List<Regel>): String {
        val gewerkt = "${uurMin(regels.sumOf { (it.uren ?: 0f).toDouble() }.toFloat())} uur"
        return listOfNotNull(gewerkt, Uren.vrijZiek(regels.map { it.soort })).joinToString(" · ")
    }

    /** "ma 19-10-2026: 7:45 – 15:45 = 7:15 uur", of "wo 21-10-2026: vrij". */
    fun regel(r: Regel): String {
        val datum = "${DAGEN[r.datum.dayOfWeek.value - 1]} ${"%02d-%02d-%d".format(r.datum.dayOfMonth, r.datum.monthValue, r.datum.year)}"
        val rest = when {
            r.soort != DagSoort.Werk -> r.soort.tekst
            r.uren == null || r.begin == null -> "–"
            else -> "${tijd(r.begin)} – ${r.eind?.let(::tijd) ?: "…"} = ${uurMin(r.uren)} uur"
        }
        return "$datum: $rest"
    }

    /** Uren als uren en minuten (keuze van Odelia): 7.25 → "7:15", 7.0 → "7:00". */
    fun uurMin(uren: Float): String {
        val minuten = (uren * 60).roundToInt()
        return "${minuten / 60}:${(minuten % 60).toString().padStart(2, '0')}"
    }

    /** Vrijdag na het uitklokken, of later in het weekend: dan "staat de urenmail klaar". */
    fun staatKlaar(nu: LocalDateTime, staat: KlokStaat): Boolean = when (nu.dayOfWeek) {
        DayOfWeek.FRIDAY -> !staat.isIn && staat.uitVandaag != null
        DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> true
        else -> false
    }

    private fun tijd(t: LocalTime) = "${t.hour}:${t.minute.toString().padStart(2, '0')}"

    private val DAGEN = listOf("ma", "di", "wo", "do", "vr", "za", "zo")
}
