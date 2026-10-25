package nl.odelia.dashboard.logica

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HerkenTest {
    private val vrijdag = LocalDate.of(2026, 10, 23)

    @Test
    fun woorden() {
        assertEquals(vrijdag, Herken.datum("vandaag tandarts bellen", vrijdag))
        assertEquals(vrijdag.plusDays(1), Herken.datum("Morgen boek meenemen", vrijdag))
        assertEquals(vrijdag.plusDays(2), Herken.datum("overmorgen", vrijdag))
        assertNull(Herken.datum("goedemorgen allemaal", vrijdag))
    }

    @Test
    fun dagnamen() {
        assertEquals(LocalDate.of(2026, 10, 26), Herken.datum("verslag inleveren maandag", vrijdag))
        assertEquals(LocalDate.of(2026, 10, 30), Herken.datum("vrijdag uren sturen", vrijdag))
        val woensdag = LocalDate.of(2026, 10, 21)
        assertEquals(LocalDate.of(2026, 10, 23), Herken.datum("vrijdag", woensdag))
        assertEquals(LocalDate.of(2026, 10, 30), Herken.datum("volgende week vrijdag", woensdag))
    }

    @Test
    fun datums() {
        assertEquals(LocalDate.of(2026, 10, 29), Herken.datum("formulier voor 29 oktober", vrijdag))
        assertEquals(LocalDate.of(2026, 11, 2), Herken.datum("bloed prikken 2 nov", vrijdag))
        assertEquals(LocalDate.of(2027, 1, 5), Herken.datum("5 januari", vrijdag))
        assertEquals(LocalDate.of(2026, 11, 12), Herken.datum("op 12-11", vrijdag))
        assertNull(Herken.datum("bel om 3 uur", vrijdag))
        assertNull(Herken.datum("over 3 maanden", vrijdag))
    }

    @Test
    fun wieEnWat() {
        assertEquals("Kim", Herken.uitLijst("kim bellen over de uren", listOf("Kim", "Mama")))
        assertEquals("Dhr. Jansen", Herken.uitLijst("verslag voor dhr. jansen", listOf("Jansen", "Dhr. Jansen")))
        assertNull(Herken.uitLijst("niemand", listOf("Kim")))
        assertEquals("Bellen", Herken.soort("Tandarts bellen"))
        assertEquals("Inleveren", Herken.soort("verslag inleveren voor maandag"))
        assertNull(Herken.soort("iets anders"))
    }
}
