package nl.odelia.dashboard.logica

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** De voorbeeld-teksten zijn verzonnen: de echte tekst van ING-meldingen is nog niet gezien. */
class BankMeldingTest {
    private val dag = LocalDate.of(2026, 11, 15)

    @Test
    fun leestAfschrijving() {
        val b = BankMelding.lees("Afschrijving", "Er is € 12,50 afgeschreven van je betaalrekening door ALBERT HEIJN 1234. Nieuw saldo: € 1.234,56")
        assertEquals(BankBericht(BoekSoort.Uit, 12_50, "Albert Heijn 1234"), b)
        assertEquals(BankBericht(BoekSoort.Uit, 4_05, "Jumbo Slochteren"), BankMelding.lees("ING", "Je hebt € 4,05 betaald bij Jumbo Slochteren met je betaalpas"))
        assertEquals(BankBericht(BoekSoort.Uit, 1_234_00, "bol.com"), BankMelding.lees(null, "Betaling aan bol.com: EUR 1.234,00"))
        assertEquals(BoekSoort.Uit, BankMelding.lees("Kruidvat", "€ -3,99")?.soort)
        assertEquals("Kruidvat", BankMelding.lees("Kruidvat", "€ -3,99")?.naam)
    }

    @Test
    fun leestBijschrijving() {
        assertEquals(BankBericht(BoekSoort.In, 100_00, "J. Jansen"), BankMelding.lees("Bijschrijving", "Bijschrijving van € 100,00 van J. Jansen op je betaalrekening"))
        assertEquals(BankBericht(BoekSoort.In, 1_234_56, "Randstad Nederland"), BankMelding.lees("ING", "Er is 1.234,56 euro bijgeschreven van RANDSTAD NEDERLAND"))
        assertEquals("Onbekend", BankMelding.lees("Bijschrijving", "€ 5,00 bijgeschreven")?.naam)
    }

    @Test
    fun slaatAndereMeldingenOver() {
        assertNull(BankMelding.lees("Betaalverzoek", "Sam vraagt je € 10,00 te betalen"))
        assertNull(BankMelding.lees("ING", "Bevestig je betaling van € 25,00 aan bol.com"))
        assertNull(BankMelding.lees("ING", "Welkom terug!"))
        assertNull(BankMelding.lees("ING", "Je saldo is lager dan € 50,00"))
        assertTrue(BankMelding.nietBegrepen("ING", "Je saldo is lager dan € 50,00"))
        assertFalse(BankMelding.nietBegrepen("Betaalverzoek", "Sam vraagt je € 10,00 te betalen"))
        assertFalse(BankMelding.nietBegrepen("ING", "Welkom terug!"))
    }

    @Test
    fun sleutelEnGok() {
        assertEquals("albert heijn", BankMelding.sleutel("Albert Heijn 1234"))
        assertEquals(BankMelding.sleutel("ALBERT HEIJN 8801 GRONINGEN"), BankMelding.sleutel("Albert Heijn 1234"))
        assertEquals("", BankMelding.sleutel("Onbekend"))
        assertEquals("", BankMelding.sleutel("1234"))
        assertEquals("bood", BankMelding.gokCategorie("Albert Heijn 1234"))
        assertEquals("ov", BankMelding.gokCategorie("NS Groningen"))
        assertNull(BankMelding.gokCategorie("Bakker Jansens"))
    }

    private val wacht = BankWacht("w1", BoekSoort.Uit, 12_50, "Albert Heijn 1234", dag)

    @Test
    fun kiezenBoektEnOnthoudt() {
        val geld = GeldData()
        val (d, b) = Bank.kies(BankData(aan = true, wachtrij = listOf(wacht)), geld, "w1", "lekkers", "b1")
        assertEquals(Boeking("b1", BoekSoort.Uit, 12_50, "lekkers", "bood", dag, notitie = "🔔 Albert Heijn 1234"), b)
        assertTrue(d.wachtrij.isEmpty())
        // De volgende keer bij een andere Albert Heijn: vanzelf hetzelfde potje.
        val regel = Bank.regelVoor(d, geld, BankBericht(BoekSoort.Uit, 3_00, "ALBERT HEIJN 8801"))
        assertEquals("lekkers", regel?.potjeId)
        // Geld terug van de winkel is iets anders dan betalen: daar geldt de regel niet.
        assertNull(Bank.regelVoor(d, geld, BankBericht(BoekSoort.In, 3_00, "Albert Heijn 8801")))
        // Vergeten = weer vragen.
        assertNull(Bank.regelVoor(Bank.vergeet(d, regel!!), geld, BankBericht(BoekSoort.Uit, 3_00, "Albert Heijn 8801")))
    }

    @Test
    fun inkomenGaatNaarInkomsten() {
        val w = BankWacht("w2", BoekSoort.In, 1_234_56, "Randstad Nederland", dag)
        val (d, b) = Bank.kies(BankData(wachtrij = listOf(w)), GeldData(), "w2", "loon", "b2")
        assertEquals("inkomsten", b?.potjeId)
        assertEquals("loon", b?.categorie)
        assertEquals(BoekSoort.In, b?.soort)
        assertEquals(1, d.regels.size)
    }

    @Test
    fun fouteKeuzeDoetNiks() {
        val d0 = BankData(wachtrij = listOf(wacht))
        assertNull(Bank.kies(d0, GeldData(), "w1", "bestaat-niet", "b").second)
        assertNull(Bank.kies(d0, GeldData(), "weg", "lekkers", "b").second)
        assertEquals(d0, Bank.kies(d0, GeldData(), "w1", "bestaat-niet", "b").first)
        // Onbekende naam: wel boeken, niks onthouden.
        val (d, b) = Bank.kies(BankData(wachtrij = listOf(wacht.copy(naam = "Onbekend"))), GeldData(), "w1", "vrij", "b")
        assertNotNull(b)
        assertTrue(d.regels.isEmpty())
    }

    @Test
    fun dubbeleMeldingTeltEenKeer() {
        val d = Bank.zetGezien(BankData(), "sleutel|1", 1_000)
        assertTrue(Bank.alGezien(d, "sleutel|1", 20_000))
        assertFalse(Bank.alGezien(d, "sleutel|1", 200_000)) // zelfde koffie een paar minuten later telt wel
        assertFalse(Bank.alGezien(d, "sleutel|2", 1_000))
    }

    @Test
    fun keuzesEnJson() {
        val geld = GeldData()
        assertEquals("inkomsten", Bank.inkomstenPotje(geld))
        // Losse potjes en echte rekeningen staan achteraan.
        assertEquals(setOf("inkomsten", "inv", "spaar"), Bank.keuzes(geld, BoekSoort.Uit).takeLast(3).map { it.id }.toSet())
        assertEquals("loon", Bank.keuzes(geld, BoekSoort.In).first().id)
        assertEquals("€ 12,50 bij Albert Heijn 1234", Bank.vraagTitel(wacht))
        val d = Bank.onthoudNietHerkend(Bank.zetGezien(Bank.kies(BankData(aan = true, wachtrij = listOf(wacht, wacht.copy(id = "w9"))), geld, "w1", "vrij", "b").first, "m", 5), "rare tekst")
        assertEquals(d, BankJson.lees(BankJson.schrijf(d)))
        assertEquals(BankData(), BankJson.lees("kapot"))
    }
}
