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

    /** Twee soorten (gekozen door Odelia): gewone tekst of een appje (chatbericht). */
    enum class Soort(val label: String, val hint: String, val stijl: String) {
        Tekst("📄 Gewone tekst", "Typ of plak je tekst", ""),
        Appje("💬 Appje", "Typ of plak je appje", "Het is een appje (chatbericht): houd het kort en in spreektaal, met dezelfde toon. Laat emoji staan."),
    }

    /** De hele vraag voor Claude, of `null` als er geen tekst is. */
    fun vraag(tekst: String, taal: Taal, uitleg: Boolean = false, soort: Soort = Soort.Tekst): String? {
        val schoon = tekst.trim().take(MAX)
        if (schoon.isEmpty()) return null
        val slot = if (uitleg) "Geef eerst de vertaling. Leg daarna kort en simpel uit wat lastige woorden betekenen."
        else "Geef alleen de vertaling, zonder uitleg."
        return listOf(taal.opdracht, soort.stijl, slot).filter { it.isNotEmpty() }.joinToString(" ") + "\n\n$schoon"
    }
}
