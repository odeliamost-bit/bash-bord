package nl.odelia.dashboard.logica

import java.time.DayOfWeek
import java.time.LocalDate

/** Haalt uit ingesproken (of getypte) tekst: wanneer, wie, wat en welke groep. */
object Herken {
    private val dagen = mapOf(
        "maandag" to DayOfWeek.MONDAY, "dinsdag" to DayOfWeek.TUESDAY, "woensdag" to DayOfWeek.WEDNESDAY,
        "donderdag" to DayOfWeek.THURSDAY, "vrijdag" to DayOfWeek.FRIDAY, "zaterdag" to DayOfWeek.SATURDAY,
        "zondag" to DayOfWeek.SUNDAY,
    )
    private val maanden = listOf(
        "jan januari", "feb februari", "mrt maart", "apr april", "mei", "jun juni", "jul juli",
        "aug augustus", "sep sept september", "okt oktober", "nov november", "dec december",
    )

    /** Soorten uit het ontwerp, met woorden die erbij horen. */
    val soorten = linkedMapOf(
        "Inleveren" to listOf("inleveren", "inlever", "afgeven", "opsturen"),
        "Maken" to listOf("maken", "afmaken", "schrijven", "invullen"),
        "Bellen" to listOf("bellen", "opbellen", "terugbellen"),
        "Mailen" to listOf("mailen", "mail", "e-mail", "emailen"),
        "Meenemen" to listOf("meenemen", "mee nemen", "neem mee"),
        "Vragen" to listOf("vragen", "navragen", "vraag"),
    )

    /** "morgen", "vrijdag", "volgende week maandag", "26 oktober", "26-10". */
    fun datum(tekst: String, vandaag: LocalDate): LocalDate? {
        val t = tekst.lowercase()
        if (heeftWoord(t, "overmorgen")) return vandaag.plusDays(2)
        if (heeftWoord(t, "morgen")) return vandaag.plusDays(1)
        if (heeftWoord(t, "vandaag") || heeftWoord(t, "vanmiddag") || heeftWoord(t, "vanavond")) return vandaag

        Regex("""\b(\d{1,2})\s*([a-z]+)""").findAll(t).forEach { m ->
            val dag = m.groupValues[1].toInt()
            val maand = maanden.indexOfFirst { namen -> m.groupValues[2] in namen.split(" ") } + 1
            if (maand > 0) maakDatum(vandaag, dag, maand)?.let { return it }
        }
        Regex("""\b(\d{1,2})[-/](\d{1,2})\b""").find(t)?.let { m ->
            maakDatum(vandaag, m.groupValues[1].toInt(), m.groupValues[2].toInt())?.let { return it }
        }

        val volgendeWeek = t.contains("volgende week")
        dagen.forEach { (woord, dag) ->
            if (heeftWoord(t, woord)) {
                var d = vandaag.plusDays(1)
                while (d.dayOfWeek != dag) d = d.plusDays(1)
                if (volgendeWeek && d.isBefore(Uren.maandagVan(vandaag).plusWeeks(1))) d = d.plusWeeks(1)
                return d
            }
        }
        if (volgendeWeek) return Uren.maandagVan(vandaag).plusWeeks(1)
        return null
    }

    /** De eerste bekende naam die in de tekst voorkomt (langste naam eerst). */
    fun uitLijst(tekst: String, bekend: Collection<String>): String? {
        val t = tekst.lowercase()
        return bekend.sortedByDescending { it.length }.firstOrNull { heeftWoord(t, it.lowercase()) }
    }

    fun soort(tekst: String): String? {
        val t = tekst.lowercase()
        return soorten.entries.firstOrNull { (_, woorden) -> woorden.any { heeftWoord(t, it) } }?.key
    }

    private fun heeftWoord(tekst: String, woord: String) =
        Regex("""(^|[^\p{L}])${Regex.escape(woord)}($|[^\p{L}])""").containsMatchIn(tekst)

    /** Dag + maand; ligt die datum al achter ons, dan volgend jaar. */
    private fun maakDatum(vandaag: LocalDate, dag: Int, maand: Int): LocalDate? = try {
        val d = LocalDate.of(vandaag.year, maand, dag)
        if (d.isBefore(vandaag.minusDays(7))) d.plusYears(1) else d
    } catch (_: Exception) {
        null
    }
}
