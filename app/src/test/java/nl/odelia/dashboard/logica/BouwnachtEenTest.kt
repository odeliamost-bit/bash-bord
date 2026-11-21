package nl.odelia.dashboard.logica

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BouwnachtEenTest {
    private val dag = LocalDate.of(2026, 11, 20)
    private fun uit(id: String, pot: String, cent: Long) = Boeking(id, BoekSoort.Uit, cent, pot, "Boodschappen", dag)
    private fun bij(id: String, pot: String, cent: Long) = Boeking(id, BoekSoort.In, cent, pot, "Loon", dag)

    @Test
    fun alleenMeldingAlsPotjeNetOnderNulGaat() {
        val start = GeldData(boekingen = listOf(bij("1", "lekkers", 10_00), bij("2", "vrij", 50_00)))
        val minder = start.copy(boekingen = start.boekingen + uit("3", "lekkers", 5_00))
        assertTrue(PotjeAlarm.nieuwOnderNul(start, minder).isEmpty())
        val onder = minder.copy(boekingen = minder.boekingen + uit("4", "lekkers", 8_00))
        val alarm = PotjeAlarm.nieuwOnderNul(minder, onder)
        assertEquals(listOf("lekkers"), alarm.map { it.potje.id })
        assertTrue(alarm[0].advies, alarm[0].advies.contains("Vrije geld"))
        assertTrue(alarm[0].advies.contains("3,00"))
        // Nog verder onder nul: geen nieuwe melding.
        val verder = onder.copy(boekingen = onder.boekingen + uit("5", "lekkers", 1_00))
        assertTrue(PotjeAlarm.nieuwOnderNul(onder, verder).isEmpty())
    }

    @Test
    fun logboekNieuwsteBovenaanZonderDubbel() {
        val t = LocalDateTime.of(2026, 11, 21, 22, 0)
        var d = Logboek.voegToe(LogboekData(), LogRegel(t, "☁️", "Verstuurd: geld"))
        d = Logboek.voegToe(d, LogRegel(t.plusMinutes(1), "☁️", "Verstuurd: geld"))
        d = Logboek.voegToe(d, LogRegel(t.plusMinutes(2), "🔴", "Uitgeklokt"))
        assertEquals(listOf("Uitgeklokt", "Verstuurd: geld"), d.regels.map { it.tekst })
        assertEquals(d, Logboek.lees(Logboek.schrijf(d)))
        repeat(40) { d = Logboek.voegToe(d, LogRegel(t.plusHours(it.toLong()), "•", "r$it")) }
        assertEquals(Logboek.MAX, d.regels.size)
    }

    @Test
    fun apiKostenNieuweMaandBeginBijNul() {
        val nov = ApiKostenData(YearMonth.of(2026, 11), 4_50, 5_00)
        assertEquals(90, ApiKosten.procent(nov))
        assertTrue(ApiKosten.bericht(nov).startsWith("⚠️"))
        val dec = ApiKosten.voorMaand(nov, YearMonth.of(2026, 12))
        assertEquals(0L, dec.cent)
        assertEquals(5_00L, dec.limietCent)
        assertEquals(nov, ApiKosten.lees(ApiKosten.schrijf(nov)))
    }
}
