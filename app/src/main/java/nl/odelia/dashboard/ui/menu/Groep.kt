package nl.odelia.dashboard.ui.menu

import nl.odelia.dashboard.ui.Plek

/**
 * De 7 knoppen in de zijbalk (gekozen door Odelia, okt 2026).
 * Een groep met meer plekken opent een tegelscherm; met één plek ga je er meteen heen.
 */
enum class Groep(val label: String, val icoon: Plek, val plekken: List<Plek>) {
    Thuis("Thuis", Plek.Thuis, listOf(Plek.Thuis)),
    Werk("Werk", Plek.Werk, listOf(Plek.Werk, Plek.Mail)),
    Onthoud("Onthoud", Plek.Onthoud, listOf(Plek.Onthoud, Plek.Mensen)),
    Leren("Leren", Plek.Leren, listOf(Plek.Leren)),
    Geld("Geld", Plek.Geld, listOf(Plek.Geld, Plek.Wensen)),
    VoorMij("Voor mij", Plek.Wensen, listOf(Plek.Cyclus, Plek.Recepten, Plek.Zeilweer, Plek.Advies)),
    Instellingen("Instellingen", Plek.Instellingen, listOf(Plek.Instellingen)),
    ;

    /** Plekken die niet verborgen zijn in Instellingen. */
    fun zichtbaar(verborgen: Set<String>) = plekken.filter { it.name !in verborgen || it in Plek.altijd }

    companion object {
        fun van(plek: Plek): Groep = entries.first { plek in it.plekken }
    }
}

/** Korte uitleg onder elke tegel in een groep. */
fun Plek.onderschrift(): String = when (this) {
    Plek.Thuis -> "dashboard"
    Plek.Werk -> "uren, in- en uitklokken"
    Plek.Mail -> "mail schrijven"
    Plek.Onthoud -> "lijstjes en afspraken"
    Plek.Mensen -> "wie is wie"
    Plek.Leren -> "kaarten, quiz, school"
    Plek.Geld -> "je potjes"
    Plek.Wensen -> "wensenlijst"
    Plek.Cyclus -> "alleen op je telefoon"
    Plek.Recepten -> "koken"
    Plek.Zeilweer -> "wind en weer"
    Plek.Advies -> "maken, repareren, vragen"
    Plek.Instellingen -> "instellingen"
}
