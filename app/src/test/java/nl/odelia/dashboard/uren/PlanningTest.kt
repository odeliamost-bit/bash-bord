package nl.odelia.dashboard.uren

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class PlanningTest {
    @Test
    fun `voor 8_15 op een werkdag is het vandaag`() {
        val dinsdag = LocalDateTime.of(2026, 10, 20, 7, 0)
        assertEquals(LocalDateTime.of(2026, 10, 20, 8, 15), Planning.volgende(dinsdag, Planning.INKLOKKEN))
    }

    @Test
    fun `na 8_15 is het morgen`() {
        val dinsdag = LocalDateTime.of(2026, 10, 20, 8, 15)
        assertEquals(LocalDateTime.of(2026, 10, 21, 8, 15), Planning.volgende(dinsdag, Planning.INKLOKKEN))
    }

    @Test
    fun `vrijdagmiddag na 16_15 is het maandag`() {
        val vrijdag = LocalDateTime.of(2026, 10, 23, 17, 0)
        assertEquals(LocalDateTime.of(2026, 10, 26, 16, 15), Planning.volgende(vrijdag, Planning.UITKLOKKEN))
    }

    @Test
    fun `in het weekend is het maandag`() {
        val zaterdag = LocalDateTime.of(2026, 10, 24, 6, 0)
        assertEquals(LocalDateTime.of(2026, 10, 26, 8, 15), Planning.volgende(zaterdag, Planning.INKLOKKEN))
    }
}
