package nl.odelia.dashboard.logica

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeldTest {
    private val zaterdag = LocalDate.of(2026, 10, 24)

    // Verzonnen voorbeeld, zoals in het ontwerp.
    private val potjes = listOf(
        Potje("zorg", "Zorg", "🩺", vastCent = 180_00),
        Potje("vast", "Vaste lasten", "📱", vastCent = 41_30),
        Potje("inv", "Investeren", "📈", procent = 30),
        Potje("vrij", "Vrije geld", "👛", procent = 20, startCent = 10_00),
        Potje("nood", "Noodpot", "🛟", procent = 20, echteRekening = true),
        Potje("spaar", "Spaarpot", "🐷", procent = 20, echteRekening = true),
        Potje("kerk", "Goede doelen/kerk", "⛪", procent = 10),
    )

    private fun uit(cent: Long, cat: String, dag: LocalDate = zaterdag, potje: String = "vrij") =
        Boeking("u$cent$cat$dag", BoekSoort.Uit, cent, potje, cat, dag)

    @Test
    fun verdelen_zoalsHetOntwerp() {
        val v = Geld.verdeel(1000_00, potjes)
        assertEquals(778_70, v.rest)
        assertEquals(180_00L, v.perPotje["zorg"])
        assertEquals(41_30L, v.perPotje["vast"])
        assertEquals(233_61L, v.perPotje["inv"])
        assertEquals(155_74L, v.perPotje["vrij"])
        assertEquals(77_87L, v.perPotje["kerk"])
        assertEquals(1000_00L, v.perPotje.values.sum())
        assertEquals(0L, v.over)
        assertTrue(v.klopt)
    }

    @Test
    fun verdelen_losseCentenRakenNietZoek() {
        val drie = listOf(Potje("a", "A", "", procent = 33), Potje("b", "B", "", procent = 33), Potje("c", "C", "", procent = 34))
        val v = Geld.verdeel(1_00, drie)
        assertEquals(1_00L, v.perPotje.values.sum())
    }

    @Test
    fun verdelen_onder100Procent_restBlijftOver() {
        val v = Geld.verdeel(100_00, listOf(Potje("a", "A", "", procent = 60)))
        assertEquals(60_00L, v.perPotje["a"])
        assertEquals(40_00L, v.over)
        assertFalse(v.klopt)
    }

    @Test
    fun verdelen_boven100Procent_nooitMeerDanErIs() {
        val v = Geld.verdeel(100_00, listOf(Potje("a", "A", "", procent = 80), Potje("b", "B", "", procent = 80)))
        assertEquals(100_00L, v.perPotje.values.sum())
    }

    @Test
    fun verdelen_teWeinigVoorVasteBedragen() {
        val v = Geld.verdeel(200_00, potjes)
        assertEquals(180_00L, v.perPotje["zorg"])
        assertEquals(20_00L, v.perPotje["vast"])
        assertEquals(0L, v.rest)
        assertEquals(200_00L, v.perPotje.values.sum())
    }

    @Test
    fun saldo_metStartUitInEnOverboeken() {
        val b = listOf(
            uit(3_50, "lekkers"),
            Boeking("i", BoekSoort.In, 50_00, "vrij", "loon", zaterdag),
            Boeking("o", BoekSoort.Over, 20_00, "vrij", "spaar", zaterdag, naarPotjeId = "spaar"),
        )
        val vrij = potjes.first { it.id == "vrij" }
        val spaar = potjes.first { it.id == "spaar" }
        assertEquals(10_00 - 3_50 + 50_00 - 20_00L, Geld.saldo(vrij, b))
        assertEquals(20_00L, Geld.saldo(spaar, b))
        // Overboeken verandert het totaal niet.
        assertEquals(10_00 - 3_50 + 50_00L, Geld.totaal(potjes, b))
    }

    @Test
    fun week_isMaandagTotZondag() {
        val r = Geld.bereik(GeldPeriode.Week, zaterdag)
        assertEquals(LocalDate.of(2026, 10, 19), r.start)
        assertEquals(LocalDate.of(2026, 10, 25), r.endInclusive)
        assertEquals(LocalDate.of(2026, 10, 31), Geld.bereik(GeldPeriode.Maand, zaterdag).endInclusive)
    }

    @Test
    fun perCategorie_grootsteEerst_procentenSamen100() {
        val b = listOf(
            uit(1_00, "lekkers"), uit(1_00, "bood"), uit(1_00, "abo"),
            uit(5_00, "bood", zaterdag.minusDays(7)), // vorige week: telt niet mee
            Boeking("i", BoekSoort.In, 99_00, "vrij", "loon", zaterdag), // inkomen: telt niet mee
        )
        val l = Geld.perCategorie(b, GeldPeriode.Week, zaterdag)
        assertEquals(3, l.size)
        assertEquals(100, l.sumOf { it.procent })
        assertEquals(3_00L, l.sumOf { it.cent })
        assertTrue(Geld.perCategorie(emptyList(), GeldPeriode.Dag, zaterdag).isEmpty())
    }

    @Test
    fun bedrag_inNederlandseSchrijfwijze() {
        assertEquals("€ 4.251,72", Geld.bedrag(4251_72))
        assertEquals("€ 0,05", Geld.bedrag(5))
        assertEquals("€ 1.000.000,00", Geld.bedrag(1_000_000_00))
        assertEquals("− € 3,50", Geld.bedrag(-3_50))
    }

    @Test
    fun leesBedrag_veelSchrijfwijzen() {
        assertEquals(5_00L, Geld.leesBedrag("5"))
        assertEquals(5_50L, Geld.leesBedrag("5,5"))
        assertEquals(5_50L, Geld.leesBedrag("5,50"))
        assertEquals(5_50L, Geld.leesBedrag("5.50"))
        assertEquals(1000_00L, Geld.leesBedrag("1.000"))
        assertEquals(1000_00L, Geld.leesBedrag("1.000,00"))
        assertEquals(12_00L, Geld.leesBedrag("€ 12"))
        assertEquals(50L, Geld.leesBedrag(",5"))
        assertNull(Geld.leesBedrag(""))
        assertNull(Geld.leesBedrag("vijf"))
        assertNull(Geld.leesBedrag("5,505"))
        assertNull(Geld.leesBedrag("1,2,3"))
        assertNull(Geld.leesBedrag("-5"))
    }

    @Test
    fun geheugensteuntje_alleenBijEchteRekening() {
        val naarSpaar = Boeking("o", BoekSoort.Over, 25_00, "vrij", "spaar", zaterdag, naarPotjeId = "spaar")
        val tekst = Geld.geheugensteuntje(naarSpaar, potjes)
        assertTrue(tekst!!.contains("€ 25,00"))
        assertTrue(tekst.contains("Spaarpot"))
        val inApp = Boeking("o2", BoekSoort.Over, 25_00, "vrij", "spaar", zaterdag, naarPotjeId = "inv")
        assertNull(Geld.geheugensteuntje(inApp, potjes))
        assertNull(Geld.geheugensteuntje(uit(1_00, "lekkers"), potjes))
    }

    @Test
    fun csv_kopRegelEnVeiligeTekst() {
        val b = listOf(
            uit(1234_50, "lekkers").copy(notitie = "=HYPERLINK(\"x\")", tags = listOf("School")),
            Boeking("i", BoekSoort.In, 50_00, "vrij", "loon", zaterdag.minusDays(1)),
        )
        val regels = Geld.csv(b, potjes).trimEnd().split("\r\n")
        assertEquals("datum,soort,bedrag,categorie,potje,naar,tags,notitie", regels[0])
        assertEquals("2026-10-23,inkomen,\"50,00\",\"Loon\",\"Vrije geld\",\"\",\"\",\"\"", regels[1])
        assertEquals("2026-10-24,uitgave,\"-1234,50\",\"Lekkers\",\"Vrije geld\",\"\",\"School\",\"'=HYPERLINK(\"\"x\"\")\"", regels[2])
    }
}
