package nl.odelia.dashboard.logica

import java.time.LocalDate
import java.time.LocalDateTime
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrenSheetTest {
    private val nu = LocalDateTime.of(2026, 10, 28, 12, 0) // woensdag
    private val ma = LocalDate.of(2026, 10, 26)
    private val perioden = listOf(
        Periode(ma.atTime(7, 45), ma.atTime(15, 45)),
        Periode(nu.toLocalDate().atTime(7, 50), null), // vandaag nog bezig
        Periode(LocalDate.of(2026, 10, 1).atTime(8, 0), LocalDate.of(2026, 10, 1).atTime(16, 0)), // te lang geleden
    )
    private val soorten = mapOf(ma.plusDays(1) to DagSoort.Ziek)

    @Test
    fun alleenAfgerondeDagen() {
        val dagen = UrenSheet.teSturen(perioden, soorten, nu, emptySet())
        // Maandag gewerkt, dinsdag ziek. Vandaag (bezig), lege dagen en 1 oktober niet.
        assertEquals(listOf(ma, ma.plusDays(1)), dagen.map { it.datum })
    }

    @Test
    fun alKlaarGaatNietNogEens() {
        val dagen = UrenSheet.teSturen(perioden, soorten, nu, setOf(ma))
        assertEquals(listOf(ma.plusDays(1)), dagen.map { it.datum })
    }

    @Test
    fun bericht() {
        val dagen = UrenSheet.teSturen(perioden, soorten, nu, emptySet())
        val o = JSONObject(UrenSheet.bericht("geheim", dagen))
        assertEquals("geheim", o.getString("token"))
        val eerste = o.getJSONArray("dagen").getJSONObject(0)
        assertEquals("2026-10-26", eerste.getString("datum"))
        assertEquals("gewerkt", eerste.getString("soort"))
        assertEquals("7:45", eerste.getString("begin"))
        assertEquals("15:45", eerste.getString("eind"))
        assertEquals("0:45", eerste.getString("pauze"))
        val tweede = o.getJSONArray("dagen").getJSONObject(1)
        assertEquals("ziek", tweede.getString("soort"))
        assertFalse(tweede.has("begin"))
    }

    @Test
    fun antwoord() {
        val a = UrenSheet.leesAntwoord("""{"ok":true,"ingevuld":["2026-10-26"],"bleef":["2026-10-27"],"niet":["2027-02-01"]}""")
        assertTrue(a.ok)
        assertEquals(setOf(ma, ma.plusDays(1)), a.klaar)
        assertEquals(1, a.niet.size)

        val fout = UrenSheet.leesAntwoord("""{"ok":false,"fout":"verkeerde code"}""")
        assertFalse(fout.ok)
        assertEquals("verkeerde code", fout.fout)

        assertFalse(UrenSheet.leesAntwoord("<html>inloggen</html>").ok)
    }

    @Test
    fun opruimen() {
        val oud = nu.toLocalDate().minusDays(UrenSheet.TERUG_DAGEN + 1)
        assertEquals(setOf(ma), UrenSheet.opruimen(setOf(oud, ma), nu.toLocalDate()))
    }

    @Test
    fun instellingAanUit() {
        val uit = InstellingenData(urenNaarSheet = false)
        assertFalse(InstellingenJson.lees(InstellingenJson.schrijf(uit)).urenNaarSheet)
        assertTrue(InstellingenJson.lees("{}").urenNaarSheet)
    }
}
