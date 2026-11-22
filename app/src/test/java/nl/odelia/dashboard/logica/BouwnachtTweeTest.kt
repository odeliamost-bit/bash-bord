package nl.odelia.dashboard.logica

import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BouwnachtTweeTest {
    private fun b(id: String, soort: BoekSoort, cent: Long, pot: String, dag: LocalDate, naar: String? = null) =
        Boeking(id, soort, cent, pot, "x", dag, naarPotjeId = naar)

    @Test
    fun vasteLastenWaarschuwing() {
        val vandaag = LocalDate.of(2026, 11, 22)
        val d = GeldData(
            boekingen = listOf(b("1", BoekSoort.In, 10_00, "vast", vandaag)),
            vasteLasten = listOf(VasteLast("t", "Telefoon", 12_50, 25, "vast"), VasteLast("z", "Zorg", 150_00, 1, "zorg")),
        )
        assertEquals(listOf(25, 1), Vooruit.komend(d.vasteLasten, vandaag).map { it.datum.dayOfMonth })
        val w = Vooruit.waarschuwingen(d, vandaag)
        assertEquals(setOf("vast", "zorg"), w.map { it.potje.id }.toSet())
        assertEquals(12_50L, w.first { it.potje.id == "vast" }.nodig)
    }

    @Test
    fun maandAfsluitenRestNaarSparen() {
        val nov = LocalDate.of(2026, 11, 10)
        val d = GeldData(boekingen = listOf(b("1", BoekSoort.In, 100_00, "vrij", nov), b("2", BoekSoort.Uit, 30_00, "vrij", nov)))
        assertNull(Vooruit.teAfsluiten(d, LocalDate.of(2026, 11, 15)))
        val m = Vooruit.teAfsluiten(d, LocalDate.of(2026, 12, 2))!!
        assertEquals(YearMonth.of(2026, 11), m)
        val a = Vooruit.afsluiting(d, m)
        assertEquals(70_00L, a.rest)
        val na = Vooruit.sluitAf(d, a, "spaar", "x", LocalDate.of(2026, 12, 2))
        assertEquals(70_00L, na.saldo("spaar"))
        assertEquals(0L, na.saldo("vrij"))
        assertNull(Vooruit.teAfsluiten(na, LocalDate.of(2026, 12, 3)))
        val rond = GeldJson.lees(GeldJson.schrijf(na))
        assertEquals(na.afgesloten, rond.afgesloten)
        assertEquals(na.vasteLasten, rond.vasteLasten)
    }

    @Test
    fun wensSparen() {
        val nu = YearMonth.of(2026, 11)
        val d = GeldData(boekingen = (1..3).map { b("$it", BoekSoort.In, 10_00, "spaar", nu.minusMonths(it.toLong()).atDay(5)) })
        val w = Wens("w", "Zeilboot", "⛵", WensSoort.Overig, null, emptyList(), prijsCent = 60_00, potjeId = "spaar")
        val s = Vooruit.sparen(w, d, nu)!!
        assertEquals(30_00L, s.saldo)
        assertEquals(3, s.maanden)
        assertTrue(Vooruit.sparenTekst(s, nu).contains("februari 2027"))
    }

    @Test
    fun nieuwsKoppen() {
        val rss = "<rss><channel><title>NOS</title><item><title><![CDATA[Kop één &amp; twee]]></title><link>https://nos.nl/a</link></item>" +
            "<item><title>Kop drie</title><link>http://x</link></item></channel></rss>"
        val k = Nieuws.lees(rss)
        assertEquals(listOf("Kop één & twee", "Kop drie"), k.map { it.titel })
        assertEquals("https://nos.nl/a", k[0].link)
        assertNull(k[1].link)
    }
}
