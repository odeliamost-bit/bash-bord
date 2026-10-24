package nl.odelia.dashboard.uren

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

/** Eén keer in- en uitklokken. [eind] is leeg zolang ze nog aan het werk is. */
data class Blok(val id: String, val begin: LocalDateTime, val eind: LocalDateTime?) {
    val datum: LocalDate get() = begin.toLocalDate()
}

/** Wat voor dag het was. Vrij en ziek komen als tekst in de urenmail. */
enum class DagSoort(val tekst: String) {
    Werk("gewerkt"),
    Vrij("vrij"),
    Ziek("ziek"),
}

/** Alles wat er over één dag bekend is. */
data class Dag(val datum: LocalDate, val soort: DagSoort, val blokken: List<Blok>) {
    val begin: LocalTime? get() = blokken.minOfOrNull { it.begin }?.toLocalTime()
    val eind: LocalTime? get() = if (blokken.any { it.eind == null }) null else blokken.mapNotNull { it.eind }.maxOrNull()?.toLocalTime()
}

/** De rekenregels voor de uren. Pauze: altijd 45 minuten eraf (keuze van Odelia, 24 okt). */
object Uren {
    val PAUZE: Duration = Duration.ofMinutes(45)
    const val WEEKDOEL = 35f

    /**
     * Tijd tussen in- en uitklokken, zonder pauze eraf.
     * Een open blok van vandaag telt tot [nu]. Een vergeten open blok van een eerdere dag telt niet mee:
     * daar weten we de eindtijd niet van.
     */
    fun bruto(blokken: List<Blok>, nu: LocalDateTime): Duration =
        blokken.fold(Duration.ZERO) { som, b ->
            val tot = b.eind ?: if (b.datum == nu.toLocalDate()) maxOf(nu, b.begin) else b.begin
            som + Duration.between(b.begin, tot).coerceAtLeast(Duration.ZERO)
        }

    /** Gewerkte tijd met de pauze eraf. Nooit minder dan nul. */
    fun netto(bruto: Duration): Duration =
        if (bruto.isZero) Duration.ZERO else (bruto - PAUZE).coerceAtLeast(Duration.ZERO)

    fun alsUren(d: Duration): Float = d.toMinutes() / 60f

    /** Maandag van de week waarin [datum] valt. */
    fun maandag(datum: LocalDate): LocalDate = datum.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    /**
     * Zet losse blokken en dag-soorten om in dagen van één week.
     * Altijd maandag t/m vrijdag; zaterdag en zondag alleen als er toen gewerkt is.
     */
    fun week(
        maandag: LocalDate,
        blokken: List<Blok>,
        soorten: Map<LocalDate, DagSoort>,
    ): List<Dag> = (0L until 7L).mapNotNull { plus ->
        val datum = maandag.plusDays(plus)
        val vanDag = blokken.filter { it.datum == datum }.sortedBy { it.begin }
        val weekend = datum.dayOfWeek == DayOfWeek.SATURDAY || datum.dayOfWeek == DayOfWeek.SUNDAY
        if (weekend && vanDag.isEmpty()) null else Dag(datum, soorten[datum] ?: DagSoort.Werk, vanDag)
    }

    /**
     * "1 dag vrij · 2 dagen ziek", of `null` als er geen vrije of zieke dagen zijn.
     * Ze tellen niet mee voor het weekdoel, maar staan er wel bij (keuze van Odelia).
     */
    fun vrijZiek(soorten: List<DagSoort>): String? {
        fun stuk(soort: DagSoort): String? = soorten.count { it == soort }.takeIf { it > 0 }?.let { n ->
            "$n ${if (n == 1) "dag" else "dagen"} ${soort.tekst}"
        }
        return listOfNotNull(stuk(DagSoort.Vrij), stuk(DagSoort.Ziek)).joinToString(" · ").ifEmpty { null }
    }

    /** Netto uren van een dag. Vrij of ziek = 0 (telt niet mee voor het weekdoel). */
    fun dagUren(dag: Dag, nu: LocalDateTime): Float =
        if (dag.soort == DagSoort.Werk) alsUren(netto(bruto(dag.blokken, nu))) else 0f
}

private fun Duration.coerceAtLeast(min: Duration): Duration = if (this < min) min else this
