package nl.odelia.dashboard

import java.time.LocalDate
import nl.odelia.dashboard.logica.Beleggen
import nl.odelia.dashboard.logica.BeleggenData
import nl.odelia.dashboard.logica.BeleggenInstellingen
import nl.odelia.dashboard.logica.BeleggenJson
import nl.odelia.dashboard.logica.Koers
import nl.odelia.dashboard.logica.Order
import nl.odelia.dashboard.logica.Pot
import nl.odelia.dashboard.logica.Soort
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BeleggenTest {
    private val dag0 = LocalDate.of(2026, 1, 1)
    private fun reeks(prijzen: List<Double>) = prijzen.mapIndexed { i, p -> Koers(dag0.plusDays(i.toLong()), p) }
    private val inst = BeleggenInstellingen(kosten = 3.0, koersdoel = 37.5)

    @Test
    fun potRekentGoed() {
        val pot = Pot(startGeld = 400.0, orders = listOf(Order(datum = dag0, koop = true, aantal = 10, prijs = 30.0, kosten = 3.0)))
        assertEquals(10, pot.aantal)
        assertEquals(97.0, pot.geld, 0.001)
        assertEquals(30.3, pot.gemiddeld, 0.001)
        assertEquals(97.0 + 330.0, pot.waarde(33.0), 0.001)
        val verkocht = pot.copy(orders = pot.orders + Order(datum = dag0, koop = false, aantal = 5, prijs = 33.0, kosten = 3.0))
        assertEquals(5, verkocht.aantal)
        assertEquals(30.3, verkocht.gemiddeld, 0.001)
    }

    @Test
    fun startAantalTeltMee() {
        val pot = Pot(startAantal = 4, startPrijs = 30.0)
        assertEquals(4, pot.aantal)
        assertEquals(10.0, pot.resultaat(33.0)!!, 0.001)
    }

    @Test
    fun rsi() {
        assertNull(Beleggen.rsi(listOf(1.0, 2.0)))
        assertEquals(100.0, Beleggen.rsi((1..20).map { it.toDouble() })!!, 0.001)
        assertTrue(Beleggen.rsi((20 downTo 1).map { it.toDouble() })!! < 1)
    }

    @Test
    fun geenKoersGeenSignaal() {
        assertNull(Beleggen.signaal(emptyList(), Pot(), inst))
    }

    @Test
    fun stopVerliesGaatVoor() {
        val pot = Pot(startAantal = 10, startPrijs = 40.0)
        val s = Beleggen.signaal(reeks(listOf(35.0)), pot, inst)!!
        assertEquals(Soort.Verkopen, s.soort)
        assertEquals(10, s.aantal)
        assertTrue(s.kort.contains("Stop-verlies"))
    }

    @Test
    fun winstNemenVerkooptDeHelft() {
        val pot = Pot(startAantal = 10, startPrijs = 30.0)
        val s = Beleggen.signaal(reeks(listOf(35.0)), pot, inst)!!
        assertEquals(Soort.Verkopen, s.soort)
        assertEquals(5, s.aantal)
    }

    @Test
    fun kopenBijStijgendeTrendEnRuimte() {
        // Rustig omhoog van 28 naar ~31,5 met kleine dipjes: trend omhoog en >12% tot het doel.
        val prijzen = (0 until 60).map { 28.0 + it * 0.06 + (if (it % 2 == 0) -0.25 else 0.0) }
        val s = Beleggen.signaal(reeks(prijzen), Pot(startGeld = 400.0), inst)!!
        assertEquals(Soort.Kopen, s.soort)
        assertEquals(12, s.aantal) // (400-3)/31,5 = 12
        assertTrue(s.kort.length < 60) // past in een melding
    }

    @Test
    fun rustNaEenOrder() {
        val prijzen = (0 until 60).map { 28.0 + it * 0.06 + (if (it % 2 == 0) -0.25 else 0.0) }
        val k = reeks(prijzen)
        val pot = Pot(startGeld = 400.0, orders = listOf(Order(datum = k.last().datum.minusDays(2), koop = false, aantal = 0, prijs = 30.0, kosten = 0.0)))
        val s = Beleggen.signaal(k, pot, inst)!!
        assertEquals(Soort.Houden, s.soort)
    }

    @Test
    fun geenGeldIsHouden() {
        val prijzen = (0 until 60).map { 28.0 + it * 0.06 + (if (it % 2 == 0) -0.25 else 0.0) }
        val s = Beleggen.signaal(reeks(prijzen), Pot(startGeld = 10.0), inst)!!
        assertEquals(Soort.Houden, s.soort)
    }

    @Test
    fun testOpOudeKoersen() {
        assertNull(Beleggen.test(reeks(List(20) { 30.0 }), inst))
        val golf = (0 until 300).map { 32.0 + 3 * kotlin.math.sin(it / 15.0) }
        val t = Beleggen.test(reeks(golf), inst)
        assertNotNull(t)
        assertTrue(t!!.model > 0)
        assertTrue(t.orders >= 1)
    }

    @Test
    fun yahooLezen() {
        val json = """{"chart":{"result":[{"timestamp":[1762941600,1763028000,1763114400],
            "indicators":{"quote":[{"close":[35.3,null,35.6]}]}}],"error":null}}"""
        val k = Beleggen.leesYahoo(json)
        assertEquals(2, k.size)
        assertEquals(35.6, k.last().prijs, 0.001)
    }

    @Test
    fun jsonHeenEnTerug() {
        val d = BeleggenData(
            koersen = reeks(listOf(35.0, 35.3)),
            echt = Pot(startGeld = 50.0, startAantal = 3, startPrijs = 34.0),
            oefen = Pot(startGeld = 400.0, orders = listOf(Order(datum = dag0, koop = true, aantal = 2, prijs = 35.0, kosten = 3.0))),
            instellingen = inst.copy(kosten = 2.0),
        )
        assertEquals(d, BeleggenJson.lees(BeleggenJson.schrijf(d)))
        assertEquals(BeleggenData(), BeleggenJson.lees("kapot"))
    }

    @Test
    fun eenKoersPerDag() {
        val d = BeleggenData().metKoers(Koers(dag0, 30.0)).metKoers(Koers(dag0, 31.0))
        assertEquals(1, d.koersen.size)
        assertEquals(31.0, d.laatste!!.prijs, 0.001)
    }
}
