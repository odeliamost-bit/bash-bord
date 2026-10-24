package nl.odelia.dashboard.ui.thuis

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.roundToInt

private val dagen = listOf("maandag", "dinsdag", "woensdag", "donderdag", "vrijdag", "zaterdag", "zondag")
private val maanden = listOf("jan", "feb", "mrt", "apr", "mei", "jun", "jul", "aug", "sep", "okt", "nov", "dec")

/** "zaterdag 24 okt" */
fun datumTekst(nu: LocalDateTime): String = datumTekst(nu.toLocalDate())

fun datumTekst(datum: LocalDate): String =
    "${dagNaam(datum.dayOfWeek)} ${datum.dayOfMonth} ${maanden[datum.monthValue - 1]}"

/** "za 24 okt" */
fun datumKort(datum: LocalDate): String =
    "${dagKort(datum.dayOfWeek)} ${datum.dayOfMonth} ${maanden[datum.monthValue - 1]}"

/** "zaterdag" */
fun dagNaam(dag: DayOfWeek): String = dagen[dag.value - 1]

/** "za" */
fun dagKort(dag: DayOfWeek): String = dagen[dag.value - 1].take(2)

/** "7:45" */
fun tijdTekst(tijd: LocalTime): String = "${tijd.hour}:${tijd.minute.toString().padStart(2, '0')}"

/** "Goedemorgen", "Goedemiddag", "Goedenavond" of "Goedenacht". */
fun groet(nu: LocalDateTime): String = when (nu.hour) {
    in 6..11 -> "Goedemorgen"
    in 12..17 -> "Goedemiddag"
    in 18..23 -> "Goedenavond"
    else -> "Goedenacht"
}

/** Klein zinnetje onder de groet. */
fun onderregel(nu: LocalDateTime): String = when {
    nu.dayOfWeek == DayOfWeek.SATURDAY || nu.dayOfWeek == DayOfWeek.SUNDAY -> "Fijn weekend"
    nu.hour >= 17 -> "Fijne avond"
    else -> "Fijne werkdag vandaag"
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
