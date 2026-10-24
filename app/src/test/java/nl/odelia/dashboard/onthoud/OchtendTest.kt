package nl.odelia.dashboard.onthoud

import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OchtendTest {
    private val woensdag = LocalDate.of(2026, 10, 21)
    private val jansen = Persoon("p1", "Dhr. Jansen", Groep.School)
    private fun a(id: String, wat: String, deadline: LocalDate?, persoon: Persoon? = null, seintje: Boolean = false) =
        Afspraak(id, wat, persoonId = persoon?.id, deadline = deadline, seintje = seintje, gemaakt = LocalDateTime.of(2026, 10, 19, 9, 0))

    @Test
    fun `vandaag en deze week in de melding`() {
        val staat = OnthoudStaat(
            listOf(
                a("1", "Tandarts bellen", woensdag),
                a("2", "Boek meenemen", woensdag.plusDays(1), jansen),
                a("3", "Verslag inleveren", woensdag.plusDays(2), jansen),
                a("4", "Volgende week", woensdag.plusDays(7)),
            ),
            listOf(jansen),
            woensdag,
        )
        val (titel, tekst) = Ochtend.bericht(staat)
        assertEquals("Goedemorgen! ☀️ Vandaag: Tandarts bellen", titel)
        assertEquals(
            "Vandaag:\n• Tandarts bellen\n\nDeze week:\n• Boek meenemen – Dhr. Jansen (morgen)\n• Verslag inleveren – Dhr. Jansen (vrijdag)" +
                "\n\n💡 Tijd over? Je kunt dit al doen:\n• Volgende week (wo 28 okt)",
            tekst,
        )
    }

    @Test
    fun `rustige dag geeft tips, eerst later en dan zonder datum`() {
        fun zonder(id: String, wat: String, dagenGeleden: Long, duur: Duur?) =
            a(id, wat, null).copy(gemaakt = woensdag.atTime(9, 0).minusDays(dagenGeleden), duur = duur)
        val staat = OnthoudStaat(
            listOf(
                a("1", "Verslag schrijven", woensdag.plusDays(12), jansen).copy(duur = Duur.Week),
                zonder("2", "Fietsband plakken", 2, Duur.Kwartier),
                zonder("3", "Kast opruimen", 10, Duur.Uur),
                zonder("4", "Foto's uitzoeken", 1, null),
                zonder("5", "Boek lezen", 0, null),
            ),
            listOf(jansen),
            woensdag,
        )
        val b = Ochtend.bericht(staat)
        // 2 uur over: verslag (begin alvast = 1 uur), dan de oudste zonder datum (kast, 1 uur). Dan is de tijd op,
        // dus de fietsband (15 min) past niet meer.
        assertEquals("Goedemorgen! ☀️ Rustige dag: 2 tips", b.titel)
        assertEquals(
            "💡 Rustige dag. Je kunt dit al doen:\n" +
                "• Begin alvast: Verslag schrijven – Dhr. Jansen (ma 2 nov · 1 week)\n" +
                "• Kast opruimen (1 uur)\n\n" +
                "📋 Nog 3 dingen zonder datum.",
            b.tekst,
        )
    }

    @Test
    fun `tips passen in de tijd die over is`() {
        val staat = OnthoudStaat(
            listOf(
                a("1", "Tandarts bellen", woensdag).copy(duur = Duur.Uur),
                a("2", "Kamer verven", null).copy(duur = Duur.PaarUur),
                a("3", "Fietsband plakken", null).copy(duur = Duur.Kwartier),
            ),
            emptyList(),
            woensdag,
        )
        // 1 uur over: verven (paar uur) past niet, de fietsband (15 min) wel.
        assertEquals(listOf("3"), Ochtend.tips(staat).map { it.id })
        assertTrue(Ochtend.bericht(staat).tekst.endsWith("• Fietsband plakken (15 min)\n\n📋 Nog 1 ding zonder datum."))
    }

    @Test
    fun `drukke dag geeft geen tips, wel hoeveel er zonder datum is`() {
        val staat = OnthoudStaat(
            listOf(
                a("1", "Presentatie maken", woensdag).copy(duur = Duur.PaarUur),
                a("2", "Kast opruimen", null).copy(duur = Duur.Uur),
                a("3", "Fietsband plakken", null).copy(duur = Duur.Kwartier),
            ),
            emptyList(),
            woensdag,
        )
        assertTrue(Ochtend.tips(staat).isEmpty())
        assertEquals("Vandaag:\n• Presentatie maken\n\n📋 2 dingen zonder datum.", Ochtend.bericht(staat).tekst)
    }

    @Test
    fun `seintje voor morgen staat apart in het ochtendbericht`() {
        val staat = OnthoudStaat(
            listOf(a("2", "Boek meenemen", woensdag.plusDays(1), jansen, seintje = true), a("3", "Verslag inleveren", woensdag.plusDays(2))),
            listOf(jansen),
            woensdag,
        )
        val b = Ochtend.bericht(staat)
        assertEquals("Goedemorgen! ☀️ Morgen: Boek meenemen", b.titel)
        assertEquals("📌 Morgen:\n• Boek meenemen – Dhr. Jansen\n\nDeze week:\n• Verslag inleveren (vrijdag)", b.tekst)
        assertTrue(b.morgen)
    }

    @Test
    fun `niets te doen`() {
        val (titel, tekst, morgen) = Ochtend.bericht(OnthoudStaat(emptyList(), emptyList(), woensdag))
        assertEquals("Goedemorgen! ☀️ Vandaag staat er niets.", titel)
        assertEquals("Fijne werkdag!", tekst)
        assertFalse(morgen)
    }

    @Test
    fun `op vrijdag de urenmail noemen`() {
        val (_, tekst) = Ochtend.bericht(OnthoudStaat(emptyList(), emptyList(), LocalDate.of(2026, 10, 23)))
        assertEquals("📧 Vandaag na het werk: urenmail voor Kim klaarzetten.", tekst)
    }

    @Test
    fun voorlezen() {
        val staat = OnthoudStaat(listOf(a("1", "Tandarts bellen", woensdag), a("2", "Was doen", woensdag)), emptyList(), woensdag)
        assertEquals("Vandaag 2 dingen: Tandarts bellen, Was doen.", Ochtend.voorlees(staat))
    }
}
