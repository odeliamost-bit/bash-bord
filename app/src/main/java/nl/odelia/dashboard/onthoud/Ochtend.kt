package nl.odelia.dashboard.onthoud

import java.time.DayOfWeek
import java.time.Duration

/** De melding van het ochtendbericht. [morgen]: er staat iets voor morgen met "seintje de dag ervoor". */
data class Bericht(val titel: String, val tekst: String, val morgen: Boolean)

/**
 * Het ochtendbericht: 20 minuten na het eerste inklokken van de dag (CLAUDE.md stap 4).
 * Hierin staat ook het seintje voor afspraken van morgen (keuze van Odelia), met de knop "Vanavond nog een keer".
 * Is er tijd over, dan komen er tips: alvast voorbereiden voor later, of iets zonder datum (wens van Odelia).
 */
object Ochtend {
    val NA_INKLOKKEN: Duration = Duration.ofMinutes(20)

    /** Zoveel minuten per dag voor dingen uit Onthoud, naast het werk (keuze van Claude, 24 okt). */
    const val TIJD_PER_DAG = 120

    /** Zonder "hoe lang duurt het?" rekenen we een half uur. */
    const val ONBEKEND = 30

    /** Iets van een dag of langer doe je niet in één keer: "begin alvast" telt als een uur. */
    const val BEGINNEN = 60

    const val MAX_TIPS = 3

    /** Wat er vandaag, morgen (seintje) en de rest van de week staat. */
    fun bericht(staat: OnthoudStaat): Bericht {
        val vandaag = staat.vandaagTeDoen
        val morgen = staat.seintjeVoorMorgen
        val week = staat.restVanDeWeek - morgen.toSet()
        val tips = tips(staat)
        val zonderDatum = staat.open.count { it.deadline == null && it !in tips }
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
            if (tips.isNotEmpty()) {
                if (isNotEmpty()) add("")
                add(if (vandaag.isEmpty()) "💡 Rustige dag. Je kunt dit al doen:" else "💡 Tijd over? Je kunt dit al doen:")
                tips.forEach { add("• ${tip(it, staat)}") }
            }
            if (zonderDatum > 0) {
                if (isNotEmpty()) add("")
                add("📋 ${if (tips.isEmpty()) "" else "Nog "}$zonderDatum ${if (zonderDatum == 1) "ding" else "dingen"} zonder datum.")
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
            tips.isNotEmpty() -> "Goedemorgen! ☀️ Rustige dag: ${tips.size} ${if (tips.size == 1) "tip" else "tips"}"
            else -> "Goedemorgen! ☀️ Vandaag staat er niets."
        }
        return Bericht(titel, regels.joinToString("\n").ifEmpty { "Fijne werkdag!" }, morgen = morgen.isNotEmpty())
    }

    /**
     * Tips als er vandaag tijd over is (max [MAX_TIPS]). Eerst dingen met een datum na deze week (voorbereiden),
     * de vroegste eerst; dan dingen zonder datum, de oudste eerst. Elke tip moet passen in de tijd die nog over is.
     */
    fun tips(staat: OnthoudStaat): List<Afspraak> {
        var over = TIJD_PER_DAG - staat.vandaagTeDoen.sumOf(::minuten)
        val later = staat.open.filter { staat.wanneer(it) == Wanneer.Later }
        val zonderDatum = staat.open.filter { it.deadline == null }.sortedBy { it.gemaakt }
        return buildList {
            for (a in later + zonderDatum) {
                if (size == MAX_TIPS) break
                if (minuten(a) <= over) {
                    add(a)
                    over -= minuten(a)
                }
            }
        }
    }

    /** Hoeveel minuten iets vandaag kost. */
    private fun minuten(a: Afspraak): Int = when {
        a.duur == null -> ONBEKEND
        a.duur.lang -> BEGINNEN
        else -> a.duur.minuten
    }

    /** "Begin alvast: Verslag schrijven – Dhr. Jansen (ma 2 nov · 1 week)" */
    private fun tip(a: Afspraak, staat: OnthoudStaat): String {
        val extra = listOfNotNull(a.deadline?.let { "${DAGEN_KORT[it.dayOfWeek.value - 1]} ${it.dayOfMonth} ${MAANDEN[it.monthValue - 1]}" }, a.duur?.label)
        val begin = if (a.duur?.lang == true) "Begin alvast: " else ""
        return "$begin${a.wat}${wie(a, staat)}${if (extra.isEmpty()) "" else " (${extra.joinToString(" · ")})"}"
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
    private val DAGEN_KORT = listOf("ma", "di", "wo", "do", "vr", "za", "zo")
    private val MAANDEN = listOf("jan", "feb", "mrt", "apr", "mei", "jun", "jul", "aug", "sep", "okt", "nov", "dec")
}
