package nl.odelia.dashboard.logica

/**
 * Vertalen (nov 2026): de app vertaalt niet zelf. Ze zet een vraag klaar voor de Claude-app;
 * Odelia drukt daar zelf op versturen. De app gaat hiervoor dus niet online.
 */
object Vertalen {
    /** Niet te lang: de tekst kan uit een andere app komen. */
    const val MAX = 5_000

    /** Waar de tekst heen moet. [opdracht] is de zin die Claude krijgt. */
    enum class Taal(val label: String, val opdracht: String) {
        Auto("NL ↔ EN", "Vertaal deze tekst. Is hij Nederlands, vertaal dan naar het Engels. Anders naar het Nederlands."),
        Engels("Engels", "Vertaal deze tekst naar het Engels."),
        Nederlands("Nederlands", "Vertaal deze tekst naar het Nederlands."),
        Duits("Duits", "Vertaal deze tekst naar het Duits."),
        Frans("Frans", "Vertaal deze tekst naar het Frans."),
        Spaans("Spaans", "Vertaal deze tekst naar het Spaans."),
    }

    /** De hele vraag voor Claude, of `null` als er geen tekst is. */
    fun vraag(tekst: String, taal: Taal, uitleg: Boolean = false): String? {
        val schoon = tekst.trim().take(MAX)
        if (schoon.isEmpty()) return null
        val slot = if (uitleg) "Geef eerst de vertaling. Leg daarna kort en simpel uit wat lastige woorden betekenen."
        else "Geef alleen de vertaling, zonder uitleg."
        return "${taal.opdracht} $slot\n\n$schoon"
    }
}
