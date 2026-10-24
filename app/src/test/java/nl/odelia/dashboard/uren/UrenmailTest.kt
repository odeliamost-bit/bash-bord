package nl.odelia.dashboard.uren

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrenmailTest {
    private val maandag = LocalDate.of(2026, 10, 19)

    private fun blok(dag: Long, van: String, tot: String?) = maandag.plusDays(dag).let { d ->
        Blok("b$dag", d.atTime(LocalTime.parse(van)), tot?.let { d.atTime(LocalTime.parse(it)) })
    }

    private val staat = KlokStaat.maak(
        listOf(blok(0, "07:45", "15:45"), blok(1, "07:30", "16:00"), blok(4, "07:45", "15:50")),
        mapOf(maandag.plusDays(2) to DagSoort.Vrij, maandag.plusDays(3) to DagSoort.Ziek),
        LocalDateTime.of(2026, 10, 23, 16, 30),
    )

    @Test
    fun `de mail volgens CLAUDE_md, met vrij en ziek als tekst`() {
        val tekst = Urenmail.tekst(Urenmail.regels(maandag, staat), "")
        assertEquals(
            """
            Hoi Kim,

            Hier de uren van afgelopen week.

            Met hartelijke groet,
            Odelia van der Most

            ma 19-10-2026: 7:45 – 15:45 = 7:15 uur
            di 20-10-2026: 7:30 – 16:00 = 7:45 uur
            wo 21-10-2026: vrij
            do 22-10-2026: ziek
            vr 23-10-2026: 7:45 – 15:50 = 7:20 uur
            Totaal: 22:20 uur · 1 dag vrij · 1 dag ziek

            """.trimIndent(),
            tekst,
        )
    }

    @Test
    fun `opmerking komt onder de eerste zin`() {
        val tekst = Urenmail.tekst(Urenmail.regels(maandag, staat), "Woensdag was ik vrij (verlof).")
        assertTrue(tekst.startsWith("Hoi Kim,\n\nHier de uren van afgelopen week.\nWoensdag was ik vrij (verlof).\n\nMet hartelijke groet,"))
    }

    @Test
    fun `dag zonder klokken krijgt een streepje`() {
        val leeg = KlokStaat.maak(emptyList(), emptyMap(), LocalDateTime.of(2026, 10, 23, 16, 30))
        assertEquals("ma 19-10-2026: –", Urenmail.regel(Urenmail.regels(maandag, leeg)[0]))
    }

    @Test
    fun `uren als uren en minuten`() {
        assertEquals("7:15", Urenmail.uurMin(7.25f))
        assertEquals("7:00", Urenmail.uurMin(7f))
        assertEquals("7:20", Urenmail.uurMin(7f + 20f / 60f))
        assertEquals("36:05", Urenmail.uurMin(36f + 5f / 60f))
    }

    @Test
    fun `welke week standaard`() {
        assertEquals(maandag, Urenmail.standaardWeek(LocalDate.of(2026, 10, 23))) // vrijdag: deze week
        assertEquals(maandag, Urenmail.standaardWeek(LocalDate.of(2026, 10, 27))) // dinsdag erna: vorige week
    }

    @Test
    fun `vrijdag na uitklokken staat hij klaar`() {
        assertTrue(Urenmail.staatKlaar(LocalDateTime.of(2026, 10, 23, 16, 30), staat))
        val bezig = KlokStaat.maak(listOf(blok(4, "07:45", null)), emptyMap(), LocalDateTime.of(2026, 10, 23, 15, 0))
        assertFalse(Urenmail.staatKlaar(LocalDateTime.of(2026, 10, 23, 15, 0), bezig))
    }
}
