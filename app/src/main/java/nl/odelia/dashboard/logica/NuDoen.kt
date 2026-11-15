package nl.odelia.dashboard.logica

import java.time.LocalDate

/**
 * "Nu doen" (uit Kompas): het ene ding dat nu het belangrijkst is.
 * Eerst wat te laat is (oudste eerst), anders wat vandaag moet. Staat er niks, dan is er geen kaart.
 */
data class NuDoen(
    val item: Onthoud,
    val teLaat: Boolean,
    /** Hoeveel er daarna nog te laat of voor vandaag staan. */
    val daarna: Int,
) {
    /** "te laat (sinds ma 9)" of "vandaag". */
    val wanneerTekst: String get() = if (teLaat) "te laat (sinds ${korteDatum(item.wanneer!!)})" else "vandaag"

    val voorlees: String get() = "Nu doen: ${item.titel}. Dat is $wanneerTekst."

    companion object {
        fun van(o: Overzicht): NuDoen? {
            val item = o.teLaat.firstOrNull() ?: o.vandaag.firstOrNull() ?: return null
            return NuDoen(item, teLaat = o.teLaat.isNotEmpty(), daarna = o.teLaat.size + o.vandaag.size - 1)
        }

        fun van(items: List<Onthoud>, vandaag: LocalDate): NuDoen? = van(Overzicht.van(items, vandaag))
    }
}
