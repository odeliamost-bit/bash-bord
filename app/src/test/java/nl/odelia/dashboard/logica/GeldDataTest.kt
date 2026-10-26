package nl.odelia.dashboard.logica

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeldDataTest {
    private val dag = LocalDate.of(2026, 10, 24)

    @Test
    fun voorbeeldPotjes_procentenSamen100() {
        assertEquals(100, GeldStart.potjes.filter { it.vastCent == null }.sumOf { it.procent ?: 0 })
        assertEquals(GeldStart.potjes.size, GeldStart.potjes.map { it.id }.toSet().size)
    }

    @Test
    fun json_heenEnTerug() {
        val d = GeldData(
            boekingen = listOf(
                Boeking("a", BoekSoort.Uit, 3_50, "vrij", "lekkers", dag, tags = listOf("School"), notitie = "Ah \"winkel\""),
                Boeking("b", BoekSoort.Over, 20_00, "vrij", "spaar", dag, naarPotjeId = "spaar"),
            ),
            tags = listOf("School", "Eigen"),
        )
        assertEquals(d, GeldJson.lees(GeldJson.schrijf(d)))
    }

    @Test
    fun json_legeOfKapotteTekst_geeftVoorbeeld() {
        assertEquals(GeldStart.potjes, GeldJson.lees("").potjes)
        assertEquals(GeldStart.potjes, GeldJson.lees("{}").potjes)
        // Bewust alle potjes weggehaald: dan komen de voorbeelden niet terug.
        assertTrue(GeldJson.lees("""{"potjes":[]}""").potjes.isEmpty())
    }

    @Test
    fun json_kapotStukjeWordtOvergeslagen() {
        val tekst = """{"potjes":[{"id":"a","naam":"A"},{"naam":"zonder id"}],
            "boekingen":[{"id":"x","soort":"Uit","cent":100,"potje":"a","categorie":"lekkers","datum":"2026-10-24"},
                         {"id":"y","soort":"Raar","cent":100,"potje":"a","categorie":"lekkers","datum":"2026-10-24"},
                         {"id":"z","soort":"Uit","cent":-5,"potje":"a","categorie":"lekkers","datum":"2026-10-24"}]}"""
        val d = GeldJson.lees(tekst)
        assertEquals(listOf("a"), d.potjes.map { it.id })
        assertEquals(listOf("x"), d.boekingen.map { it.id })
    }

    @Test
    fun zetSaldo_maaktHetSaldoPrecies() {
        val d = GeldBoek.voegToe(GeldData(), Boeking("a", BoekSoort.Uit, 7_25, "vrij", "lekkers", dag))
        val n = GeldBoek.zetSaldo(d, "vrij", 100_00)
        assertEquals(100_00L, n.saldo("vrij"))
    }

    @Test
    fun potjeWeg_alleenZonderBoekingen() {
        val d = GeldBoek.voegToe(GeldData(), Boeking("a", BoekSoort.Uit, 1_00, "vrij", "lekkers", dag))
        assertFalse(GeldBoek.magWeg(d, "vrij"))
        assertEquals(d, GeldBoek.haalPotjeWeg(d, "vrij"))
        assertTrue(GeldBoek.haalPotjeWeg(d, "kerk").potjes.none { it.id == "kerk" })
    }

    @Test
    fun verdelen_boekingenTellenOpTotHetBedrag_enTotaalKlopt() {
        val d = GeldData()
        val v = Geld.verdeel(1000_00, d.potjes)
        var n = 0
        val b = GeldBoek.verdeelBoekingen(v, dag, id = { "v${n++}" })
        assertEquals(1000_00L, b.sumOf { it.cent })
        assertEquals(1000_00L, d.copy(boekingen = b).totaal)
        assertTrue(b.all { it.soort == BoekSoort.In })
    }

    @Test
    fun overmaken_alleenEchteRekeningen() {
        val d = GeldData()
        val regels = Geld.overmaken(Geld.verdeel(1000_00, d.potjes), d.potjes)
        assertEquals(2, regels.size)
        assertTrue(regels.any { it.contains(GeldStart.ING_SPAAR) })
        assertTrue(regels.any { it.contains(GeldStart.GIRO) })
    }

    @Test
    fun laatstePotje_onthoudtKeuze() {
        val d = GeldData()
        assertEquals("vrij", GeldBoek.laatstePotje(d, BoekSoort.Uit))
        assertEquals("inkomsten", GeldBoek.laatstePotje(d, BoekSoort.In))
        val n = GeldBoek.voegToe(d, Boeking("a", BoekSoort.Uit, 1_00, "lekkers", "lekkers", dag))
        assertEquals("lekkers", GeldBoek.laatstePotje(n, BoekSoort.Uit))
    }

    @Test
    fun uitgavenPerDag_nieuwsteEerst_alleenUitgaven() {
        val b = listOf(
            Boeking("a", BoekSoort.Uit, 1_00, "vrij", "lekkers", dag.minusDays(2)),
            Boeking("b", BoekSoort.Uit, 2_00, "vrij", "bood", dag),
            Boeking("c", BoekSoort.In, 9_00, "vrij", "loon", dag),
        )
        val l = GeldBoek.uitgavenPerDag(b, GeldPeriode.Week, dag)
        assertEquals(listOf(dag, dag.minusDays(2)), l.map { it.datum })
        assertEquals(listOf("b"), l[0].boekingen.map { it.id })
    }

    @Test
    fun uitspraak_voorVoorlezen() {
        assertEquals("4251 euro en 72 cent", GeldBoek.uitspraak(4251_72))
        assertEquals("5 euro", GeldBoek.uitspraak(5_00))
        assertEquals("50 cent", GeldBoek.uitspraak(50))
        assertEquals("min 3 euro en 50 cent", GeldBoek.uitspraak(-3_50))
    }

    @Test
    fun tag_geenDubbele() {
        val d = GeldBoek.voegTagToe(GeldData(), "school")
        assertEquals(GeldStart.tags, d.tags)
        assertEquals(GeldStart.tags + "Kerst", GeldBoek.voegTagToe(d, " Kerst ").tags)
    }

    @Test
    fun verdelenVanuitPotje_isOverboeken_totaalBlijftGelijk() {
        val d = GeldBoek.zetSaldo(GeldData(), "vrij", 500_00)
        val v = Geld.verdeel(500_00, d.potjes)
        val b = GeldBoek.verdeelBoekingen(v, dag, van = "vrij")
        assertTrue(b.all { it.soort == BoekSoort.Over && it.potjeId == "vrij" && it.naarPotjeId != "vrij" })
        val n = d.copy(boekingen = b)
        assertEquals(500_00L, n.totaal)
        assertEquals(v.perPotje["vrij"], n.saldo("vrij"))
    }

    @Test
    fun teksten() {
        assertEquals("vandaag", GeldBoek.dagTekst(dag, dag))
        assertEquals("gisteren", GeldBoek.dagTekst(dag.minusDays(1), dag))
        assertEquals("woensdag 21 okt", GeldBoek.dagTekst(dag.minusDays(3), dag))
        assertEquals("19 okt – 25 okt", GeldBoek.periodeTekst(GeldPeriode.Week, dag))
        assertEquals("oktober 2026", GeldBoek.periodeTekst(GeldPeriode.Maand, dag))
        assertEquals("zaterdag 24 okt", GeldBoek.periodeTekst(GeldPeriode.Dag, dag))
    }

    @Test
    fun verdelenNieuwGeld_restGaatNaarLosPotje() {
        val d = GeldData(potjes = listOf(Potje("in", "Inkomsten", ""), Potje("a", "A", "", procent = 60)))
        val v = Geld.verdeel(100_00, d.potjes)
        val b = GeldBoek.verdeelBoekingen(v, dag, restNaar = "in")
        val n = d.copy(boekingen = b)
        assertEquals(100_00L, n.totaal)
        assertEquals(40_00L, n.saldo("in"))
        assertEquals(listOf("in"), GeldBoek.lossePotjes(d).map { it.id })
    }

    @Test
    fun ingesproken_bedragEnNotitie() {
        assertEquals(5_50L to "bij de Ah", GeldBoek.uitGesproken("5,50 euro bij de Ah"))
        assertEquals(12_00L to "Lidl", GeldBoek.uitGesproken("Lidl 12 euro"))
        assertEquals(3_25L to "snoep", GeldBoek.uitGesproken("snoep 3.25"))
        assertEquals(null to "snoep bij de Ah", GeldBoek.uitGesproken(" snoep bij de Ah "))
    }
}
