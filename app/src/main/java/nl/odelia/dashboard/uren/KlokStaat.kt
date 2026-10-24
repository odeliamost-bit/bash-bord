package nl.odelia.dashboard.uren

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Alles wat de schermen over klokken moeten weten, uitgerekend voor één moment ([nu]). */
data class KlokStaat(
    val nu: LocalDateTime,
    /** Vandaag ingeklokt en nog niet uit. */
    val isIn: Boolean,
    /** Begin van het blok dat nu loopt. */
    val sinds: LocalTime?,
    /** Tijd sinds inklokken, zonder pauze eraf ("al 5½ uur gewerkt"). */
    val brutoVandaag: Float,
    /** Uren van vandaag met de pauze eraf. */
    val nettoVandaag: Float,
    /** Vandaag al iets geklokt. */
    val gewerktVandaag: Boolean,
    /** Hoe laat ze vandaag uitklokte (als ze klaar is). */
    val uitVandaag: LocalTime?,
    val vandaag: Dag,
    /** Maandag t/m vrijdag van deze week (plus weekend als er toen gewerkt is). */
    val week: List<Dag>,
    /** Netto uren per dag van [week]. */
    val weekUren: List<Float>,
    val weekTotaal: Float,
    val weekdoel: Float,
    /** Een blok van een eerdere dag dat nooit is uitgeklokt. */
    val vergeten: Blok?,
    /** Alle blokken en bijzondere dagen die geladen zijn (ongeveer 6 weken). */
    val blokken: List<Blok>,
    val soorten: Map<LocalDate, DagSoort>,
) {
    /** Eén dag, ook buiten deze week. */
    fun dag(datum: LocalDate): Dag =
        Dag(datum, soorten[datum] ?: DagSoort.Werk, blokken.filter { it.datum == datum }.sortedBy { it.begin })

    fun uren(dag: Dag): Float = Uren.dagUren(dag, nu)

    companion object {
        fun maak(
            blokken: List<Blok>,
            soorten: Map<LocalDate, DagSoort>,
            nu: LocalDateTime,
            weekdoel: Float = Uren.WEEKDOEL,
        ): KlokStaat {
            val datum = nu.toLocalDate()
            val week = Uren.week(Uren.maandag(datum), blokken, soorten)
            val weekUren = week.map { Uren.dagUren(it, nu) }
            val vanVandaag = blokken.filter { it.datum == datum }
            val open = vanVandaag.filter { it.eind == null }.maxByOrNull { it.begin }
            val bruto = Uren.bruto(vanVandaag, nu)
            return KlokStaat(
                nu = nu,
                isIn = open != null,
                sinds = open?.begin?.toLocalTime(),
                brutoVandaag = Uren.alsUren(bruto),
                nettoVandaag = Uren.alsUren(Uren.netto(bruto)),
                gewerktVandaag = vanVandaag.isNotEmpty(),
                uitVandaag = if (open == null) vanVandaag.mapNotNull { it.eind }.maxOrNull()?.toLocalTime() else null,
                vandaag = Dag(datum, soorten[datum] ?: DagSoort.Werk, vanVandaag.sortedBy { it.begin }),
                week = week,
                weekUren = weekUren,
                weekTotaal = weekUren.sum(),
                weekdoel = weekdoel,
                vergeten = blokken.filter { it.eind == null && it.datum < datum }.maxByOrNull { it.begin },
                blokken = blokken,
                soorten = soorten,
            )
        }
    }
}
