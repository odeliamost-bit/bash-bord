package nl.odelia.dashboard.logica

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NuDoenTest {
    private val vandaag = LocalDate.of(2026, 11, 13) // vrijdag

    @Test
    fun niksAlsErNiksVoorNuIs() {
        assertNull(NuDoen.van(emptyList(), vandaag))
        val later = listOf(
            Onthoud(tekst = "Morgen", wanneer = vandaag.plusDays(1)),
            Onthoud(tekst = "Zonder datum"),
            Onthoud(tekst = "Al klaar", wanneer = vandaag, klaar = true),
        )
        assertNull(NuDoen.van(later, vandaag))
    }

    @Test
    fun teLaatGaatVoorOudsteEerst() {
        val items = listOf(
            Onthoud(tekst = "Vandaag", wanneer = vandaag),
            Onthoud(tekst = "Gisteren", wanneer = vandaag.minusDays(1)),
            Onthoud(tekst = "Maandag", wanneer = vandaag.minusDays(4)),
        )
        val n = NuDoen.van(items, vandaag)!!
        assertEquals("Maandag", n.item.titel)
        assertEquals(true, n.teLaat)
        assertEquals(2, n.daarna)
        assertEquals("te laat (sinds ma 9)", n.wanneerTekst)
    }

    @Test
    fun andersVandaag() {
        val n = NuDoen.van(listOf(Onthoud(tekst = "Tandarts bellen", wanneer = vandaag)), vandaag)!!
        assertEquals(false, n.teLaat)
        assertEquals(0, n.daarna)
        assertEquals("Nu doen: Tandarts bellen. Dat is vandaag.", n.voorlees)
    }
}
