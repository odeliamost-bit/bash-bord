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
            "Vandaag:\n• Tandarts bellen\n\nDeze week:\n• Boek meenemen – Dhr. Jansen (morgen)\n• Verslag inleveren – Dhr. Jansen (vrijdag)",
            tekst,
        )
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
