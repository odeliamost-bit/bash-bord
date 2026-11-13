package nl.odelia.dashboard.ui

/** Alle plekken in het menu, in de volgorde van ontwerp/Navigatie.dc.html. */
enum class Plek(val label: String) {
    Thuis("Thuis"),
    Werk("Werk & uren"),
    Onthoud("Onthoud"),
    Mensen("Mensen"),
    Mail("Mail-hulp"),
    Advies("Advies"),
    Vertalen("Vertalen"),
    Leren("Leren"),
    Cyclus("Cyclus"),
    Geld("Geld"),
    Beleggen("Beleggen"),
    Wensen("Wensen"),
    Zeilweer("Zeilweer"),
    Recepten("Recepten"),
    Instellingen("Instellingen"),
    ;

    companion object {
        /** Deze kun je niet verbergen in het menu. */
        val altijd = setOf(Thuis, Instellingen)
    }
}
