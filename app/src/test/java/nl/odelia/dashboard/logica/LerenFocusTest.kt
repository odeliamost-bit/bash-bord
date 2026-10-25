package nl.odelia.dashboard.logica

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LerenFocusTest {
    private val vandaag = LocalDate.of(2026, 10, 25)
    private val start = Instant.parse("2026-10-25T13:00:00Z")
    private fun min(m: Long) = start.plus(Duration.ofMinutes(m))

    @Test
    fun doel_afvinkenEnWeer_uit_alleenVoorVandaag() {
        var d = LerenData()
        d = Doelen.wissel(d, "lezen", vandaag)
        assertTrue(Doelen.isKlaar(d, "lezen", vandaag))
        assertFalse(Doelen.isKlaar(d, "lezen", vandaag.plusDays(1)))
        assertEquals(1, Doelen.aantalKlaar(d, vandaag))
        d = Doelen.wissel(d, "lezen", vandaag)
        assertFalse(Doelen.isKlaar(d, "lezen", vandaag))
        assertEquals(emptyMap<LocalDate, Set<String>>(), d.doelenKlaar)
    }

    @Test
    fun doel_oudeDagenVergeten_enSterBijDoelOfOefenen() {
        var d = LerenData(doelenKlaar = mapOf(vandaag.minusDays(30) to setOf("lezen")), geoefend = mapOf(vandaag.minusDays(1) to 3))
        d = Doelen.wissel(d, "opdracht", vandaag)
        assertEquals(setOf(vandaag), d.doelenKlaar.keys)
        assertTrue(Doelen.geleerd(d, vandaag))
        assertTrue(Doelen.geleerd(d, vandaag.minusDays(1)))
        assertFalse(Doelen.geleerd(d, vandaag.minusDays(2)))
    }

    @Test
    fun doelen_jsonHeenEnTerug() {
        val d = LerenData(doelenKlaar = mapOf(vandaag to setOf("lezen", "kaartjes")))
        assertEquals(d, LerenJson.lees(LerenJson.schrijf(d)))
    }

    @Test
    fun focus_lopen_stilzetten_verder() {
        var k = FocusKlok().start(start)
        assertEquals(Duration.ofMinutes(13), k.overOp(min(7)))
        k = k.stil(min(7))
        assertFalse(k.loopt)
        assertEquals(Duration.ofMinutes(13), k.overOp(min(60)))
        k = k.start(min(60))
        assertEquals(Duration.ofMinutes(3), k.overOp(min(70)))
        assertEquals(0.15f, k.deel(min(70)), 0.001f)
    }

    @Test
    fun focus_naLerenVanzelfPauze_daarnaKlaarVoorNieuweRonde() {
        val k = FocusKlok().start(start)
        assertNull(k.bijwerken(min(19)).second)
        val (pauze, klaar) = k.bijwerken(min(21))
        assertEquals(FocusFase.Leren, klaar)
        assertEquals(FocusFase.Pauze, pauze.fase)
        assertEquals(Duration.ofMinutes(4), pauze.overOp(min(21)))
        val (weer, klaar2) = pauze.bijwerken(min(26))
        assertEquals(FocusFase.Pauze, klaar2)
        assertEquals(FocusKlok(), weer)
    }

    @Test
    fun focus_langWeg_slaatPauzeOver() {
        val (k, klaar) = FocusKlok().start(start).bijwerken(min(90))
        assertEquals(FocusKlok(), k)
        assertEquals(FocusFase.Pauze, klaar)
    }

    @Test
    fun focus_tekst() {
        assertEquals("20:00", FocusKlok.alsTekst(Duration.ofMinutes(20)))
        assertEquals("0:01", FocusKlok.alsTekst(Duration.ofMillis(200)))
        assertEquals("12:59", FocusKlok.alsTekst(Duration.ofSeconds(779)))
    }
}
