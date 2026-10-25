package nl.odelia.dashboard.logica

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/** Eén keer ongesteld. [eind] is `null` zolang ze nog niet op "Ik ben klaar" tikte. */
data class Bloeding(val begin: LocalDate, val eind: LocalDate?)

/**
 * Hoe het er vandaag voor staat. Alles is een schatting, geen medisch advies.
 * [dag] is dag 1 op de eerste dag ongesteld; `null` als er nog niets is ingevuld.
 */
data class CyclusStand(
    val dag: Int?,
    val ongesteld: Boolean,
    /** Gemiddelde cycluslengte in dagen (of de begin-lengte als er nog te weinig is). */
    val lengte: Int,
    /** Gemiddeld aantal dagen ongesteld, voor de roze vakjes in de ring. */
    val duur: Int,
    /** `true` zolang er nog geen hele cyclus is om van te leren. */
    val nogAanHetLeren: Boolean,
    val volgende: LocalDate?,
    /** Vroegste en laatste verwachte dag. Alleen als er 3 of meer cycli zijn. */
    val vroegst: LocalDate?,
    val laatst: LocalDate?,
    /** Dagen tot [volgende]. 0 of minder = kan elk moment. */
    val dagenTot: Long?,
)

object Cyclus {
    const val BEGIN_LENGTE = 28
    const val BEGIN_DUUR = 5

    /** Korter of langer telt niet mee: dan is er waarschijnlijk een keer vergeten te tikken. */
    const val KORTSTE = 15
    const val LANGSTE = 60

    /** Zo veel laatste cycli tellen mee voor het gemiddelde. */
    const val MEE_TELLEN = 6

    /** Nog niet op "klaar" getikt na zo veel dagen? Dan is dat vast vergeten. */
    const val LANGSTE_DUUR = 10

    /** Seintje zo veel dagen voor de verwachte dag. */
    const val SEINTJE_DAGEN = 2L

    /** Lengtes van de laatste cycli (start tot start), zonder uitschieters. */
    fun lengtes(bloedingen: List<Bloeding>): List<Int> =
        bloedingen.map { it.begin }.distinct().sorted()
            .zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b).toInt() }
            .filter { it in KORTSTE..LANGSTE }
            .takeLast(MEE_TELLEN)

    fun stand(bloedingen: List<Bloeding>, vandaag: LocalDate, beginLengte: Int = BEGIN_LENGTE): CyclusStand {
        val eerder = bloedingen.filter { !it.begin.isAfter(vandaag) }.sortedBy { it.begin }
        val lengtes = lengtes(eerder)
        val lengte = if (lengtes.isEmpty()) beginLengte else lengtes.average().roundToInt()
        val duren = eerder.mapNotNull { b -> b.eind?.let { ChronoUnit.DAYS.between(b.begin, it).toInt() + 1 } }
            .filter { it in 1..LANGSTE_DUUR }
            .takeLast(MEE_TELLEN)
        val duur = if (duren.isEmpty()) BEGIN_DUUR else duren.average().roundToInt()

        val laatste = eerder.lastOrNull()
            ?: return CyclusStand(null, false, lengte, duur, true, null, null, null, null)
        val dag = ChronoUnit.DAYS.between(laatste.begin, vandaag).toInt() + 1
        val ongesteld = laatste.eind?.let { !vandaag.isAfter(it) } ?: (dag <= LANGSTE_DUUR)
        val volgende = laatste.begin.plusDays(lengte.toLong())
        val venster = lengtes.size >= 3
        return CyclusStand(
            dag = dag,
            ongesteld = ongesteld,
            lengte = lengte,
            duur = duur,
            nogAanHetLeren = lengtes.isEmpty(),
            volgende = volgende,
            vroegst = if (venster) laatste.begin.plusDays(lengtes.min().toLong()) else null,
            laatst = if (venster) laatste.begin.plusDays(lengtes.max().toLong()) else null,
            dagenTot = ChronoUnit.DAYS.between(vandaag, volgende),
        )
    }

    /** Dag voor het seintje, of `null` als die al voorbij is of ze al ongesteld is. */
    fun seintjeDag(stand: CyclusStand, vandaag: LocalDate): LocalDate? {
        val volgende = stand.volgende ?: return null
        if (stand.ongesteld) return null
        val dag = volgende.minusDays(SEINTJE_DAGEN)
        return if (dag.isBefore(vandaag)) null else dag
    }
}
