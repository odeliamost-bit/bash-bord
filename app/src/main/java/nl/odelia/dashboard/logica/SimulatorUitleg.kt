package nl.odelia.dashboard.logica

/**
 * Uitleg-knop in de Simulator (nov 2026): per tabje in een paar korte stappen hoe het werkt.
 * De sleutel is de naam van het tabje (`SimDeel.name`).
 */
object SimulatorUitleg {
    private val stappen: Map<String, List<String>> = mapOf(
        "Tekenen" to listOf(
            "Sleep een onderdeel uit het rijtje naar het tekenvlak.",
            "Kies Draad en tik twee aansluitpunten aan. Dan komt er een draad tussen.",
            "Kies Bedien en tik op een schakelaar om hem om te zetten.",
            "Kies Meten en tik op een onderdeel. Je ziet spanning en stroom.",
            "Kies Gum en tik op iets om het weg te halen. Met het pijltje maak je een stap ongedaan.",
        ),
        "Loop" to listOf(
            "Lees van links naar rechts: Veld, Junction box, MCC.",
            "Groen betekent: hier loopt stroom.",
            "Tik op Open sturen of Dicht en kijk wat er verandert.",
            "Zet een storing erin en zoek waar het groen ophoudt.",
            "Tik op een blokje voor uitleg. Onderaan staan opdrachtjes.",
        ),
        "Kast" to listOf(
            "Tik op een schakelaar om hem om te zetten.",
            "Kijk welke lampjes uitgaan.",
            "Hoofd uit: alles is uit. Aardlek uit: alleen die drie groepen zijn uit.",
            "Tik onderaan op een onderdeel voor uitleg.",
        ),
        "Kring" to listOf(
            "Tik op S1 om de schakelaar dicht te doen. De lamp gaat aan.",
            "Kijk naar de meters: A is stroom, V is spanning.",
            "Tik op Maak kortsluiting. De zekering gaat kapot en de lamp gaat uit.",
            "Tik op Vervang zekering om verder te gaan.",
        ),
        "Motor" to listOf(
            "Tik op Start S2. De motor gaat draaien.",
            "De motor blijft draaien, ook als je loslaat. Dat heet zelfhouding.",
            "Tik op Stop S1 om de motor te stoppen.",
            "Tik op F2 of Q1 en kijk wat er dan gebeurt.",
        ),
        "Aders" to listOf(
            "Hier staan de kleuren van de aders op een rij.",
            "Bij elke kleur staat waar de ader voor is.",
            "Scrol naar beneden om ze allemaal te zien.",
        ),
    )

    /** De stappen voor dit tabje. Onbekend tabje: een lege lijst (dan is er geen uitleg-knop). */
    fun voor(deel: String): List<String> = stappen[deel].orEmpty()

    /** Alles achter elkaar, om voor te lezen. */
    fun voorlees(deel: String): String = voor(deel).mapIndexed { i, s -> "Stap ${i + 1}. $s" }.joinToString(" ")
}
