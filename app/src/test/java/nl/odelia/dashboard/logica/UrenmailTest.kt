package nl.odelia.dashboard.logica

import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class UrenmailTest {
    private val maandag = LocalDate.of(2026, 10, 19)

    @Test
    fun welkeWeek() {
        assertEquals(maandag, Urenmail.maandag(maandag.plusDays(4))) // vrijdag
        assertEquals(maandag, Urenmail.maandag(maandag.plusDays(6))) // zondag
        assertEquals(maandag, Urenmail.maandag(maandag.plusDays(7))) // volgende maandag: nog de vorige week
    }

    @Test
    fun tekstEnTabel() {
        val dagen = listOf(
            Werkdag(maandag, DagSoort.Gewerkt, LocalTime.of(7, 45), LocalTime.of(15, 45), 7.25f, false),
            Werkdag(maandag.plusDays(1), DagSoort.Gewerkt, LocalTime.of(7, 30), LocalTime.of(15, 45), 7.5f, false),
            Werkdag(maandag.plusDays(2), DagSoort.Vrij, null, null, 0f, false),
            Werkdag(maandag.plusDays(3), DagSoort.Ziek, null, null, 0f, false),
            Werkdag(maandag.plusDays(4), DagSoort.Leeg, null, null, 0f, false),
        )
        val verwacht = """
            Hoi Kim,

            Hier de uren van afgelopen week.
            Woensdag was ik vrij.

            Met hartelijke groet,
            Odelia van der Most

            Datum | Begin | Eind | Uren
            ma 19-10 | 7:45 | 15:45 | 7,25
            di 20-10 | 7:30 | 15:45 | 7,50
            wo 21-10 | vrij
            do 22-10 | ziek
            vr 23-10 | –
            Totaal | | | 14,75
        """.trimIndent()
        assertEquals(verwacht, Urenmail.tekst(dagen, " Woensdag was ik vrij. "))
    }

    @Test
    fun zonderOpmerking() {
        val tekst = Urenmail.tekst(emptyList(), "")
        assertEquals(true, tekst.startsWith("Hoi Kim,\n\nHier de uren van afgelopen week.\n\nMet hartelijke groet,"))
    }
}
