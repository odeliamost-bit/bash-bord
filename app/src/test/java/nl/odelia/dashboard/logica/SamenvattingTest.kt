package nl.odelia.dashboard.logica

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class SamenvattingTest {
    private val vrijdag = LocalDate.of(2026, 10, 23)

    @Test
    fun legeDag() {
        assertEquals("Er staat vandaag en deze week niks bijzonders. Fijne dag!", ochtendTekst(emptyList(), vrijdag))
    }

    @Test
    fun vandaagEnDezeWeek() {
        val maandag = LocalDate.of(2026, 10, 19)
        val items = listOf(
            Onthoud(tekst = "Tandarts bellen", wanneer = maandag),
            Onthoud(tekst = "Verslag inleveren", wanneer = maandag.plusDays(4)),
            Onthoud(tekst = "Formulier stage", wanneer = maandag.plusDays(3)),
            Onthoud(tekst = "Al klaar", wanneer = maandag, klaar = true),
            Onthoud(tekst = "Volgende week", wanneer = maandag.plusDays(8)),
            Onthoud(tekst = "Zonder datum"),
        )
        assertEquals(
            "Vandaag: Tandarts bellen\nDeze week: Formulier stage (do 22), Verslag inleveren (vr 23)",
            ochtendTekst(items, maandag),
        )
    }

    @Test
    fun teLaat() {
        val o = Overzicht.van(listOf(Onthoud(tekst = "Vergeten", wanneer = vrijdag.minusDays(2))), vrijdag)
        assertEquals(1, o.teLaat.size)
    }
}
