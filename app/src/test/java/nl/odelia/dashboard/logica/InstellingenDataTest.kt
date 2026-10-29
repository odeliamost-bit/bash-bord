package nl.odelia.dashboard.logica

import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class InstellingenDataTest {
    @Test
    fun heenEnTerug() {
        val d = InstellingenData(
            groteTekst = true,
            inklokken = LocalTime.of(7, 30),
            weekdoel = 32f,
            ochtendMelding = false,
            tegels = listOf(TegelSoort.Leren, TegelSoort.Wensen),
            verborgenMenu = setOf("Zeilweer"),
        )
        assertEquals(d, InstellingenJson.lees(InstellingenJson.schrijf(d)))
    }

    @Test
    fun kapotGeeftStandaard() {
        assertEquals(InstellingenData(), InstellingenJson.lees("kapot"))
        assertEquals(InstellingenData(), InstellingenJson.lees("{}"))
    }

    @Test
    fun tegelsSchuivenEnWisselen() {
        val d = InstellingenData(tegels = listOf(TegelSoort.Advies, TegelSoort.Mail, TegelSoort.Leren))
        assertEquals(listOf(TegelSoort.Mail, TegelSoort.Advies, TegelSoort.Leren), d.schuifTegel(TegelSoort.Mail, -1).tegels)
        assertEquals(d, d.schuifTegel(TegelSoort.Advies, -1))
        assertFalse(TegelSoort.Mail in d.wisselTegel(TegelSoort.Mail).tegels)
        assertEquals(TegelSoort.Wensen, d.wisselTegel(TegelSoort.Wensen).tegels.last())
    }

    @Test
    fun weekdoelBlijftBinnenGrenzen() {
        assertEquals(1f, InstellingenData.weekdoelStap(1f, -1))
        assertEquals(36f, InstellingenData.weekdoelStap(35f, 1))
    }
}

class InstellingenGedeeldTest {
    @Test
    fun groteTekstEnTegelsBlijvenPerApparaat() {
        val telefoon = InstellingenData(groteTekst = true, tegels = listOf(TegelSoort.Week), weekdoel = 30f, verborgenMenu = setOf("Cyclus"))
        val tablet = InstellingenData(groteTekst = false, tegels = listOf(TegelSoort.Leren, TegelSoort.Boeken))
        val opTablet = InstellingenJson.neemGedeeld(tablet, InstellingenJson.schrijfGedeeld(telefoon))
        org.junit.Assert.assertEquals(30f, opTablet.weekdoel)
        org.junit.Assert.assertEquals(setOf("Cyclus"), opTablet.verborgenMenu)
        org.junit.Assert.assertEquals(false, opTablet.groteTekst)
        org.junit.Assert.assertEquals(tablet.tegels, opTablet.tegels)
    }
}
