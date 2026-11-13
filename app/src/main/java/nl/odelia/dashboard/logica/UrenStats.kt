package nl.odelia.dashboard.logica

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

/** Urenlogboek (nov 2026): terugkijken en tellen per week, maand en jaar. Alleen op de telefoon. */
object UrenStats {
    /** Opgeteld: netto uren en het aantal gewerkte dagen. */
    data class Som(val uren: Float, val dagen: Int) {
        val gemiddeld: Float get() = if (dagen == 0) 0f else uren / dagen
    }

    private val maandNamen = listOf(
        "januari", "februari", "maart", "april", "mei", "juni",
        "juli", "augustus", "september", "oktober", "november", "december",
    )

    fun naam(maand: YearMonth): String = "${maandNamen[maand.monthValue - 1]} ${maand.year}"

    /** Alle dagen waar iets op staat (gewerkt, vrij of ziek), nieuwste eerst. */
    fun dagen(perioden: List<Periode>, soorten: Map<LocalDate, DagSoort>, nu: LocalDateTime): List<Werkdag> =
        (perioden.map { it.begin.toLocalDate() } + soorten.keys).toSortedSet()
            .map { Uren.werkdag(perioden, soorten, it, nu) }
            .filter { it.soort != DagSoort.Leeg }
            .sortedByDescending { it.datum }

    /** Uren en gewerkte dagen van [van] t/m [tot]. */
    fun som(dagen: List<Werkdag>, van: LocalDate, tot: LocalDate): Som {
        val gewerkt = dagen.filter { it.soort == DagSoort.Gewerkt && !it.datum.isBefore(van) && !it.datum.isAfter(tot) }
        return Som(gewerkt.sumOf { it.uren.toDouble() }.toFloat(), gewerkt.size)
    }

    fun week(dagen: List<Werkdag>, datum: LocalDate): Som = Uren.maandagVan(datum).let { som(dagen, it, it.plusDays(6)) }

    fun maand(dagen: List<Werkdag>, maand: YearMonth): Som = som(dagen, maand.atDay(1), maand.atEndOfMonth())

    fun jaar(dagen: List<Werkdag>, jaar: Int): Som = som(dagen, LocalDate.of(jaar, 1, 1), LocalDate.of(jaar, 12, 31))

    /** De dagen van één maand, nieuwste eerst. */
    fun vanMaand(dagen: List<Werkdag>, maand: YearMonth): List<Werkdag> = dagen.filter { YearMonth.from(it.datum) == maand }
}
