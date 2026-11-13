package nl.odelia.dashboard.logica

/**
 * Schema lezen (nov 2026): een oefen-lus van een afsluiter, van Veld → Junction box → MCC.
 * Alle namen en nummers zijn verzonnen. Echte werk-tekeningen komen nooit in deze (openbare) code.
 */
object LoopSchema {
    enum class Storing(val label: String) {
        Geen("Geen storing"),
        Draadbreuk("Draad los in de junction box"),
        GeenLucht("Geen perslucht"),
    }

    data class Staat(val sturen: Boolean = false, val storing: Storing = Storing.Geen)

    /** Wat er op elke plek gebeurt. `true` = er loopt stroom / het contact is dicht. */
    data class Stand(
        val ventiel: Boolean,
        val klepOpen: Boolean,
        val schakelaarOpen: Boolean,
        val schakelaarDicht: Boolean,
        val ingangOpen: Boolean,
        val ingangDicht: Boolean,
        val storing: Boolean,
        val melding: String,
    )

    fun reken(s: Staat): Stand {
        val ventiel = s.sturen
        val klepOpen = ventiel && s.storing != Storing.GeenLucht
        val ingangOpen = klepOpen && s.storing != Storing.Draadbreuk
        val ingangDicht = !klepOpen
        val (fout, tekst) = when {
            ingangOpen -> false to "✅ De besturing ziet: afsluiter OPEN"
            ingangDicht && !s.sturen -> false to "✅ De besturing ziet: afsluiter DICHT"
            ingangDicht -> true to "⚠️ Storing: open gestuurd, maar de melding blijft DICHT"
            else -> true to "⚠️ Storing: geen melding OPEN en geen melding DICHT"
        }
        return Stand(ventiel, klepOpen, klepOpen, !klepOpen, ingangOpen, ingangDicht, fout, tekst)
    }

    val afsluiter = Onderdeel("xv", "Afsluiter XV-101", "Klep onder de tank. Gaat open met perslucht.",
        "Krijgt hij lucht, dan gaat hij open. Valt de lucht weg, dan duwt een veer hem dicht. Dat is veilig: bij een storing gaat hij vanzelf dicht.")
    val ventiel = Onderdeel("yv", "Magneetventiel YV-101", "Elektrisch kraantje voor de perslucht.",
        "Krijgt de spoel 24 V, dan laat hij lucht door naar de afsluiter. Zonder spanning blaast hij de lucht af.")
    val zso = Onderdeel("zso", "Eindschakelaar ZSO-101", "Contact dat dicht gaat als de afsluiter helemaal OPEN is.",
        "ZS = standschakelaar, O = open. Zo weet de besturing zeker dat de klep echt open staat.")
    val zsc = Onderdeel("zsc", "Eindschakelaar ZSC-101", "Contact dat dicht is als de afsluiter helemaal DICHT is.",
        "ZS = standschakelaar, C = closed (dicht).")
    val jb = Onderdeel("jb", "Junction box JB-10", "Kastje in het veld met klemmen.",
        "Hier komen de korte kabels van de apparaten samen op één dikke kabel naar het MCC. Elke draad heeft een klemnummer. Bij een storing meet je hier vaak als eerste.")
    val versterker = Onderdeel("k1", "Scheidingsversterker K1", "Kastje tussen het veld en de besturing.",
        "Hij geeft het contact uit het veld door, maar houdt de stroom in het veld heel klein. Dat is nodig in een Ex-zone, waar een vonk gevaarlijk is.")
    val ingang = Onderdeel("di", "Ingang (DI)", "Hier 'ziet' de besturing een signaal.",
        "DI = digitale ingang. 1 = er komt 24 V binnen, 0 = niks. I3.0 is het adres: kaart 3, kanaal 0.")
    val uitgang = Onderdeel("do", "Uitgang (DO) Q5.0", "Hier stuurt de besturing iets aan.",
        "DO = digitale uitgang. Zet de besturing hem aan, dan gaat er 24 V naar het magneetventiel.")
    val onderdelen = listOf(afsluiter, ventiel, zso, zsc, jb, versterker, ingang, uitgang)

    data class Vraag(val vraag: String, val antwoorden: List<String>, val goed: Int, val uitleg: String)

    val vragen = listOf(
        Vraag("Welke kant loopt het signaal van een eindschakelaar op?", listOf("Veld → MCC", "MCC → veld"), 0,
            "Een melding komt uit het veld en gaat naar een ingang van de besturing."),
        Vraag("De afsluiter is open gestuurd. Er komt geen melding OPEN en geen melding DICHT. Wat is het meest logisch?",
            listOf("Geen perslucht", "Draad los van de open-melding", "Ventiel kapot"), 1,
            "De klep is wél van zijn dicht-stand af (melding DICHT is weg). Alleen de open-melding komt niet aan: zoek in die draad."),
        Vraag("Waarom zit er een scheidingsversterker tussen?", listOf("Om het signaal sneller te maken", "Om de stroom in de Ex-zone klein te houden"), 1,
            "In een Ex-zone mag geen vonk ontstaan. De versterker begrenst de stroom en spanning naar het veld."),
    )
}
