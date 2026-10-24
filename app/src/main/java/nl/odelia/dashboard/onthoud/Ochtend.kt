package nl.odelia.dashboard.onthoud

import java.time.DayOfWeek
import java.time.Duration

/** De melding van het ochtendbericht. [morgen]: er staat iets voor morgen met "seintje de dag ervoor". */
data class Bericht(val titel: String, val tekst: String, val morgen: Boolean)

/**
 * Het ochtendbericht: 20 minuten na het eerste inklokken van de dag (CLAUDE.md stap 4).
 * Hierin staat ook het seintje voor afspraken van morgen (keuze van Odelia), met de knop "Vanavond nog een keer".
 */
object Ochtend {
    val NA_INKLOKKEN: Duration = Duration.ofMinutes(20)

    /** Wat er vandaag, morgen (seintje) en de rest van de week staat. */
    fun bericht(staat: OnthoudStaat): Bericht {
        val vandaag = staat.vandaagTeDoen
        val morgen = staat.seintjeVoorMorgen
        val week = staat.restVanDeWeek - morgen.toSet()
        val regels = buildList {
            if (vandaag.isNotEmpty()) {
                add("Vandaag:")
                vandaag.forEach { add("• ${it.wat}${wie(it, staat)}${if (staat.wanneer(it) == Wanneer.TeLaat) " (te laat)" else ""}") }
            }
            if (morgen.isNotEmpty()) {
                if (isNotEmpty()) add("")
                add("📌 Morgen:")
                morgen.forEach { add("• ${it.wat}${wie(it, staat)}") }
            }
            if (week.isNotEmpty()) {
                if (isNotEmpty()) add("")
                add("Deze week:")
                week.forEach { add("• ${it.wat}${wie(it, staat)} (${dagNaam(it, staat)})") }
            }
            if (staat.vandaag.dayOfWeek == DayOfWeek.FRIDAY) {
                if (isNotEmpty()) add("")
                add("📧 Vandaag na het werk: urenmail voor Kim klaarzetten.")
            }
        }
        val titel = when {
            vandaag.size == 1 -> "Goedemorgen! ☀️ Vandaag: ${vandaag[0].wat}"
            vandaag.size > 1 -> "Goedemorgen! ☀️ Vandaag ${vandaag.size} dingen"
            morgen.size == 1 -> "Goedemorgen! ☀️ Morgen: ${morgen[0].wat}"
            morgen.size > 1 -> "Goedemorgen! ☀️ Morgen ${morgen.size} dingen"
            week.isNotEmpty() -> "Goedemorgen! ☀️ Vandaag niets, deze week ${week.size}."
            else -> "Goedemorgen! ☀️ Vandaag staat er niets."
        }
        return Bericht(titel, regels.joinToString("\n").ifEmpty { "Fijne werkdag!" }, morgen = morgen.isNotEmpty())
    }

    /** Kort, om voor te lezen bij de welkomkaart. */
    fun voorlees(staat: OnthoudStaat): String {
        val vandaag = staat.vandaagTeDoen
        return when (vandaag.size) {
            0 -> "Vandaag staat er niets in Onthoud."
            1 -> "Vandaag: ${vandaag[0].wat}."
            else -> "Vandaag ${vandaag.size} dingen: " + vandaag.joinToString(", ") { it.wat } + "."
        }
    }

    private fun wie(a: Afspraak, staat: OnthoudStaat) = staat.persoon(a.persoonId)?.let { " – ${it.naam}" } ?: ""

    private fun dagNaam(a: Afspraak, staat: OnthoudStaat): String =
        if (staat.wanneer(a) == Wanneer.Morgen) "morgen" else DAGEN[a.deadline!!.dayOfWeek.value - 1]

    private val DAGEN = listOf("maandag", "dinsdag", "woensdag", "donderdag", "vrijdag", "zaterdag", "zondag")
}
