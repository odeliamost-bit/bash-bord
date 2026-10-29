package nl.odelia.dashboard.logica

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ToetsTest {
    @Test
    fun toetsenBlijvenBewaard() {
        val t = Toets("a", "elektro", "Wet van Ohm", LocalDate.of(2026, 11, 5), "U = I × R")
        val terug = LerenJson.lees(LerenJson.schrijf(LerenData(toetsen = listOf(t))))
        assertEquals(listOf(t), terug.toetsen)
        assertEquals(7L, t.dagenTot(LocalDate.of(2026, 10, 29)))
    }
}
