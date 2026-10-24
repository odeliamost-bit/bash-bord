package nl.odelia.dashboard.onthoud

import java.time.DayOfWeek
import java.time.Duration

/** De ochtendmelding: 20 minuten na het eerste inklokken van de dag (CLAUDE.md stap 4). */
object Ochtend {
    val NA_INKLOKKEN: Duration = Duration.ofMinutes(20)

    /** Titel en tekst van de melding: wat staat er vandaag en deze week. */
    fun bericht(staat: OnthoudStaat): Pair<String, String> {
        val vandaag = staat.vandaagTeDoen
        val week = staat.restVanDeWeek
        val regels = buildList {
            if (vandaag.isNotEmpty()) {
                add("Vandaag:")
                vandaag.forEach { add("• ${it.wat}${wie(it, staat)}${if (staat.wanneer(it) == Wanneer.TeLaat) " (te laat)" else ""}") }
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
            vandaag.isEmpty() && week.isEmpty() -> "Goedemorgen! ☀️ Vandaag staat er niets."
            vandaag.isEmpty() -> "Goedemorgen! ☀️ Vandaag niets, deze week ${week.size}."
            vandaag.size == 1 -> "Goedemorgen! ☀️ Vandaag: ${vandaag[0].wat}"
            else -> "Goedemorgen! ☀️ Vandaag ${vandaag.size} dingen"
        }
        return titel to regels.joinToString("\n").ifEmpty { "Fijne werkdag!" }
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
