package nl.odelia.dashboard.logica

import java.time.DayOfWeek
import java.time.LocalDate

private val korteDagen = listOf("ma", "di", "wo", "do", "vr", "za", "zo")

/** "ma 26" */
fun korteDatum(datum: LocalDate): String = "${korteDagen[datum.dayOfWeek.value - 1]} ${datum.dayOfMonth}"

/** Wat er open staat, verdeeld over te laat, vandaag en de rest van deze week. */
data class Overzicht(
    val teLaat: List<Onthoud>,
    val vandaag: List<Onthoud>,
    val dezeWeek: List<Onthoud>,
) {
    val isLeeg get() = teLaat.isEmpty() && vandaag.isEmpty() && dezeWeek.isEmpty()

    companion object {
        fun van(items: List<Onthoud>, vandaag: LocalDate): Overzicht {
            val open = items.filter { !it.klaar && it.wanneer != null }.sortedBy { it.wanneer }
            val zondag = vandaag.plusDays((DayOfWeek.SUNDAY.value - vandaag.dayOfWeek.value).toLong())
            return Overzicht(
                teLaat = open.filter { it.wanneer!!.isBefore(vandaag) },
                vandaag = open.filter { it.wanneer == vandaag },
                dezeWeek = open.filter { it.wanneer!!.isAfter(vandaag) && !it.wanneer.isAfter(zondag) },
            )
        }
    }
}

/** Tekst voor de ochtendmelding: wat staat er vandaag en deze week. */
fun ochtendTekst(items: List<Onthoud>, vandaag: LocalDate): String {
    val o = Overzicht.van(items, vandaag)
    if (o.isLeeg) return "Er staat vandaag en deze week niks bijzonders. Fijne dag!"
    return buildList {
        if (o.vandaag.isNotEmpty()) add("Vandaag: " + o.vandaag.joinToString(", ") { it.titel })
        if (o.dezeWeek.isNotEmpty()) add("Deze week: " + o.dezeWeek.joinToString(", ") { "${it.titel} (${korteDatum(it.wanneer!!)})" })
        if (o.teLaat.isNotEmpty()) add("Nog open: " + o.teLaat.joinToString(", ") { it.titel })
    }.joinToString("\n")
}
