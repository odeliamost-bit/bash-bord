package nl.odelia.dashboard.logica

import java.time.LocalDate

/** Eén persoon met alles wat je met hem of haar hebt afgesproken. Komt uit Onthoud (het veld "wie"). */
data class Persoon(val naam: String, val groep: String, val afspraken: List<Onthoud>) {
    val nogDoen: Int get() = afspraken.count { !it.klaar }
}

/** Mensen-scherm (ontwerp/Mensen.dc.html): personen per groep, hun afspraken, en tips om te zoeken. */
object Mensen {
    /** Groepen die er altijd staan, in de volgorde van het ontwerp. */
    val vasteGroepen = listOf("School", "Werk", "Thuis")

    /** Voor mensen zonder groep. */
    const val OVERIG = "Overig"

    private val maanden = listOf("jan", "feb", "mrt", "apr", "mei", "jun", "jul", "aug", "sep", "okt", "nov", "dec")

    /**
     * Alle personen. Iemand hoort bij de groep die het vaakst bij zijn of haar afspraken staat
     * (bij gelijk: de nieuwste afspraak). Zonder groep: [OVERIG].
     */
    fun personen(items: List<Onthoud>): List<Persoon> =
        items.filter { !it.wie.isNullOrBlank() }
            .groupBy { it.wie!!.trim() }
            .map { (naam, lijst) ->
                val groep = lijst.filter { !it.groep.isNullOrBlank() }
                    .groupBy { it.groep!! }
                    .maxWithOrNull(compareBy<Map.Entry<String, List<Onthoud>>> { it.value.size }.thenBy { e -> e.value.maxOf { it.gemaakt } })
                    ?.key ?: OVERIG
                Persoon(naam, groep, afspraken(lijst))
            }
            .sortedBy { it.naam.lowercase() }

    /** De tabjes: altijd School, Werk, Thuis; daarna andere groepen die mensen hebben. */
    fun groepen(personen: List<Persoon>): List<String> {
        val extra = personen.map { it.groep }.distinct().filter { it !in vasteGroepen && it != OVERIG }.sorted()
        val overig = if (personen.any { it.groep == OVERIG }) listOf(OVERIG) else emptyList()
        return vasteGroepen + extra + overig
    }

    /** Eerst wat nog moet (snelste datum eerst, zonder datum achteraan), daarna wat klaar is. */
    fun afspraken(items: List<Onthoud>): List<Onthoud> =
        items.sortedWith(compareBy<Onthoud> { it.klaar }.thenBy { it.wanneer == null }.thenBy { it.wanneer }.thenByDescending { it.gemaakt })

    /** Haast: te laat, of binnen 3 dagen. */
    fun binnenkort(o: Onthoud, vandaag: LocalDate): Boolean {
        val w = o.wanneer ?: return false
        return !o.klaar && !w.isAfter(vandaag.plusDays(3))
    }

    /** "di 20 okt" */
    fun gezegdOp(datum: LocalDate): String = "${korteDatum(datum)} ${maanden[datum.monthValue - 1]}"

    /** Letter in het rondje: eerste letter van het laatste woord ("Dhr. Jansen" → "J"). */
    fun letter(naam: String): String =
        naam.trim().split(" ").lastOrNull { it.isNotBlank() }?.firstOrNull()?.uppercase() ?: "?"

    /** Een paar zoek-tips: namen en woorden uit de nieuwste afspraken. */
    fun probeer(items: List<Onthoud>, aantal: Int = 3): List<String> {
        val nieuwste = items.sortedByDescending { it.gemaakt }
        val woorden = nieuwste.mapNotNull { o ->
            o.tekst.split(" ").map { w -> w.trim { !it.isLetterOrDigit() } }.firstOrNull { it.length >= 4 }?.lowercase()
        }
        val namen = nieuwste.mapNotNull { it.wie?.trim()?.split(" ")?.last() }
        // Om en om: woord, naam, woord …
        val mix = woorden.zip(namen).flatMap { listOf(it.first, it.second) } + woorden + namen
        return mix.filter { it.isNotBlank() }.distinctBy { it.lowercase() }.take(aantal)
    }
}
