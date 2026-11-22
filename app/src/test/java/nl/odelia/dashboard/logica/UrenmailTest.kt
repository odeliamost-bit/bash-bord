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
            Hallo Kim,

            Hierbij mijn gewerkte uren van week 43:
            Woensdag was ik vrij.

            ma 19-10: 7:45 – 15:45 = 7,25 uur
            di 20-10: 7:30 – 15:45 = 7,50 uur
            wo 21-10: vrij
            do 22-10: ziek
            vr 23-10: –

            Totaal: 14,75 uur

            Met vriendelijke groet,
            Odelia van der Most
        """.trimIndent()
        assertEquals(verwacht, Urenmail.tekst(dagen, " Woensdag was ik vrij. "))
    }

    @Test
    fun zonderOpmerking() {
        val tekst = Urenmail.tekst(emptyList(), "")
        assertEquals(true, tekst.startsWith("Hallo Kim,\n\nHierbij mijn gewerkte uren:\n\n"))
        assertEquals(true, tekst.endsWith("Met vriendelijke groet,\nOdelia van der Most"))
    }

    @Test
    fun totaalIsOptelsomVanAfgerondeDagen() {
        // Zoals week 47: 9,30 + 9,57 + 7,75 + 9,10 + 8,72 = 44,44
        val uren = listOf(9.30f, 9.5667f, 7.75f, 9.10f, 8.7234f)
        val dagen = uren.mapIndexed { i, u -> Werkdag(maandag.plusDays(i.toLong()), DagSoort.Gewerkt, null, null, u, false) }
        assertEquals("44,44", Urenmail.decimaal(Urenmail.totaal(dagen)))
    }
}
