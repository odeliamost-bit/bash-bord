package nl.odelia.dashboard.onthoud

import java.text.Normalizer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/** Waar iemand of iets bij hoort (de "organisatie"). */
enum class Groep(val label: String, val emoji: String) {
    School("School", "🎒"),
    Werk("Werk", "🏭"),
    Thuis("Thuis", "🏠"),
}

/** Wat voor afspraak het is (uit ontwerp/Toevoegen.dc.html). Mag ook leeg blijven. */
enum class Soort(val label: String) {
    Inleveren("Inleveren"),
    Maken("Maken"),
    Bellen("Bellen"),
    Mailen("Mailen"),
    Meenemen("Meenemen"),
    Vragen("Vragen"),
}

/** Hoe iets in de app kwam. */
enum class Bron { Getypt, Ingesproken, Gedeeld, Geplakt }

data class Persoon(
    val id: String,
    val naam: String,
    val groep: Groep,
    val rol: String = "",
    /** Welke kleur het rondje krijgt (0, 1, 2 …). */
    val kleur: Int = 0,
) {
    /** Eerste letter van de naam zonder "Dhr."/"Mevr." ervoor: "Dhr. Jansen" → "J". */
    val letter: String
        get() = naam.split(" ").lastOrNull { it.isNotBlank() && !it.endsWith(".") }?.take(1)?.uppercase()
            ?: naam.take(1).uppercase()
}

/** Iets om te onthouden: een afspraak, een taak, een deadline. Alleen [wat] is verplicht. */
data class Afspraak(
    val id: String,
    val wat: String,
    val notitie: String = "",
    val persoonId: String? = null,
    val groep: Groep? = null,
    val project: String? = null,
    val soort: Soort? = null,
    val deadline: LocalDate? = null,
    val seintje: Boolean = false,
    val klaar: Boolean = false,
    val gemaakt: LocalDateTime,
    val bron: Bron = Bron.Getypt,
    /** To-do (iets om zelf te doen) in plaats van een afspraak. Kies je bovenaan bij "Onthoud dit". */
    val todo: Boolean = false,
)

/** Wanneer iets moet: om de lijst in stukjes te verdelen. */
enum class Wanneer(val kop: String) {
    TeLaat("Te laat"),
    Vandaag("Vandaag"),
    Morgen("Morgen"),
    DezeWeek("Deze week"),
    Later("Later"),
    ZonderDatum("Geen datum"),
    Klaar("Klaar"),
}

/** Alle afspraken en personen, met handige vragen erover. */
data class OnthoudStaat(val afspraken: List<Afspraak>, val personen: List<Persoon>, val vandaag: LocalDate) {
    private val perId = personen.associateBy { it.id }

    fun persoon(id: String?): Persoon? = id?.let { perId[it] }

    /** Groep van een afspraak: zelf gekozen, of die van de persoon. */
    fun groep(a: Afspraak): Groep? = a.groep ?: persoon(a.persoonId)?.groep

    val open: List<Afspraak> get() = afspraken.filter { !it.klaar }.sortedWith(volgorde)

    /** Alle projecten die al eens gebruikt zijn, de meest gebruikte eerst. */
    val projecten: List<String>
        get() = afspraken.mapNotNull { it.project?.trim()?.takeIf(String::isNotEmpty) }
            .groupingBy { it }.eachCount().entries.sortedByDescending { it.value }.map { it.key }

    fun wanneer(a: Afspraak): Wanneer {
        val d = a.deadline
        return when {
            a.klaar -> Wanneer.Klaar
            d == null -> Wanneer.ZonderDatum
            d < vandaag -> Wanneer.TeLaat
            d == vandaag -> Wanneer.Vandaag
            d == vandaag.plusDays(1) -> Wanneer.Morgen
            d <= zondag(vandaag) -> Wanneer.DezeWeek
            else -> Wanneer.Later
        }
    }

    /** Afspraken van vandaag (en te laat). */
    val vandaagTeDoen: List<Afspraak> get() = open.filter { wanneer(it) in setOf(Wanneer.TeLaat, Wanneer.Vandaag) }

    /** Afspraken voor de rest van deze week (na vandaag). */
    val restVanDeWeek: List<Afspraak> get() = open.filter { wanneer(it) in setOf(Wanneer.Morgen, Wanneer.DezeWeek) }

    /** Afspraken voor morgen met "seintje de dag ervoor" aan. */
    val seintjeVoorMorgen: List<Afspraak> get() = open.filter { it.seintje && it.deadline == vandaag.plusDays(1) }

    fun vanPersoon(id: String): List<Afspraak> = afspraken.filter { it.persoonId == id }.sortedWith(volgorde)

    /**
     * Zoeken in alles: wat, notitie, persoon (naam en rol), project, groep, soort en "to-do"/"afspraak".
     * Hoofdletters en accenten maken niet uit. Meerdere woorden = alle woorden moeten erin staan.
     */
    fun zoek(vraag: String): List<Afspraak> {
        val woorden = plat(vraag).split(" ").filter { it.isNotBlank() }
        if (woorden.isEmpty()) return emptyList()
        return afspraken.filter { a ->
            val p = persoon(a.persoonId)
            val type = if (a.todo) "to-do todo" else "afspraak"
            val tekst = plat(listOfNotNull(a.wat, a.notitie, p?.naam, p?.rol, a.project, groep(a)?.label, a.soort?.label, type).joinToString(" "))
            woorden.all { it in tekst }
        }.sortedWith(compareBy<Afspraak> { it.klaar }.then(volgorde))
    }

    /** Samenvatting van een groep van afspraken (per project of per persoon). */
    fun samenvatting(lijst: List<Afspraak>): Samenvatting {
        val nogDoen = lijst.filter { !it.klaar }
        return Samenvatting(
            open = nogDoen.size,
            klaar = lijst.size - nogDoen.size,
            eerstvolgende = nogDoen.filter { it.deadline != null }.minByOrNull { it.deadline!! },
        )
    }

    /** Eerst de vroegste deadline, dan zonder datum; binnen gelijke datum de nieuwste eerst. */
    private val volgorde = compareBy<Afspraak>({ it.deadline == null }, { it.deadline })
        .thenByDescending { it.gemaakt }
}

data class Samenvatting(val open: Int, val klaar: Int, val eerstvolgende: Afspraak?)

private fun zondag(d: LocalDate): LocalDate = d.plusDays((DayOfWeek.SUNDAY.value - d.dayOfWeek.value).toLong())

/** "Één Café" → "een cafe": voor zoeken zonder op hoofdletters en accenten te letten. */
fun plat(tekst: String): String =
    Normalizer.normalize(tekst.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "").replace(Regex("\\s+"), " ").trim()
