package nl.odelia.dashboard.onthoud

import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OnthoudTest {
    // Vrijdag 23 oktober 2026 (verzonnen voorbeelden, geen echte afspraken)
    private val vandaag = LocalDate.of(2026, 10, 23)
    private val jansen = Persoon("p1", "Dhr. Jansen", Groep.School, "leraar Nederlands")
    private val kim = Persoon("p2", "Kim", Groep.Werk, "Randstad")

    private fun afspraak(id: String, wat: String, deadline: LocalDate? = null, persoon: Persoon? = null, project: String? = null, klaar: Boolean = false, seintje: Boolean = false) =
        Afspraak(id, wat, persoonId = persoon?.id, project = project, deadline = deadline, klaar = klaar, seintje = seintje, gemaakt = LocalDateTime.of(2026, 10, 20, 9, 0))

    private val staat = OnthoudStaat(
        listOf(
            afspraak("a1", "Verslag inleveren", vandaag.plusDays(3), jansen, "PVB", seintje = true),
            afspraak("a2", "Uren sturen", vandaag, kim),
            afspraak("a3", "Café bellen", vandaag.minusDays(1)),
            afspraak("a4", "Boek meenemen", vandaag.plusDays(1), jansen, seintje = true),
            afspraak("a5", "Formulier stage", null, project = "PVB"),
            afspraak("a6", "Mail over toets", vandaag.minusDays(5), jansen, klaar = true),
        ),
        listOf(jansen, kim),
        vandaag,
    )

    @Test
    fun `zoeken kijkt naar wat, persoon, rol en project`() {
        assertEquals(listOf("a1"), staat.zoek("verslag").map { it.id })
        assertEquals(listOf("a2"), staat.zoek("randstad").map { it.id })
        assertEquals(listOf("a1", "a5"), staat.zoek("pvb").map { it.id })
        assertEquals(setOf("a1", "a4", "a6"), staat.zoek("jansen").map { it.id }.toSet())
    }

    @Test
    fun `zoeken op to-do`() {
        val metTodo = staat.copy(afspraken = staat.afspraken + afspraak("t1", "Fietsband plakken").copy(todo = true))
        assertEquals(listOf("t1"), metTodo.zoek("todo").map { it.id })
        assertEquals(listOf("t1"), metTodo.zoek("To-do fiets").map { it.id })
        assertTrue("t1" !in metTodo.zoek("afspraak").map { it.id })
    }

    @Test
    fun `zoeken let niet op hoofdletters en accenten, alle woorden moeten passen`() {
        assertEquals(listOf("a3"), staat.zoek("CAFE").map { it.id })
        assertEquals(listOf("a3"), staat.zoek("cafe").map { it.id })
        assertEquals(listOf("a1"), staat.zoek("jansen verslag").map { it.id })
        assertTrue(staat.zoek("   ").isEmpty())
    }

    @Test
    fun `klaar komt achteraan bij zoeken`() {
        assertEquals("a6", staat.zoek("jansen").last().id)
    }

    @Test
    fun `indelen in te laat, vandaag, morgen, deze week, later`() {
        val per = staat.afspraken.associate { it.id to staat.wanneer(it) }
        assertEquals(Wanneer.DezeWeek, staat.wanneer(staat.afspraken[0].copy(deadline = vandaag.plusDays(2)))) // zondag
        assertEquals(Wanneer.Later, per["a1"]) // maandag = volgende week
        assertEquals(Wanneer.Vandaag, per["a2"])
        assertEquals(Wanneer.TeLaat, per["a3"])
        assertEquals(Wanneer.Morgen, per["a4"])
        assertEquals(Wanneer.ZonderDatum, per["a5"])
        assertEquals(Wanneer.Klaar, per["a6"])
    }

    @Test
    fun `open lijst begint met de vroegste deadline`() {
        assertEquals(listOf("a3", "a2", "a4", "a1", "a5"), staat.open.map { it.id })
        assertEquals(listOf("a3", "a2"), staat.vandaagTeDoen.map { it.id })
        assertEquals(listOf("a4"), staat.restVanDeWeek.map { it.id })
    }

    @Test
    fun `seintje voor morgen alleen als het aan staat`() {
        assertEquals(listOf("a4"), staat.seintjeVoorMorgen.map { it.id })
    }

    @Test
    fun `samenvatting per persoon en project`() {
        val s = staat.samenvatting(staat.vanPersoon("p1"))
        assertEquals(2, s.open)
        assertEquals(1, s.klaar)
        assertEquals("a4", s.eerstvolgende?.id)
        assertEquals(listOf("PVB"), staat.projecten)
    }

    @Test
    fun `groep komt van de persoon als hij niet zelf gekozen is`() {
        assertEquals(Groep.School, staat.groep(staat.afspraken[0]))
        assertEquals(null, staat.groep(staat.afspraken[2]))
    }

    @Test
    fun `letter van een naam`() {
        assertEquals("J", jansen.letter)
        assertEquals("K", kim.letter)
        assertEquals("B", Persoon("x", "Mevr. de Boer", Groep.School).letter)
    }
}
