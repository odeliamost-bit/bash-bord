package nl.odelia.dashboard.onthoud

/** Wat je op de Onthoud-schermen kunt doen. */
class OnthoudActies(
    val bewaar: (Afspraak) -> Unit,
    val zetKlaar: (id: String, klaar: Boolean) -> Unit,
    val verwijder: (id: String) -> Unit,
    /** Nieuwe persoon; [klaar] krijgt de nieuwe persoon (met id) terug. */
    val nieuwePersoon: (naam: String, groep: Groep, rol: String, klaar: (Persoon) -> Unit) -> Unit,
    /** Naam, beschrijving of groep veranderen. */
    val pasPersoonAan: (Persoon) -> Unit,
    /** Persoon weg; de afspraken blijven staan, zonder naam. */
    val verwijderPersoon: (id: String) -> Unit,
    /** Inspreken met de spraakherkenning van de telefoon; [klaar] krijgt de tekst. */
    val inspreken: (klaar: (String) -> Unit) -> Unit,
) {
    companion object {
        /** Doet niets (voor voorbeelden). */
        val niets = OnthoudActies({}, { _, _ -> }, {}, { _, _, _, _ -> }, {}, {}, {})
    }
}
