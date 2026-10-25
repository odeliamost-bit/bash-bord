package nl.odelia.dashboard.logica

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UrenTest {
    private val vrijdag = LocalDate.of(2026, 10, 23)
    private fun op(dag: LocalDate, u: Int, m: Int) = dag.atTime(u, m)

    @Test
    fun gewoneWerkdag_isZevenEnEenKwartUur() {
        val p = listOf(Periode(op(vrijdag, 7, 45), op(vrijdag, 15, 45)))
        val dag = Uren.werkdag(p, emptyMap(), vrijdag, op(vrijdag, 20, 0))
        assertEquals(7.25f, dag.uren, 0.001f)
        assertEquals(DagSoort.Gewerkt, dag.soort)
        assertEquals(LocalTime.of(7, 45), dag.begin)
        assertEquals(LocalTime.of(15, 45), dag.eind)
        assertEquals("7¼", alsBreuk(dag.uren))
    }

    @Test
    fun tweeKeerKlokken_pauzeMaarEenKeerEraf() {
        val p = listOf(
            Periode(op(vrijdag, 7, 30), op(vrijdag, 12, 0)),
            Periode(op(vrijdag, 12, 30), op(vrijdag, 16, 0)),
        )
        assertEquals(7.25f, Uren.werkdag(p, emptyMap(), vrijdag, op(vrijdag, 20, 0)).uren, 0.001f)
    }

    @Test
    fun nogBezig_teltTotNu() {
        val p = listOf(Periode(op(vrijdag, 7, 45), null))
        val dag = Uren.werkdag(p, emptyMap(), vrijdag, op(vrijdag, 13, 15))
        assertEquals(4.75f, dag.uren, 0.001f)
        assertEquals(true, dag.bezig)
        assertNull(dag.eind)
    }

    @Test
    fun korterDanDePauze_isNul() {
        val p = listOf(Periode(op(vrijdag, 8, 0), op(vrijdag, 8, 30)))
        assertEquals(0f, Uren.werkdag(p, emptyMap(), vrijdag, op(vrijdag, 9, 0)).uren, 0.001f)
    }

    @Test
    fun vrijeDag_teltNiet() {
        val p = listOf(Periode(op(vrijdag, 7, 45), op(vrijdag, 15, 45)))
        val dag = Uren.werkdag(p, mapOf(vrijdag to DagSoort.Vrij), vrijdag, op(vrijdag, 20, 0))
        assertEquals(DagSoort.Vrij, dag.soort)
        assertEquals(0f, dag.uren, 0.001f)
    }

    @Test
    fun vergetenUitTeKlokken_teltNietEnWordtGevonden() {
        val donderdag = vrijdag.minusDays(1)
        val p = listOf(Periode(op(donderdag, 7, 45), null))
        val nu = op(vrijdag, 8, 0)
        assertEquals(0f, Uren.werkdag(p, emptyMap(), donderdag, nu).uren, 0.001f)
        assertEquals(op(donderdag, 7, 45), Uren.vergeten(p, nu)?.begin)
    }

    @Test
    fun werkweek_isMaandagTotVrijdag() {
        val week = Uren.werkweek(emptyList(), emptyMap(), vrijdag, op(vrijdag, 9, 0))
        assertEquals(5, week.size)
        assertEquals(LocalDate.of(2026, 10, 19), week.first().datum)
        assertEquals(vrijdag, week.last().datum)
    }

    @Test
    fun weekTotaal_teltAlleDagenOp() {
        val p = (19..23).map { d ->
            val dag = LocalDate.of(2026, 10, d)
            Periode(op(dag, 7, 45), op(dag, 15, 45))
        }
        assertEquals(36.25f, Uren.weekTotaal(p, emptyMap(), vrijdag, LocalDateTime.of(2026, 10, 24, 9, 0)), 0.001f)
    }

    @Test
    fun breuken() {
        assertEquals("34¼", alsBreuk(34.25f))
        assertEquals("7", alsBreuk(7f))
        assertEquals("5½", alsBreuk(5.5f))
        assertEquals("¾", alsBreuk(0.75f))
        assertEquals("0", alsBreuk(0f))
        assertEquals("7:05", alsKlok(LocalTime.of(7, 5)))
    }
}
