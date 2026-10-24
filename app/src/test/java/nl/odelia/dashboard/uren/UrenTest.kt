package nl.odelia.dashboard.uren

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UrenTest {
    private val vrijdag = LocalDate.of(2026, 10, 23)

    private fun blok(dag: LocalDate, van: String, tot: String?) =
        Blok("id-$dag-$van", dag.atTime(LocalTime.parse(van)), tot?.let { dag.atTime(LocalTime.parse(it)) })

    @Test
    fun `een gewone dag is 8 uur min 45 minuten pauze`() {
        val dag = Dag(vrijdag, DagSoort.Werk, listOf(blok(vrijdag, "07:45", "15:45")))
        assertEquals(7.25f, Uren.dagUren(dag, vrijdag.atTime(20, 0)))
    }

    @Test
    fun `pauze gaat er ook af op een korte dag, maar nooit onder nul`() {
        val kort = Dag(vrijdag, DagSoort.Werk, listOf(blok(vrijdag, "08:00", "08:30")))
        assertEquals(0f, Uren.dagUren(kort, vrijdag.atTime(20, 0)))
        val niets = Dag(vrijdag, DagSoort.Werk, emptyList())
        assertEquals(0f, Uren.dagUren(niets, vrijdag.atTime(20, 0)))
    }

    @Test
    fun `twee keer klokken op een dag telt op, pauze maar een keer`() {
        val dag = Dag(vrijdag, DagSoort.Werk, listOf(blok(vrijdag, "07:30", "12:00"), blok(vrijdag, "12:30", "16:00")))
        assertEquals(8f - 0.75f, Uren.dagUren(dag, vrijdag.atTime(20, 0)))
    }

    @Test
    fun `vrij en ziek tellen als nul uur`() {
        assertEquals(0f, Uren.dagUren(Dag(vrijdag, DagSoort.Vrij, listOf(blok(vrijdag, "07:45", "15:45"))), vrijdag.atTime(20, 0)))
        assertEquals(0f, Uren.dagUren(Dag(vrijdag, DagSoort.Ziek, emptyList()), vrijdag.atTime(20, 0)))
    }

    @Test
    fun `aan het werk telt tot nu`() {
        val staat = KlokStaat.maak(listOf(blok(vrijdag, "07:45", null)), emptyMap(), vrijdag.atTime(13, 15))
        assertTrue(staat.isIn)
        assertEquals(LocalTime.of(7, 45), staat.sinds)
        assertEquals(5.5f, staat.brutoVandaag)
        assertEquals(4.75f, staat.nettoVandaag)
    }

    @Test
    fun `vergeten uit te klokken telt niet mee en wordt gemeld`() {
        val donderdag = vrijdag.minusDays(1)
        val vergeten = blok(donderdag, "07:45", null)
        val staat = KlokStaat.maak(listOf(vergeten), emptyMap(), vrijdag.atTime(8, 0))
        assertFalse(staat.isIn)
        assertEquals(vergeten, staat.vergeten)
        assertEquals(0f, staat.weekTotaal)
    }

    @Test
    fun `week is maandag tot vrijdag, weekend alleen als er gewerkt is`() {
        val maandag = Uren.maandag(vrijdag)
        assertEquals(LocalDate.of(2026, 10, 19), maandag)
        assertEquals(5, Uren.week(maandag, emptyList(), emptyMap()).size)
        val zaterdag = maandag.plusDays(5)
        val week = Uren.week(maandag, listOf(blok(zaterdag, "10:00", "12:00")), emptyMap())
        assertEquals(6, week.size)
        assertEquals(zaterdag, week.last().datum)
    }

    @Test
    fun `uitgeklokt vandaag geeft de eindtijd`() {
        val staat = KlokStaat.maak(listOf(blok(vrijdag, "07:45", "15:50")), emptyMap(), vrijdag.atTime(17, 0))
        assertFalse(staat.isIn)
        assertEquals(LocalTime.of(15, 50), staat.uitVandaag)
        assertNull(staat.vergeten)
    }

    @Test
    fun `vrij en ziek erbij zetten`() {
        assertEquals(null, Uren.vrijZiek(listOf(DagSoort.Werk, DagSoort.Werk)))
        assertEquals("1 dag ziek", Uren.vrijZiek(listOf(DagSoort.Werk, DagSoort.Ziek)))
        assertEquals("2 dagen vrij · 1 dag ziek", Uren.vrijZiek(listOf(DagSoort.Vrij, DagSoort.Ziek, DagSoort.Vrij)))
        val staat = KlokStaat.maak(emptyList(), mapOf(vrijdag.minusDays(1) to DagSoort.Ziek), vrijdag.atTime(12, 0))
        assertEquals("1 dag ziek", staat.weekVrijZiek)
    }

    @Test
    fun `begin en eind van een dag`() {
        val dag = Dag(vrijdag, DagSoort.Werk, listOf(blok(vrijdag, "12:30", "16:00"), blok(vrijdag, "07:30", "12:00")))
        assertEquals(LocalTime.of(7, 30), dag.begin)
        assertEquals(LocalTime.of(16, 0), dag.eind)
    }
}
