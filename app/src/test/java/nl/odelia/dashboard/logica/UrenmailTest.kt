package nl.odelia.dashboard.logica

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

            Hier de uren van deze week.
            Woensdag was ik vrij.

            Met hartelijke groet,
            Odelia van der Most

            Datum | Begin | Eind | Pauze | Totaal
            ma 19-10 | 7:45 | 15:45 | 0:45 | 7,25
            di 20-10 | 7:30 | 15:45 | 0:45 | 7,50
            wo 21-10 | vrij
            do 22-10 | ziek
            vr 23-10 | –

            Totaal: 14,75 uur

            De tabel zit ook als PDF in de bijlage.
        """.trimIndent()
        assertEquals(verwacht, Urenmail.tekst(dagen, " Woensdag was ik vrij. ", "deze week", bijlage = true))
    }

    @Test
    fun zonderOpmerking() {
        val tekst = Urenmail.tekst(emptyList(), "")
        assertTrue(tekst.startsWith("Hoi Kim,\n\nHier de uren van afgelopen week.\n\nMet hartelijke groet,"))
        assertTrue(tekst.endsWith("Totaal: 0,00 uur"))
    }

    @Test
    fun periodesEnOnderwerp() {
        val vrijdag = LocalDate.of(2026, 10, 30)
        val dag = Urenmail.periode(UrenBereik.Vandaag, vrijdag)
        assertEquals("Uren Odelia – vr 30-10-2026", Urenmail.onderwerp(dag))
        assertEquals("vandaag", dag.zin)

        val week = Urenmail.periode(UrenBereik.Week, vrijdag)
        assertEquals(LocalDate.of(2026, 10, 26), week.van)
        assertEquals(LocalDate.of(2026, 11, 1), week.tot)
        assertEquals("Uren Odelia – week 44 (26-10 t/m 30-10)", Urenmail.onderwerp(week))

        val maand = Urenmail.periode(UrenBereik.Maand, vrijdag)
        assertEquals(LocalDate.of(2026, 10, 1), maand.van)
        assertEquals(LocalDate.of(2026, 10, 31), maand.tot)
        assertEquals("Uren Odelia – oktober 2026", Urenmail.onderwerp(maand))

        // Zelf kiezen: omgedraaid mag ook.
        val zelf = Urenmail.periode(UrenBereik.Zelf, vrijdag, LocalDate.of(2026, 10, 24), LocalDate.of(2026, 10, 13))
        assertEquals(LocalDate.of(2026, 10, 13), zelf.van)
        assertEquals("Uren Odelia – 13-10 t/m 24-10-2026", Urenmail.onderwerp(zelf))
        val overJaar = Urenmail.periode(UrenBereik.Zelf, vrijdag, LocalDate.of(2025, 12, 29), LocalDate.of(2026, 1, 2))
        assertEquals("29-12-2025 t/m 2-1-2026", overJaar.naam)
    }

    @Test
    fun welkeDagenInDeTabel() {
        val nu = LocalDateTime.of(2026, 10, 28, 12, 0) // woensdag
        val perioden = listOf(
            Periode(LocalDateTime.of(2026, 10, 24, 9, 0), LocalDateTime.of(2026, 10, 24, 12, 0)), // zaterdag gewerkt
            Periode(LocalDateTime.of(2026, 10, 27, 7, 45), LocalDateTime.of(2026, 10, 27, 15, 45)),
        )
        // Zelf: za 24 t/m vr 30. Zondag (leeg) valt weg, zaterdag (gewerkt) blijft, na vandaag telt niet.
        val p = Urenmail.periode(UrenBereik.Zelf, nu.toLocalDate(), LocalDate.of(2026, 10, 24), LocalDate.of(2026, 10, 30))
        val dagen = Urenmail.dagen(perioden, emptyMap(), p, nu)
        assertEquals(listOf(24, 26, 27, 28), dagen.map { it.datum.dayOfMonth })
        assertEquals(2.25f + 7.25f, Urenmail.totaal(dagen), 0.001f)
        assertEquals("Totaal: 9,50 uur", Urenmail.totaalRegel(dagen))

        // Eén dag gekozen (zondag, leeg): die staat er toch in.
        val zondag = Urenmail.periode(UrenBereik.Zelf, nu.toLocalDate(), LocalDate.of(2026, 10, 25), LocalDate.of(2026, 10, 25))
        assertEquals(1, Urenmail.dagen(perioden, emptyMap(), zondag, nu).size)

        // Helemaal in de toekomst: niks.
        val later = Urenmail.periode(UrenBereik.Zelf, nu.toLocalDate(), LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 6))
        assertTrue(Urenmail.dagen(perioden, emptyMap(), later, nu).isEmpty())
    }

    @Test
    fun nogBezig() {
        val d = Werkdag(maandag, DagSoort.Gewerkt, LocalTime.of(7, 50), null, 3f, true)
        assertEquals(UrenRij("ma 19-10", "7:50", "bezig", "0:45", "3,00"), Urenmail.rij(d))
    }

    @Test
    fun bestandsnaam() {
        val week = Urenmail.periode(UrenBereik.Week, LocalDate.of(2026, 10, 30))
        assertEquals("Uren Odelia - week 44 (26-10 tot 30-10).pdf", Urenmail.bestandsnaam(week))
    }
}
