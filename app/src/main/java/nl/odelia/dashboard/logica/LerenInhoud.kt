package nl.odelia.dashboard.logica

/**
 * De kaarten en quizvragen die al in de app zitten.
 * Elektrotechniek komt uit de Leerplek-pagina (claude.ai). Je kunt in de app zelf kaarten toevoegen.
 * Let op: verander je een id, dan begint die kaart weer in bakje 1.
 */
object LerenInhoud {
    val vakken = listOf(
        Vak("elektro", "Elektrotechniek", "⚡"),
        Vak("burgerschap", "Burgerschap", "🏛️"),
        Vak("nederlands", "Nederlands", "📝"),
        Vak("keuzedeel", "Keuzedeel", "🎯"),
    )

    val onderwerpen = listOf(
        Onderwerp("relais", "elektro", "Relais en besturing"),
        Onderwerp("ohm", "elektro", "Wet van Ohm"),
        Onderwerp("politiek", "burgerschap", "Politiek en rechtsstaat"),
        Onderwerp("werkgeld", "burgerschap", "Werk en geld"),
        Onderwerp("spelling", "nederlands", "Werkwoorden spellen"),
        Onderwerp("teksten", "nederlands", "Teksten lezen en schrijven"),
        Onderwerp("project", "keuzedeel", "Project en bewijsstuk"),
    )

    val kaarten = listOf(
        // ⚡ Relais en besturing
        Kaart("r1", "relais", "Wat is een zelfhoudschakeling?", "Een NO-hulpcontact van de magneetschakelaar staat parallel aan de startknop. Laat je de startknop los, dan blijft de spoel bekrachtigd via dat contact."),
        Kaart("r2", "relais", "Waarom is de stopknop een NC-contact?", "Bij een draadbreuk of losse draad valt de schakeling dan vanzelf uit. Dat is veilig. Indrukken onderbreekt de stroom naar de spoel."),
        Kaart("r3", "relais", "Wat betekent NO?", "Normally Open: maakcontact. In rust open, sluit als je drukt. Klemnummers eindigen op 3 en 4, bijvoorbeeld 13-14."),
        Kaart("r4", "relais", "Wat betekent NC?", "Normally Closed: verbreekcontact. In rust gesloten, opent als je drukt. Klemnummers eindigen op 1 en 2, bijvoorbeeld 21-22."),
        Kaart("r5", "relais", "Wat zijn de klemmen van de spoel van een magneetschakelaar?", "A1 en A2."),
        Kaart("r6", "relais", "Wat zijn de hoofdcontacten van een magneetschakelaar?", "1-2, 3-4 en 5-6. Bovenaan de voeding (L1, L2, L3), onderaan naar de motor (T1, T2, T3)."),
        Kaart("r7", "relais", "Verschil tussen stuurstroomkring en hoofdstroomkring?", "De stuurstroomkring bedient de spoel (knoppen, hulpcontacten). De hoofdstroomkring voedt de verbruiker, bijvoorbeeld de motor."),
        Kaart("r8", "relais", "Wat doet een thermisch overstroomrelais?", "Het beschermt de motor tegen overbelasting. Het NC-contact 95-96 staat in de stuurstroomkring en schakelt de spoel uit."),
        // ⚡ Wet van Ohm
        Kaart("o1", "ohm", "Wat is de wet van Ohm?", "U = I × R\nU = spanning in volt (V)\nI = stroom in ampère (A)\nR = weerstand in ohm (Ω)"),
        Kaart("o2", "ohm", "Hoe reken je vermogen uit?", "P = U × I\nP in watt (W). Ook: P = I² × R."),
        Kaart("o3", "ohm", "230 V over een weerstand van 46 Ω. Hoe groot is de stroom?", "I = U ÷ R = 230 ÷ 46 = 5 A"),
        Kaart("o4", "ohm", "Hoe schrijf je de wet van Ohm om naar I en naar R?", "I = U ÷ R\nR = U ÷ I"),
        // 🏛️ Politiek en rechtsstaat
        Kaart("b1", "politiek", "Wat zijn de drie machten van de trias politica?", "1. Wetgevende macht: maakt de wetten.\n2. Uitvoerende macht: voert de wetten uit.\n3. Rechterlijke macht: oordeelt bij ruzie of misdaad."),
        Kaart("b2", "politiek", "Waarom zijn de drie machten gescheiden?", "Zo heeft niemand alle macht. De machten houden elkaar in de gaten."),
        Kaart("b3", "politiek", "Hoeveel zetels heeft de Tweede Kamer?", "150 zetels."),
        Kaart("b4", "politiek", "Hoeveel zetels heeft de Eerste Kamer?", "75 zetels."),
        Kaart("b5", "politiek", "Vanaf welke leeftijd mag je stemmen voor de Tweede Kamer?", "Vanaf 18 jaar, als je de Nederlandse nationaliteit hebt."),
        Kaart("b6", "politiek", "Waar gaat artikel 1 van de Grondwet over?", "Iedereen in Nederland wordt in gelijke gevallen gelijk behandeld. Discriminatie is niet toegestaan."),
        // 🏛️ Werk en geld
        Kaart("b7", "werkgeld", "Wat is een cao?", "Collectieve arbeidsovereenkomst. Afspraken over bijvoorbeeld loon en werktijden, gemaakt door werkgevers en vakbonden, voor een hele groep werknemers."),
        Kaart("b8", "werkgeld", "Wat is het verschil tussen bruto en netto loon?", "Bruto = je loon vóór belasting en premies.\nNetto = wat je echt op je rekening krijgt."),
        Kaart("b9", "werkgeld", "Wat doet een vakbond?", "Een vakbond komt op voor werknemers. Hij praat met werkgevers over bijvoorbeeld loon en de cao."),
        // 📝 Werkwoorden spellen
        Kaart("n1", "spelling", "Twijfel tussen d, t of dt (nu)? Welke truc helpt?", "Zet er 'lopen' voor in de plaats.\nHij loopt → hij wordt.\nIk loop → ik word."),
        Kaart("n2", "spelling", "Ik (vinden) het leuk. Hoe schrijf je het?", "Ik vind. Bij 'ik' komt er geen t achter de stam."),
        Kaart("n3", "spelling", "(Worden) jij ook moe? Hoe schrijf je het?", "Word jij. Staat 'jij' achter het werkwoord, dan geen t."),
        Kaart("n4", "spelling", "Verleden tijd: -te of -de? Hoe weet je dat?", "Kijk naar de laatste letter van de stam. Zit die in 't kofschip (t, k, f, s, ch, p)? Dan -te. Anders -de.\nwerk → werkte, woon → woonde."),
        Kaart("n5", "spelling", "Het is gisteren (gebeuren). Gebeurd of gebeurt?", "Gebeurd. Het is een voltooid deelwoord. De stam 'gebeur' eindigt op een r. Die zit niet in 't kofschip, dus een d."),
        // 📝 Teksten lezen en schrijven
        Kaart("n6", "teksten", "Wat is het verschil tussen onderwerp en hoofdgedachte?", "Onderwerp = waar de tekst over gaat, in een paar woorden.\nHoofdgedachte = de belangrijkste boodschap, in één zin."),
        Kaart("n7", "teksten", "Welke tekstdoelen zijn er?", "Informeren, overtuigen, activeren (iemand iets laten doen) en amuseren."),
        Kaart("n8", "teksten", "Hoe begin je een formele mail als je de naam niet weet?", "Geachte heer, mevrouw,"),
        Kaart("n9", "teksten", "Wat zet je in een alinea?", "Eén onderwerp of gedachte per alinea. Vaak staat de kern in de eerste zin."),
        // 🎯 Project en bewijsstuk
        Kaart("k1", "project", "Wat is een bewijsstuk?", "Iets wat je maakt om te laten zien dat je iets kunt. Bijvoorbeeld een verslag, product, filmpje of presentatie. Het moet passen bij de eisen van je keuzedeel."),
        Kaart("k2", "project", "Wat doe je eerst bij een nieuw project?", "Lees de opdracht en de beoordeling goed. Snap je iets niet? Vraag het aan je docent."),
        Kaart("k3", "project", "Wat betekent SMART bij een doel?", "Specifiek, Meetbaar, Acceptabel, Realistisch, Tijdgebonden."),
        Kaart("k4", "project", "Wat is de STARR-methode?", "Zo vertel je over wat je deed:\nSituatie, Taak, Actie, Resultaat, Reflectie."),
        Kaart("k5", "project", "Wat zet je in een planning?", "Welke taken er zijn, wie wat doet, wanneer het klaar moet zijn, en wat je nodig hebt."),
    )

    val vragen: List<Vraag> = listOf(
        // ⚡ Relais en besturing
        Meerkeuze("q1", "relais", "Welk contact zet je parallel aan de startknop voor zelfhouding?",
            listOf("Het NO-hulpcontact 13-14 van de magneetschakelaar", "Het NC-contact van de stopknop", "De spoel A1-A2", "Het contact 95-96 van het overstroomrelais"), 0,
            "Het NO-hulpcontact sluit zodra de spoel aantrekt. Zo blijft de stroom lopen als je de startknop loslaat."),
        Meerkeuze("q2", "relais", "Hoe sluit je de stopknop aan in de stuurstroomkring?",
            listOf("NO, parallel aan de startknop", "NC, in serie met de rest", "NO, in serie met de spoel", "NC, parallel aan de spoel"), 1,
            "De stopknop is NC en staat in serie. Indrukken of een draadbreuk onderbreekt dan de stroom naar de spoel."),
        Meerkeuze("q3", "relais", "Je laat de startknop los en de motor stopt meteen. Wat is de meest waarschijnlijke fout?",
            listOf("De stopknop is kapot", "Het zelfhoudcontact is niet (goed) aangesloten", "De motor is te zwaar belast", "De spoel is te sterk"), 1,
            "Zonder werkend zelfhoudcontact loopt de stroom alleen via de startknop. Loslaten betekent dan uit."),
        Meerkeuze("q4", "relais", "Welk klemnummer hoort bij een NC-hulpcontact?",
            listOf("13-14", "A1-A2", "21-22", "1-2 van het hoofdcontact"), 2,
            "NC-hulpcontacten eindigen op 1 en 2, zoals 11-12 of 21-22. NO-contacten eindigen op 3 en 4."),
        Meerkeuze("q5", "relais", "De draad naar een NC-stopknop breekt. Wat gebeurt er?",
            listOf("De motor blijft draaien", "De motor start vanzelf", "De schakeling valt uit", "Er gebeurt niets"), 2,
            "Een NC-stopknop is draadbreukveilig. Een breuk werkt als indrukken: de spoel valt af."),
        // ⚡ Wet van Ohm
        Rekenvraag("q6", "ohm", "Er staat 12 V over een weerstand van 4 Ω. Hoe groot is de stroom?", 3.0, "A", "I = U ÷ R = 12 ÷ 4 = 3 A"),
        Rekenvraag("q7", "ohm", "Een lamp op 230 V trekt 0,5 A. Hoe groot is het vermogen?", 115.0, "W", "P = U × I = 230 × 0,5 = 115 W"),
        Rekenvraag("q8", "ohm", "Door een weerstand van 100 Ω loopt 2 A. Hoe groot is de spanning?", 200.0, "V", "U = I × R = 2 × 100 = 200 V"),
        Rekenvraag("q9", "ohm", "Een kachel van 2300 W op 230 V. Hoe groot is de stroom?", 10.0, "A", "I = P ÷ U = 2300 ÷ 230 = 10 A"),
        // 🏛️ Politiek en rechtsstaat
        Meerkeuze("qb1", "politiek", "Welke macht maakt de wetten?",
            listOf("De wetgevende macht", "De uitvoerende macht", "De rechterlijke macht", "De politie"), 0,
            "De wetgevende macht maakt de wetten. In Nederland zijn dat de regering en de Tweede en Eerste Kamer samen."),
        Meerkeuze("qb2", "politiek", "Hoeveel zetels heeft de Tweede Kamer?",
            listOf("75", "100", "150", "225"), 2,
            "De Tweede Kamer heeft 150 zetels. De Eerste Kamer heeft er 75."),
        Meerkeuze("qb3", "politiek", "Vanaf welke leeftijd mag je stemmen voor de Tweede Kamer?",
            listOf("16 jaar", "18 jaar", "21 jaar", "25 jaar"), 1,
            "Je mag stemmen vanaf 18 jaar, als je de Nederlandse nationaliteit hebt."),
        Meerkeuze("qb4", "politiek", "Waar gaat artikel 1 van de Grondwet over?",
            listOf("Vrijheid van meningsuiting", "Gelijke behandeling en geen discriminatie", "Het koningshuis", "Belasting betalen"), 1,
            "Artikel 1: iedereen wordt in gelijke gevallen gelijk behandeld. Discriminatie mag niet."),
        // 🏛️ Werk en geld
        Meerkeuze("qb5", "werkgeld", "Wat krijg je echt op je rekening?",
            listOf("Je brutoloon", "Je nettoloon", "Je vakantiegeld", "Je cao"), 1,
            "Netto is wat overblijft na belasting en premies. Dat komt op je rekening."),
        Meerkeuze("qb6", "werkgeld", "Wie maken samen een cao?",
            listOf("De regering en de Tweede Kamer", "Werkgevers en vakbonden", "De gemeente en de werknemer", "De rechter en de werkgever"), 1,
            "Werkgevers en vakbonden maken samen afspraken in een cao."),
        // 📝 Werkwoorden spellen
        Meerkeuze("qn1", "spelling", "Hij ___ morgen 18 jaar.",
            listOf("word", "wordt", "wort", "wordd"), 1,
            "Truc: hij loopt → hij wordt. Stam 'word' + t."),
        Meerkeuze("qn2", "spelling", "Ik ___ het spannend.",
            listOf("vind", "vindt", "vint", "vinden"), 0,
            "Bij 'ik' alleen de stam: ik vind. Truc: ik loop."),
        Meerkeuze("qn3", "spelling", "Gisteren ___ ik de hele dag.",
            listOf("werkde", "werkte", "werkten", "werkt"), 1,
            "De stam 'werk' eindigt op een k. Die zit in 't kofschip, dus -te: werkte."),
        Meerkeuze("qn4", "spelling", "Wat is er ___?",
            listOf("gebeurt", "gebeurd", "gebeurdt", "gebeur"), 1,
            "Voltooid deelwoord. Stam 'gebeur' eindigt op r, niet in 't kofschip, dus een d: gebeurd."),
        Meerkeuze("qn5", "spelling", "___ jij ook moe?",
            listOf("Word", "Wordt", "Wort", "Worden"), 0,
            "Staat 'jij' achter het werkwoord, dan geen t: word jij."),
        // 📝 Teksten lezen en schrijven
        Meerkeuze("qn6", "teksten", "Wat is meestal het doel van een reclame?",
            listOf("Informeren", "Overtuigen", "Amuseren", "Uitleggen hoe iets werkt"), 1,
            "Een reclame wil je overtuigen om iets te kopen of te doen."),
        Meerkeuze("qn7", "teksten", "Wat is de hoofdgedachte van een tekst?",
            listOf("De titel", "Het eerste woord", "De belangrijkste boodschap in één zin", "De naam van de schrijver"), 2,
            "De hoofdgedachte is de belangrijkste boodschap. Je kunt hem in één zin zeggen."),
        Meerkeuze("qn8", "teksten", "Je mailt een bedrijf en weet de naam niet. Hoe begin je?",
            listOf("Hoi!", "Hallo allemaal,", "Geachte heer, mevrouw,", "Beste vriend,"), 2,
            "In een formele mail zonder naam schrijf je: Geachte heer, mevrouw,"),
        // 🎯 Project en bewijsstuk
        Meerkeuze("qk1", "project", "Waar staat de T in SMART voor?",
            listOf("Toetsbaar", "Tijdgebonden", "Teamwerk", "Taak"), 1,
            "Tijdgebonden: je doel heeft een datum waarop het klaar moet zijn."),
        Meerkeuze("qk2", "project", "Wat is de laatste R in STARR?",
            listOf("Resultaat", "Rapport", "Reflectie", "Regel"), 2,
            "Reflectie: je kijkt terug. Wat ging goed? Wat doe je de volgende keer anders?"),
        Meerkeuze("qk3", "project", "Wat is een bewijsstuk?",
            listOf("Een cijferlijst", "Iets wat je maakt om te laten zien dat je iets kunt", "Een brief van je ouders", "Je rooster"), 1,
            "Een bewijsstuk laat zien wat je kunt, zoals een verslag, product of presentatie."),
    )
}
