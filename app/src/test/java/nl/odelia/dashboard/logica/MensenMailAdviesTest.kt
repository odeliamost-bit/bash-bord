package nl.odelia.dashboard.logica

import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MensenMailAdviesTest {
    private val dag = LocalDateTime.of(2026, 10, 20, 10, 0)

    @Test
    fun personenPerGroep() {
        val items = listOf(
            Onthoud(tekst = "Verslag inleveren", wie = "Dhr. Jansen", groep = "School", wanneer = LocalDate.of(2026, 10, 26), gemaakt = dag),
            Onthoud(tekst = "Boek meenemen", wie = "Dhr. Jansen", groep = "School", klaar = true, gemaakt = dag),
            Onthoud(tekst = "Uren sturen", wie = "Kim", groep = "Werk", gemaakt = dag),
            Onthoud(tekst = "Zonder groep", wie = "Sanne", gemaakt = dag),
            Onthoud(tekst = "Niemand", gemaakt = dag),
        )
        val p = Mensen.personen(items)
        assertEquals(listOf("Dhr. Jansen", "Kim", "Sanne"), p.map { it.naam })
        assertEquals("School", p[0].groep)
        assertEquals(1, p[0].nogDoen)
        assertEquals("Verslag inleveren", p[0].afspraken.first().tekst)
        assertEquals(Mensen.OVERIG, p[2].groep)
        assertEquals(listOf("School", "Werk", "Thuis", Mensen.OVERIG), Mensen.groepen(p))
        assertEquals("J", Mensen.letter("Dhr. Jansen"))
        assertEquals("di 20 okt", Mensen.gezegdOp(LocalDate.of(2026, 10, 20)))
    }

    @Test
    fun mailSjablonen() {
        val c = MailHulp.concept("ziek", "officieel")
        assertEquals("Ziekmelding", c.onderwerp)
        assertTrue(c.tekst.startsWith("Beste meneer/mevrouw,\n\nHierbij meld ik mij vandaag ziek."))
        assertTrue(c.tekst.endsWith("Met vriendelijke groet,\nOdelia van der Most"))
        val kort = MailHulp.concept("ziek", "officieel", korter = true)
        assertTrue(kort.tekst.length < c.tekst.length)
        assertTrue(MailHulp.isUitleg(MailHulp.concept("anders", "gewoon")))
    }

    @Test
    fun eigenTekstKrijgtAanhefEnGroet() {
        val c = MailHulp.concept("anders", "gewoon", eigen = "Kan ik morgen later komen? Mijn trein valt uit.")
        assertEquals("Hoi,\n\nKan ik morgen later komen? Mijn trein valt uit.\n\nGroetjes,\nOdelia", c.tekst)
        assertEquals("Kan ik morgen later komen", c.onderwerp)
        val u = MailHulp.concept("uren", "gewoon", urenTekst = "TABEL")
        assertEquals(Urenmail.ONDERWERP, u.onderwerp)
        assertEquals("TABEL", u.tekst)
    }

    @Test
    fun adviesZoekenEnBewaren() {
        assertEquals("fietsband", Advies.zoek("mijn band is lek").first().id)
        assertEquals("rijst", Advies.zoek("Hoe kook ik rijst?").first().id)
        assertTrue(Advies.zoek("belastingaangifte doen").isEmpty())
        val d = AdviesData().wissel("plant").wissel("rijst").wissel("plant")
        assertEquals(listOf("rijst"), d.favorieten)
        assertEquals(d, AdviesJson.lees(AdviesJson.schrijf(d)))
        assertEquals(AdviesData(), AdviesJson.lees("kapot"))
    }
}
