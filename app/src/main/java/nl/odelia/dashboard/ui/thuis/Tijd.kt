package nl.odelia.dashboard.ui.thuis

import java.time.DayOfWeek
import java.time.LocalDateTime
import kotlin.math.roundToInt

private val dagen = listOf("maandag", "dinsdag", "woensdag", "donderdag", "vrijdag", "zaterdag", "zondag")
private val maanden = listOf("jan", "feb", "mrt", "apr", "mei", "jun", "jul", "aug", "sep", "okt", "nov", "dec")

/** "zaterdag 24 okt" */
fun datumTekst(nu: LocalDateTime): String =
    "${dagen[nu.dayOfWeek.value - 1]} ${nu.dayOfMonth} ${maanden[nu.monthValue - 1]}"

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
