package nl.odelia.dashboard.logica

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.roundToInt

/** Pauze die er elke gewerkte dag vast af gaat. */
const val PAUZE_MINUTEN = 45L

/** Eén keer in- en uitklokken. [eind] is `null` zolang je nog aan het werk bent. */
data class Periode(val begin: LocalDateTime, val eind: LocalDateTime?)

enum class DagSoort { Gewerkt, Vrij, Ziek, Leeg }

/** Eén dag in het overzicht. [uren] is netto: de pauze is er al af. */
data class Werkdag(
    val datum: LocalDate,
    val soort: DagSoort,
    val begin: LocalTime?,
    val eind: LocalTime?,
    val uren: Float,
    val bezig: Boolean,
)

object Uren {
    /** Minuten gewerkt op [datum], zonder pauze eraf. Een open periode loopt tot [nu]. */
    fun brutoMinuten(perioden: List<Periode>, datum: LocalDate, nu: LocalDateTime): Long {
        val dagBegin = datum.atStartOfDay()
        val dagEind = datum.plusDays(1).atStartOfDay()
        return perioden.sumOf { p ->
            val eind = eindVan(p, nu) ?: return@sumOf 0L
            val van = maxOf(p.begin, dagBegin)
            val tot = minOf(eind, dagEind)
            if (tot.isAfter(van)) Duration.between(van, tot).toMinutes() else 0L
        }
    }

    /** Netto uren: bruto min de vaste pauze, nooit minder dan 0. */
    fun netto(brutoMinuten: Long): Float = (brutoMinuten - PAUZE_MINUTEN).coerceAtLeast(0) / 60f

    fun werkdag(perioden: List<Periode>, soorten: Map<LocalDate, DagSoort>, datum: LocalDate, nu: LocalDateTime): Werkdag {
        val soort = soorten[datum]
        if (soort == DagSoort.Vrij || soort == DagSoort.Ziek) {
            return Werkdag(datum, soort, null, null, 0f, false)
        }
        val vanDeDag = perioden.filter { it.begin.toLocalDate() == datum }.sortedBy { it.begin }
        if (vanDeDag.isEmpty()) return Werkdag(datum, DagSoort.Leeg, null, null, 0f, false)
        val laatste = vanDeDag.last()
        val bezig = laatste.eind == null && datum == nu.toLocalDate()
        return Werkdag(
            datum = datum,
            soort = DagSoort.Gewerkt,
            begin = vanDeDag.first().begin.toLocalTime(),
            eind = laatste.eind?.toLocalTime(),
            uren = netto(brutoMinuten(perioden, datum, nu)),
            bezig = bezig,
        )
    }

    /** Maandag t/m vrijdag van de week waar [datum] in valt. */
    fun werkweek(perioden: List<Periode>, soorten: Map<LocalDate, DagSoort>, datum: LocalDate, nu: LocalDateTime): List<Werkdag> {
        val maandag = maandagVan(datum)
        return (0L..4L).map { werkdag(perioden, soorten, maandag.plusDays(it), nu) }
    }

    /** Alle uren van de hele week (ook als je in het weekend werkt). */
    fun weekTotaal(perioden: List<Periode>, soorten: Map<LocalDate, DagSoort>, datum: LocalDate, nu: LocalDateTime): Float {
        val maandag = maandagVan(datum)
        return (0L..6L).sumOf { werkdag(perioden, soorten, maandag.plusDays(it), nu).uren.toDouble() }.toFloat()
    }

    fun maandagVan(datum: LocalDate): LocalDate = datum.minusDays((datum.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())

    /** Een open periode van een eerdere dag: vergeten uit te klokken. */
    fun vergeten(perioden: List<Periode>, nu: LocalDateTime): Periode? =
        perioden.filter { it.eind == null && it.begin.toLocalDate().isBefore(nu.toLocalDate()) }.maxByOrNull { it.begin }

    /**
     * Wanneer stopt deze periode? Nog bezig vandaag: tot nu.
     * Vergeten uit te klokken (open van een eerdere dag): telt niet mee tot je het aanpast.
     */
    private fun eindVan(p: Periode, nu: LocalDateTime): LocalDateTime? = when {
        p.eind != null -> p.eind
        p.begin.toLocalDate() == nu.toLocalDate() -> nu
        else -> null
    }
}

/** Uren als getal met breuk: 34.25 → "34¼", 7.0 → "7", 5.5 → "5½". Op een kwartier afgerond. */
fun alsBreuk(uren: Float): String {
    val kwartieren = (uren * 4).roundToInt()
    val heel = kwartieren / 4
    val rest = when (kwartieren % 4) {
        1 -> "¼"
        2 -> "½"
        3 -> "¾"
        else -> ""
    }
    return if (heel == 0 && rest.isNotEmpty()) rest else "$heel$rest"
}

/** "7:45" */
fun alsKlok(tijd: LocalTime): String = "${tijd.hour}:${tijd.minute.toString().padStart(2, '0')}"
