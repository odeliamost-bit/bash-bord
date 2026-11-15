package nl.odelia.dashboard.logica

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Let op: dit zijn verzonnen voorbeelden. Het echte formaat van ING-meldingen is nog niet gezien.
class IngImportTest {
    private val dag = LocalDate.of(2026, 11, 13)
    private var teller = 0
    private val id = { "id${teller++}" }

    private fun betaling(titel: String?, tekst: String?) = IngImport.lees(titel, tekst) as IngLees.Betaling

    @Test
    fun leest_bedragEnWinkel() {
        assertEquals(IngLees.Betaling(4_50, "Jumbo Utrecht"), betaling("Betaling", "€ 4,50 betaald bij Jumbo Utrecht"))
        assertEquals(IngLees.Betaling(12_34, "ALBERT HEIJN 1234"), betaling(null, "Je hebt €12,34 betaald aan ALBERT HEIJN 1234 met je pas."))
        assertEquals(IngLees.Betaling(1_250_00, "Coolblue B.V."), betaling("Afschrijving", "Er is € 1.250,00 afgeschreven door Coolblue B.V."))
        assertEquals(IngLees.Betaling(3_00, "NS Groep"), betaling("Betaald", "3,00 EUR naar NS Groep op 13-11"))
        assertEquals(IngLees.Betaling(8_00, "Bakker Jan B.V."), betaling(null, "€ 8,00 betaald bij Bakker Jan B.V. met Google Pay. Saldo € 20,00"))
        assertEquals(IngLees.Betaling(1_99, "Lidl"), betaling(null, "Betaald: € 1,99 bij Lidl. Je saldo is € 10,00."))
        assertEquals(IngLees.Betaling(2_95, "Kiosk"), betaling("Kiosk", "Contactloos betaald: € 2,95"))
    }

    @Test
    fun negeert_inkomenEnRuis() {
        assertEquals(IngLees.Geen, IngImport.lees("Bijschrijving", "€ 50,00 ontvangen van Oma"))
        assertEquals(IngLees.Geen, IngImport.lees("ING", "Log in om je nieuwe afschrift te bekijken"))
        assertEquals(IngLees.Geen, IngImport.lees(null, null))
        assertTrue(IngImport.lees("Betaling", "Er is een betaling gedaan") is IngLees.Onbegrepen)
    }

    @Test
    fun sleutel_haaltRuisWeg() {
        assertEquals("albert heijn", IngImport.sleutel("CCV*ALBERT HEIJN 1234 UTRECHT"))
        assertEquals("ah", IngImport.sleutel("AH 1234"))
        assertEquals("coolblue", IngImport.sleutel("Coolblue B.V."))
        assertEquals("bakker jan", IngImport.sleutel("SumUp  *Bakker Jan"))
        assertEquals("AH", IngImport.mooi("AH 1234"))
        assertEquals("Albert Heijn", IngImport.mooi("ALBERT HEIJN 1234"))
    }

    @Test
    fun onbekendeWinkel_wacht_daarnaVanzelf() {
        val start = GeldData()
        val (na1, u1) = IngImport.verwerk(start, IngLees.Betaling(4_50, "JUMBO UTRECHT 12"), "a", dag, id)
        assertTrue(u1 is IngUitkomst.Gevraagd)
        assertEquals(1, na1.ingWacht.size)
        assertTrue(na1.boekingen.isEmpty())

        // Tweede keer dezelfde winkel, nog niet gekozen: ook wachten.
        val (na2, _) = IngImport.verwerk(na1, IngLees.Betaling(2_00, "JUMBO AMERSFOORT"), "b", dag, id)
        assertEquals(2, na2.ingWacht.size)

        // Odelia kiest "Vrije geld" en noemt hem "Jumbo": beide wachtende gaan mee.
        val gekozen = IngImport.kies(na2, na2.ingWacht[0].id, "Jumbo", "vrij", "bood", id)
        assertTrue(gekozen.ingWacht.isEmpty())
        assertEquals(listOf(4_50L, 2_00L), gekozen.boekingen.map { it.cent })
        assertTrue(gekozen.boekingen.all { it.potjeId == "vrij" && it.categorie == "bood" && it.soort == BoekSoort.Uit })
        assertEquals("vrij", gekozen.winkels["jumbo"]?.potjeId)
        assertTrue(IngImport.TAG in gekozen.tags)

        // Volgende keer: meteen geboekt.
        val (na3, u3) = IngImport.verwerk(gekozen, IngLees.Betaling(7_00, "Jumbo Zeist"), "c", dag, id)
        assertTrue(u3 is IngUitkomst.Geboekt)
        assertEquals(3, na3.boekingen.size)
        assertEquals(gekozen.saldo("vrij") - 7_00, na3.saldo("vrij"))
    }

    @Test
    fun zelfdeMelding_teltMaarEenKeer() {
        val d = GeldData(winkels = mapOf("jumbo" to WinkelPot("Jumbo", "vrij", "bood")))
        val (na1, _) = IngImport.verwerk(d, IngLees.Betaling(4_50, "Jumbo"), "zelfde", dag, id)
        val (na2, u2) = IngImport.verwerk(na1, IngLees.Betaling(4_50, "Jumbo"), "zelfde", dag, id)
        assertEquals(IngUitkomst.AlGezien, u2)
        assertEquals(1, na2.boekingen.size)
    }

    @Test
    fun weggehaaldPotje_vraagtOpnieuw() {
        val d = GeldData(winkels = mapOf("jumbo" to WinkelPot("Jumbo", "bestaatniet", "bood")))
        assertNull(IngImport.zoek(d, "Jumbo"))
    }

    @Test
    fun overslaan_vergeten_enJson() {
        val (na, _) = IngImport.verwerk(GeldData(), IngLees.Betaling(4_50, "Hema"), "x", dag, id)
        assertTrue(IngImport.slaOver(na, na.ingWacht[0].id).ingWacht.isEmpty())

        val (vreemd, _) = IngImport.verwerk(na, IngLees.Onbegrepen("raar"), "y", dag, id)
        val d = IngImport.kies(vreemd, na.ingWacht[0].id, "HEMA", "lekkers", "shop", id)
        assertEquals(d, GeldJson.lees(GeldJson.schrijf(d)))
        assertEquals(listOf("raar"), d.ingOnbegrepen)
        assertTrue(IngImport.vergeet(d, "hema").winkels.isEmpty())
        assertEquals("kerk", IngImport.zetPot(d, "hema", "kerk").winkels["hema"]?.potjeId)
    }

    @Test
    fun oudeJson_zonderIng_blijftWerken() {
        val oud = """{"versie":1,"potjes":[{"id":"vrij","naam":"Vrij","emoji":"👛"}],"boekingen":[],"tags":[]}"""
        val d = GeldJson.lees(oud)
        assertTrue(d.winkels.isEmpty() && d.ingWacht.isEmpty() && d.ingGezien.isEmpty())
    }

    @Test
    fun gokCategorie() {
        assertEquals("bood", IngImport.gokCategorie("ALBERT HEIJN 1234"))
        assertEquals("verz", IngImport.gokCategorie("Kruidvat 7766"))
        assertEquals("ov", IngImport.gokCategorie("NS Groep"))
        assertEquals("shop", IngImport.gokCategorie("Onbekend winkeltje"))
    }
}
