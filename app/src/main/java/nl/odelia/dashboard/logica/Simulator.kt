package nl.odelia.dashboard.logica

/**
 * Simulator voor Leren (okt 2026): schema's met uitleg per onderdeel, formules en aderkleuren.
 * Teksten uit de Kompas Elektrotechniek-pagina op claude.ai. Werkt zonder internet.
 */

data class Onderdeel(val id: String, val naam: String, val kort: String, val uitleg: String)

data class Schema(val id: String, val naam: String, val intro: String, val onderdelen: List<Onderdeel>) {
    fun deel(id: String): Onderdeel = onderdelen.first { it.id == id }
}

data class Formule(val titel: String, val regels: List<String>, val uitleg: String)

data class Ader(val kleur: String, val naam: String, val uitleg: String)

object Simulator {
    val Kast = Schema(
        "kast", "Groepenkast",
        "De groepenkast van een woning, met de meter erboven. De draden tussen de modules zijn vereenvoudigd.",
        listOf(
            Onderdeel("aansluitkabel", "Aansluitkabel", "De kabel van het net van de netbeheerder naar je huis.", "Alles tot en met de kWh-meter is van de netbeheerder. Daar mag je zelf niet aan werken."),
            Onderdeel("hoofdzekering", "Hoofdzekering", "Beveiligt de aansluiting van het hele huis.", "Zit vóór de meter en hoort bij de netbeheerder. Een woning heeft bijvoorbeeld 1 × 35 A of 3 × 25 A."),
            Onderdeel("kwh", "kWh-meter", "Meet hoeveel elektrische energie je gebruikt, in kWh.", "Vaak is het een slimme meter die de standen zelf doorgeeft. Hij hoort bij de netbeheerder."),
            Onderdeel("kamrail", "Kamrail", "Koperen strip die de automaten van één aardlekschakelaar verbindt.", "Zo hoef je niet elke automaat apart te bedraden. Elke aardlekschakelaar heeft zijn eigen kamrail."),
            Onderdeel("dinrail", "DIN-rail", "Metalen rail van 35 mm breed waar de modules op klikken.", "Ook wel montagerail genoemd. De hoofdschakelaar, aardlekschakelaars en automaten zitten er allemaal op."),
            Onderdeel("hoofdschakelaar", "Hoofdschakelaar", "Schakelt de hele groepenkast in één keer uit.", "Daarmee maak je de installatie spanningsloos. Let op: de aansluiting vóór de schakelaar blijft onder spanning."),
            Onderdeel("als", "Aardlekschakelaar", "Schakelt uit als er stroom weglekt, bijvoorbeeld via een persoon.", "Hij vergelijkt de stroom door de fase en de nul. Bij een verschil van ongeveer 30 mA schakelt hij af. Test hem een paar keer per jaar met de testknop."),
            Onderdeel("automaat", "Installatieautomaat", "Beveiligt één groep tegen overbelasting en kortsluiting.", "Op de schakelaar staat bijvoorbeeld B16: B-karakteristiek en 16 A. Bij te veel stroom schakelt hij af."),
            Onderdeel("groepen", "Eindgroepen", "Hier gaan de draden naar de stopcontacten en lampen.", "Elke automaat voedt één groep, bijvoorbeeld de keuken of de woonkamer."),
            Onderdeel("nulrail", "Nulrail (N)", "Verzamelpunt voor de blauwe nuldraden.", "De nuldraad van een groep moet op de nulrail van zijn eigen aardlekschakelaar. Anders schakelt de aardlekschakelaar af."),
            Onderdeel("aardrail", "Aardrail (PE)", "Verzamelpunt voor de geel-groene aarddraden.", "De aardrail is verbonden met de aarde. Zo krijgen metalen delen bij een fout geen gevaarlijke spanning."),
        ),
    )

    val Kring = Schema(
        "kring", "Stroomkring",
        "Een stroomkring met een ampèremeter en een voltmeter. Let op hoe de meters zijn aangesloten.",
        listOf(
            Onderdeel("bron", "Spanningsbron", "Levert de spanning, hier 12 V gelijkspanning.", "In het symbool is de lange streep de plus (+) en de korte, dikke streep de min (−)."),
            Onderdeel("schakelaar", "Schakelaar S1", "Maakt of verbreekt de stroomkring.", "Open: er loopt geen stroom. Dicht: de stroom kan rondlopen."),
            Onderdeel("zekering", "Zekering F1", "Smelt door als de stroom te groot wordt.", "Zo beschermt hij de draden en onderdelen tegen kortsluiting en overbelasting."),
            Onderdeel("stroom", "Stroomrichting I", "De pijl laat zien hoe de stroom loopt.", "Afgesproken is: buiten de bron loopt de stroom van plus (+) naar min (−)."),
            Onderdeel("amp", "Ampèremeter", "Meet de stroom en staat in serie.", "Alle stroom moet door de meter. Daarom staat hij ín de kring, niet ernaast."),
            Onderdeel("weerstand", "Weerstand R1", "Beperkt de stroom.", "Met de wet van Ohm reken je: U = I × R."),
            Onderdeel("volt", "Voltmeter", "Meet de spanning over de weerstand en staat parallel.", "Hij hangt over het onderdeel heen, met een draad aan elke kant."),
            Onderdeel("lamp", "Lamp H1", "Zet elektrische energie om in licht.", "Het symbool is een cirkel met een kruis erin."),
        ),
    )

    val Motor = Schema(
        "motor", "Motor: hoofdstroom",
        "Het hoofdstroomschema van een motor die direct wordt ingeschakeld (direct-aan-net).",
        listOf(
            Onderdeel("net", "Netvoeding", "Het driefasennet: L1, L2 en L3.", "Tussen twee fasen staat 400 V. L1 is bruin, L2 zwart en L3 grijs."),
            Onderdeel("q1", "Hoofdschakelaar Q1", "Schakelt de hele motorgroep spanningsloos.", "Vaak kun je hem op slot doen met een hangslot. Dan kan niemand hem aanzetten terwijl jij werkt."),
            Onderdeel("f1", "Zekeringen F1", "Beveiligen tegen kortsluiting.", "Vaak zit hier een motorbeveiligingsschakelaar. Die beschermt tegen kortsluiting én overbelasting."),
            Onderdeel("k1", "Magneetschakelaar K1", "Schakelt de motor aan en uit.", "De spoel van K1 zit in het stuurstroomschema. Krijgt de spoel spanning, dan sluiten deze drie hoofdcontacten."),
            Onderdeel("f2", "Thermisch relais F2", "Beveiligt de motor tegen overbelasting.", "Is de stroom te lang te hoog, dan opent zijn contact 95-96 in de stuurstroom. Dan valt K1 af en stopt de motor."),
            Onderdeel("m1", "Motor M1", "Een driefasen-asynchrone motor.", "Verwissel je twee fasen, dan draait de motor de andere kant op."),
            Onderdeel("pe", "Aarde (PE)", "Beschermingsleiding naar het motorhuis.", "Raakt een fasedraad het metalen huis, dan loopt de stroom via PE weg en schakelt de beveiliging af."),
        ),
    )

    val Stuur = Schema(
        "stuur", "Start/stop: stuurstroom",
        "Het stuurstroomschema van dezelfde motor, met zelfhouding. Je leest het van boven naar beneden.",
        listOf(
            Onderdeel("L", "Fase (L)", "De bovenste lijn van het stuurstroomschema.", "Stuurstroom is vaak 230 V of 24 V. Je leest het schema van boven naar beneden."),
            Onderdeel("N", "Nul (N)", "De onderste lijn: terug naar de bron.", "Elke stroombaan loopt van L via de contacten en de spoel of lamp naar N."),
            Onderdeel("f2c", "Contact F2 (95-96)", "Verbreekcontact van het thermisch relais.", "Normaal is het gesloten. Bij overbelasting gaat het open, valt K1 af en stopt de motor."),
            Onderdeel("s1", "Stopknop S1", "Drukknop met een verbreekcontact (NC).", "Normaal gesloten. Indrukken onderbreekt de stroom naar de spoel. De stopknop staat in serie, zodat stoppen altijd werkt."),
            Onderdeel("s2", "Startknop S2", "Drukknop met een maakcontact (NO).", "Normaal open. Indrukken geeft de spoel van K1 spanning."),
            Onderdeel("k1z", "Zelfhouding K1 (13-14)", "Hulpcontact van K1, parallel aan de startknop.", "Trekt K1 aan, dan sluit dit contact. Daardoor houdt de spoel spanning als je de startknop loslaat."),
            Onderdeel("spoel", "Spoel K1 (A1-A2)", "De spoel van de magneetschakelaar.", "Krijgt de spoel spanning, dan trekt K1 aan. Alle maakcontacten van K1 sluiten, ook de hoofdcontacten naar de motor."),
            Onderdeel("k1s", "Hulpcontact K1 (23-24)", "Tweede maakcontact van K1.", "Het sluit als K1 aantrekt. Daardoor gaat lamp H1 branden."),
            Onderdeel("h1", "Signaallamp H1", "Laat zien dat de motor draait.", "Hij brandt zolang K1 aangetrokken is."),
        ),
    )

    val formules = listOf(
        Formule("Wet van Ohm", listOf("U = I × R", "I = U / R", "R = U / I"), "U = spanning in volt (V), I = stroom in ampère (A), R = weerstand in ohm (Ω)."),
        Formule("Vermogen", listOf("P = U × I", "P = I² × R", "P = U² / R"), "P = vermogen in watt (W)."),
        Formule("Energie", listOf("E = P × t", "kosten = E × prijs per kWh"), "E is in kWh als P in kW staat en t in uren."),
        Formule("Serie", listOf("Rt = R1 + R2 + …", "Ut = U1 + U2 + …"), "In serie is de stroom overal gelijk."),
        Formule("Parallel", listOf("1/Rt = 1/R1 + 1/R2 + …", "Rt = (R1 × R2) / (R1 + R2)", "It = I1 + I2 + …"), "Parallel is de spanning overal gelijk. De middelste formule is voor twee weerstanden."),
        Formule("Spanningsdeler", listOf("U1 = U × R1 / (R1 + R2)"), "Twee weerstanden in serie verdelen de spanning naar verhouding."),
        Formule("Wisselspanning", listOf("Û = U × √2", "f = 1 / T", "230 V × √3 ≈ 400 V"), "Û = topwaarde, U = effectieve waarde, f = frequentie in hertz (Hz), T = tijd van één periode in seconden."),
        Formule("Transformator", listOf("U1 / U2 = N1 / N2"), "N = aantal windingen. 1 is de primaire kant, 2 de secundaire kant."),
        Formule("Rendement", listOf("η = P nuttig / P toegevoerd × 100%"), "η (eta) is altijd kleiner dan 100%."),
        Formule("Voorvoegsels", listOf("k = × 1000", "M = × 1 000 000", "m = ÷ 1000", "µ = ÷ 1 000 000"), "Bijvoorbeeld: 4,7 kΩ = 4700 Ω en 250 mA = 0,25 A."),
    )

    /** Kleuren van de aders (NEN 1010). */
    val aders = listOf(
        Ader("bruin", "L1 – fase", "Hier staat spanning op. In huis meestal de enige fase."),
        Ader("zwart", "L2 – fase", "Tweede fase bij driefasen (400 V tussen twee fasen)."),
        Ader("grijs", "L3 – fase", "Derde fase bij driefasen."),
        Ader("blauw", "N – nul", "Terug naar de bron. Gebruik blauw nooit als fase."),
        Ader("geelgroen", "PE – aarde", "Beschermingsleiding. Alleen voor aarde, nooit voor iets anders."),
    )
}

/** Groepenkast: hoofdschakelaar, 2 aardlekschakelaars, elk met 3 automaten. */
data class KastStaat(
    val hoofd: Boolean = true,
    val aardlek: List<Boolean> = listOf(true, true),
    val automaten: List<List<Boolean>> = listOf(listOf(true, true, true), listOf(true, true, true)),
) {
    fun heeftSpanning(al: Int, groep: Int): Boolean = hoofd && aardlek[al] && automaten[al][groep]
    fun wisselHoofd() = copy(hoofd = !hoofd)
    fun wisselAardlek(al: Int) = copy(aardlek = aardlek.mapIndexed { i, a -> if (i == al) !a else a })
    fun wisselAutomaat(al: Int, groep: Int) = copy(
        automaten = automaten.mapIndexed { i, r -> if (i == al) r.mapIndexed { j, g -> if (j == groep) !g else g } else r },
    )

    companion object {
        val groepen = listOf(listOf("Keuken", "Kamer", "Was"), listOf("Slaapkamer", "Badkamer", "Buiten"))
    }
}

/** Stroomkring: 12 V, R1 = 4 Ω, lamp 20 Ω. */
data class KringStaat(val dicht: Boolean = false, val zekeringHeel: Boolean = true) {
    val stroomt: Boolean get() = dicht && zekeringHeel
    /** Stroom in ampère: I = U / R. */
    val stroom: Double get() = if (stroomt) BRON / (R1 + LAMP) else 0.0
    /** Spanning over R1 in volt: U = I × R. */
    val spanningR1: Double get() = stroom * R1

    companion object {
        const val BRON = 12.0
        const val R1 = 4.0
        const val LAMP = 20.0
    }
}

/** Motor met start/stop en zelfhouding. */
data class MotorStaat(val q1: Boolean = true, val k1: Boolean = false, val f2Uit: Boolean = false) {
    val draait: Boolean get() = q1 && k1
    fun start() = if (q1 && !f2Uit) copy(k1 = true) else this
    fun stop() = copy(k1 = false)
    fun overbelasting() = copy(f2Uit = true, k1 = false)
    fun herstel() = copy(f2Uit = false)
    fun wisselQ1() = if (q1) copy(q1 = false, k1 = false) else copy(q1 = true)
}
