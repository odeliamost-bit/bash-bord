package nl.odelia.dashboard.logica

import java.time.LocalDate

/**
 * Een to-do-lijstje (bijv. tekst uit een foto via Google Lens, gedeeld met Bash-bord) → losse Onthoud-dingen.
 * Bouwnacht 1, nov 2026. Tekst lezen gebeurt in Google Lens, niet in de app (geen extra onderdelen die naar Google sturen).
 */
object Lijstje {
    private val bolletje = Regex("""^\s*(?:[-–—•*·▪●○◦☐☑✓✔✗□■]|\[\s?[xX]?\s?]|\d{1,2}[.)])\s*""")

    /** Losse regels zonder bolletjes, nummers of hokjes. Lege en hele korte regels vallen weg, dubbele ook. */
    fun regels(tekst: String): List<String> = tekst.lines()
        .map { it.replace(bolletje, "").trim() }
        .filter { r -> r.count { it.isLetterOrDigit() } >= 3 }
        .distinctBy { it.lowercase() }
        .take(30)

    /** Is het een lijstje (2 regels of meer)? */
    fun isLijstje(tekst: String) = regels(tekst).size >= 2

    fun naarOnthoud(regel: String, vandaag: LocalDate, mensen: Collection<String>, groepen: Collection<String>) = Onthoud(
        tekst = regel,
        wie = Herken.uitLijst(regel, mensen),
        wat = Herken.soort(regel),
        wanneer = Herken.datum(regel, vandaag),
        groep = Herken.uitLijst(regel, groepen),
    )
}
