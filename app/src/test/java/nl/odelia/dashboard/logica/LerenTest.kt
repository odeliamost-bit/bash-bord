package nl.odelia.dashboard.logica

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LerenTest {
    private val vandaag = LocalDate.of(2026, 10, 25)

    // ---- Bakjes ----

    @Test
    fun wistIk_schuiftEenBakjeOp_enKomtLaterTerug() {
        val s1 = Leren.naAntwoord(null, wist = true, vandaag)
        assertEquals(2, s1.bakje)
        assertEquals(vandaag.plusDays(1), s1.terug)
        val s2 = Leren.naAntwoord(s1, wist = true, vandaag)
        assertEquals(KaartStand(3, vandaag.plusDays(3)), s2)
        assertEquals(KaartStand(4, vandaag.plusDays(7)), Leren.naAntwoord(s2, true, vandaag))
        assertEquals(KaartStand(5, vandaag.plusDays(14)), Leren.naAntwoord(KaartStand(4, vandaag), true, vandaag))
        assertEquals(KaartStand(5, vandaag.plusDays(14)), Leren.naAntwoord(KaartStand(5, vandaag), true, vandaag))
    }

    @Test
    fun nogOefenen_terugNaarBakje1_vandaagNogKlaar() {
        val s = Leren.naAntwoord(KaartStand(4, vandaag), wist = false, vandaag)
        assertEquals(KaartStand(1, vandaag), s)
        val data = LerenData(standen = mapOf("r1" to s))
        assertTrue(Leren.isKlaar(data, "r1", vandaag))
    }

    @Test
    fun nieuweKaart_isMeteenKlaar_enLatereNiet() {
        val data = LerenData(standen = mapOf("r1" to KaartStand(3, vandaag.plusDays(2))))
        assertTrue(Leren.isKlaar(data, "r2", vandaag))
        assertFalse(Leren.isKlaar(data, "r1", vandaag))
        assertTrue(Leren.isKlaar(data, "r1", vandaag.plusDays(2)))
    }

    @Test
    fun geoefend_teltPerDag_enVergeetOudeDagen() {
        var d = LerenData(geoefend = mapOf(vandaag.minusDays(20) to 9))
        d = Leren.geoefend(d, "r1", true, vandaag)
        d = Leren.geoefend(d, "r2", false, vandaag)
        assertEquals(mapOf(vandaag to 2), d.geoefend)
        assertEquals(2, d.standen["r1"]!!.bakje)
    }

    // ---- Rondje ----

    @Test
    fun rondje_laagsteBakjesEerst_enHoogstens12() {
        val data = LerenData(standen = mapOf("r1" to KaartStand(3, vandaag), "r2" to KaartStand(2, vandaag)))
        val r = Leren.nieuwRondje(data, "relais", vandaag, alles = false, kans = Random(1))
        assertEquals(8, r.totaal)
        val bakjes = r.rij.map { Leren.bakje(data, it) }
        assertEquals(bakjes.sorted(), bakjes)
        assertEquals("r1", r.rij.last())
    }

    @Test
    fun rondje_slaatKaartenOverDieNogNietTerugMoeten() {
        val data = LerenData(standen = (1..8).associate { "r$it" to KaartStand(3, vandaag.plusDays(3)) } - "r5")
        assertEquals(listOf("r5"), Leren.nieuwRondje(data, "relais", vandaag, alles = false).rij)
        assertEquals(8, Leren.nieuwRondje(data, "relais", vandaag, alles = true).totaal)
    }

    @Test
    fun nogOefenen_komtOverDrieKaartenTerug() {
        var r = Rondje(listOf("a", "b", "c", "d", "e"), totaal = 5)
        r = r.antwoord(wist = false)
        assertEquals(listOf("b", "c", "d", "a", "e"), r.rij)
        assertEquals(1, r.nogOefenen)
        r = Rondje(listOf("a", "b"), totaal = 2).antwoord(false)
        assertEquals(listOf("b", "a"), r.rij)
        r = Rondje(listOf("a"), totaal = 1).antwoord(false).antwoord(true)
        assertTrue(r.klaar)
        assertEquals(1, r.goed)
    }

    // ---- Quiz ----

    @Test
    fun getallen_metKommaOfPunt() {
        assertEquals(0.5, Leren.leesGetal("0,5")!!, 0.0)
        assertEquals(115.0, Leren.leesGetal(" 115 ")!!, 0.0)
        assertEquals(2300.0, Leren.leesGetal("2 300")!!, 0.0)
        assertNull(Leren.leesGetal("vijf"))
        assertNull(Leren.leesGetal(""))
        assertEquals("0,5", Leren.alsGetal(0.5))
        assertEquals("3", Leren.alsGetal(3.0))
    }

    @Test
    fun rekenvraag_binnenEenProcentIsGoed() {
        val v = Rekenvraag("x", "ohm", "?", 115.0, "W", "")
        assertTrue(Leren.klopt(v, 115.0))
        assertTrue(Leren.klopt(v, 115.5))
        assertFalse(Leren.klopt(v, 120.0))
    }

    @Test
    fun quizKlaar_bewaartBesteScore() {
        var d = Leren.quizKlaar(LerenData(), "ohm", 3, 4, vandaag)
        d = Leren.quizKlaar(d, "ohm", 2, 4, vandaag)
        assertEquals(QuizScore(2, 4, 3, vandaag), d.quiz["ohm"])
        assertEquals(8, d.geoefend[vandaag])
    }

    @Test
    fun quiz_hoogstens8Vragen_optiesDoorElkaar() {
        val q = Leren.nieuweQuiz("spelling", Random(3))
        assertEquals(5, q.vragen.size)
        q.vragen.zip(q.volgorde).forEach { (v, volgorde) ->
            if (v is Meerkeuze) assertEquals(v.opties.indices.toSet(), volgorde.toSet())
        }
        var r = q
        repeat(5) { r = r.beantwoord(it % 2 == 0).volgende() }
        assertTrue(r.klaar)
        assertEquals(3, r.goed)
    }

    // ---- Inhoud ----

    @Test
    fun inhoud_klopt() {
        val ids = LerenInhoud.kaarten.map { it.id } + LerenInhoud.vragen.map { it.id }
        assertEquals("Elke kaart en vraag een eigen id", ids.size, ids.toSet().size)
        val onderwerpen = LerenInhoud.onderwerpen.map { it.id }.toSet()
        val vakken = LerenInhoud.vakken.map { it.id }.toSet()
        LerenInhoud.onderwerpen.forEach { assertTrue(it.vak in vakken) }
        LerenInhoud.kaarten.forEach { assertTrue(it.id, it.onderwerp in onderwerpen) }
        LerenInhoud.vragen.forEach { v ->
            assertTrue(v.id, v.onderwerp in onderwerpen)
            if (v is Meerkeuze) {
                assertTrue(v.id, v.goed in v.opties.indices)
                assertEquals(v.id, v.opties.size, v.opties.toSet().size)
            }
        }
        // Elk vak heeft iets om te oefenen.
        vakken.forEach { vak -> assertTrue(vak, Leren.onderwerpen(vak).any { Leren.kaarten(LerenData(), it.id).isNotEmpty() }) }
    }

    @Test
    fun rekenvragen_kloppenMetDeWetVanOhm() {
        val v = LerenInhoud.vragen.filterIsInstance<Rekenvraag>().associateBy { it.id }
        assertEquals(12.0 / 4, v.getValue("q6").antwoord, 1e-9) // I = U ÷ R
        assertEquals(230 * 0.5, v.getValue("q7").antwoord, 1e-9) // P = U × I
        assertEquals(2.0 * 100, v.getValue("q8").antwoord, 1e-9) // U = I × R
        assertEquals(2300.0 / 230, v.getValue("q9").antwoord, 1e-9) // I = P ÷ U
    }

    // ---- Eigen kaarten ----

    @Test
    fun eigenKaart_toevoegenEnWissen() {
        val k = Kaart.nieuw("project", "  Wat is mijn onderwerp? ", "Zonnepanelen", nu = 5)
        var d = Leren.metEigen(LerenData(), k)
        assertEquals("Wat is mijn onderwerp?", d.eigen.single().vraag)
        assertTrue(Leren.kaarten(d, "project").contains(k))
        d = Leren.geoefend(d, k.id, true, vandaag)
        d = Leren.zonderEigen(d, k.id)
        assertTrue(d.eigen.isEmpty())
        assertNull(d.standen[k.id])
    }

    @Test
    fun voortgang_teltBekendEnBezig() {
        val d = LerenData(standen = mapOf("o1" to KaartStand(5, vandaag.plusDays(9)), "o2" to KaartStand(2, vandaag), "o3" to KaartStand(4, vandaag.plusDays(1))))
        assertEquals(Leren.Voortgang(kaarten = 4, bekend = 2, bezig = 1, klaar = 2), Leren.voortgang(d, "ohm", vandaag))
    }

    // ---- Bewaren ----

    @Test
    fun json_heenEnTerug() {
        val d = LerenData(
            standen = mapOf("r1" to KaartStand(3, vandaag)),
            quiz = mapOf("ohm" to QuizScore(3, 4, 4, vandaag)),
            eigen = listOf(Kaart("abc", "project", "Vraag \"met\" quotes", "Regel 1\nRegel 2", eigen = true, updatedAt = 42)),
            geoefend = mapOf(vandaag to 7),
            vak = "nederlands",
        )
        assertEquals(d, LerenJson.lees(LerenJson.schrijf(d)))
    }

    @Test
    fun json_kapotGeeftLeegOfSlaatStukjeOver() {
        assertEquals(LerenData(), LerenJson.lees("dit is geen json"))
        val d = LerenJson.lees("""{"standen":{"r1":{"bakje":9,"terug":"2026-10-25"},"r2":{"bakje":2,"terug":"gisteren"}},"vak":"bestaatniet"}""")
        assertEquals(mapOf("r1" to KaartStand(5, vandaag)), d.standen)
        assertEquals("elektro", d.vak)
    }

    // ---- Voorlezen ----

    @Test
    fun uitspraak_formulesGoedVoorlezen() {
        assertEquals("I is U gedeeld door R is 230 gedeeld door 46 is 5 ampère", Leren.uitspraak("I = U ÷ R = 230 ÷ 46 = 5 A"))
        assertEquals("230 keer 0 komma 5 is 115 watt", Leren.uitspraak("230 × 0,5 = 115 W"))
        assertEquals("Ook: P is I kwadraat keer R.", Leren.uitspraak("Ook: P = I² × R."))
        assertEquals("30 milliampère", Leren.uitspraak("30 mA"))
    }

    // ---- Pauze ----

    private val start = Instant.parse("2026-10-25T13:00:00Z")
    private fun sec(s: Long) = Duration.ofSeconds(s)

    @Test
    fun pauze_na20Minuten() {
        var w = PauzeWacht()
        var t = start
        var reden: PauzeReden? = null
        var keer = 0
        while (reden == null && keer < 200) {
            t = t.plus(sec(10))
            val (nieuw, r) = w.naAntwoord(sec(10), t)
            w = nieuw
            reden = r
            keer++
        }
        assertEquals(PauzeReden.Tijd, reden)
        // 20 minuten = 120 antwoorden van 10 seconden.
        assertEquals(120, keer)
    }

    @Test
    fun pauze_alsJeVeelLangzamerWordt() {
        var w = PauzeWacht()
        var t = start
        fun antwoord(s: Long): PauzeReden? {
            t = t.plus(sec(s))
            val (nieuw, r) = w.naAntwoord(sec(s), t)
            w = nieuw
            return r
        }
        repeat(5) { assertNull(antwoord(6)) }
        assertNull(antwoord(14))
        assertNull(antwoord(14))
        assertEquals(PauzeReden.Langzamer, antwoord(14))
    }

    @Test
    fun pauze_nietBijEenBeetjeLangzamer_ofTelefoonWeggelegd() {
        var w = PauzeWacht()
        var t = start
        fun antwoord(s: Long): PauzeReden? {
            t = t.plus(sec(s))
            val (nieuw, r) = w.naAntwoord(sec(s), t)
            w = nieuw
            return r
        }
        repeat(5) { antwoord(8) }
        repeat(3) { assertNull(antwoord(12)) } // niet 2× zo traag
        assertNull(antwoord(240)) // 4 minuten weg: telt niet
        assertEquals(8, w.tijden.size)
    }

    @Test
    fun nogEvenDoor_tienMinutenStil_naPauzeOpnieuw() {
        val bezig = PauzeWacht(begin = start)
        val nu = start.plus(Duration.ofMinutes(21))
        assertEquals(PauzeReden.Tijd, bezig.naAntwoord(sec(5), nu).second)
        val door = bezig.doorgaan(nu)
        assertNull(door.naAntwoord(sec(5), nu.plus(Duration.ofMinutes(9))).second)
        assertEquals(PauzeReden.Tijd, door.naAntwoord(sec(5), nu.plus(Duration.ofMinutes(10))).second)
        val (na, r) = bezig.naPauze().naAntwoord(sec(5), nu.plus(Duration.ofMinutes(5)))
        assertNull(r)
        assertEquals(nu.plus(Duration.ofMinutes(5)).minus(sec(5)), na.begin)
    }
}
