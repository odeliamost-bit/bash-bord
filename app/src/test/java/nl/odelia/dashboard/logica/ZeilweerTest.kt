package nl.odelia.dashboard.logica

import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ZeilweerTest {
    private val plek = Plek("Testmeer", 52.0, 4.7)
    private val nu = LocalDateTime.of(2026, 10, 24, 9, 0)

    /** Klein stukje zoals Open-Meteo het stuurt: 2 modellen, 3 uur. GFS mist één waarde. */
    private val json = """
        {"hourly":{
          "time":["2026-10-24T11:00","2026-10-24T12:00","2026-10-24T13:00"],
          "wind_speed_10m_knmi_harmonie_arome_netherlands":[20.0,24.0,26.0],
          "wind_gusts_10m_knmi_harmonie_arome_netherlands":[35.0,45.7,40.0],
          "wind_direction_10m_knmi_harmonie_arome_netherlands":[240,250,350],
          "precipitation_knmi_harmonie_arome_netherlands":[0.0,0.0,0.2],
          "temperature_2m_knmi_harmonie_arome_netherlands":[11.0,12.0,12.5],
          "wind_speed_10m_gfs_seamless":[18.0,22.0,null],
          "wind_gusts_10m_gfs_seamless":[30.0,39.0,null],
          "wind_direction_10m_gfs_seamless":[230,240,10],
          "precipitation_gfs_seamless":[0.0,0.0,0.0],
          "temperature_2m_gfs_seamless":[11.5,12.5,13.0],
          "wind_speed_10m_icon_d2":[null,null,null]
        }}
    """.trimIndent()

    @Test
    fun leesWeer_alleenModellenMetGegevens() {
        val b = Zeilweer.leesWeer(json, plek, nu)
        assertEquals(listOf("HARMONIE", "GFS"), b.perModel.keys.map { it.naam })
        assertEquals(3, b.perModel.values.first().size)
    }

    @Test
    fun middagEnSamen() {
        val b = Zeilweer.leesWeer(json, plek, nu)
        val middag = Zeilweer.middag(b, LocalDate.of(2026, 10, 24))
        assertEquals(2, middag.values.first().size) // 12:00 en 13:00
        val samen = Zeilweer.samen(middag)
        assertEquals(23.0, samen[0].wind!!, 0.01) // (24 + 22) / 2
        assertEquals(45.7, samen[0].stoot!!, 0.01) // hardste
        assertEquals(26.0, samen[1].wind!!, 0.01) // GFS ontbreekt: alleen HARMONIE
        // 350° en 10° liggen naast elkaar (rond het noorden), niet 180°.
        assertTrue(samen[1].richting!! > 355 || samen[1].richting!! < 5)
    }

    @Test
    fun oordelen() {
        fun uur(wind: Double, stoot: Double, regen: Double) = Uurweer(nu, wind, stoot, 240.0, regen, 12.0)
        assertEquals("Te veel wind", Zeilweer.oordeel(listOf(uur(25.0, 46.0, 0.0))).tekst)
        assertEquals("Weinig wind", Zeilweer.oordeel(listOf(uur(5.0, 8.0, 0.0))).tekst)
        assertEquals("Veel regen", Zeilweer.oordeel(listOf(uur(20.0, 30.0, 1.5), uur(20.0, 30.0, 1.0))).tekst)
        assertEquals("Goed zeilweer!", Zeilweer.oordeel(listOf(uur(20.0, 30.0, 0.1))).tekst)
        assertEquals("Geen weer gevonden", Zeilweer.oordeel(emptyList()).tekst)
    }

    @Test
    fun eensOfNiet() {
        val m1 = Zeilweer.modellen[0]
        val m2 = Zeilweer.modellen[1]
        fun uren(wind: Double) = listOf(Uurweer(nu, wind, null, null, null, null))
        assertTrue(Zeilweer.eens(mapOf(m1 to uren(20.0), m2 to uren(23.0))))
        assertFalse(Zeilweer.eens(mapOf(m1 to uren(20.0), m2 to uren(30.0))))
    }

    @Test
    fun beaufortEnRichting() {
        assertEquals(4, Zeilweer.bft(25.0))
        assertEquals(5, Zeilweer.bft(29.0))
        assertEquals(0, Zeilweer.bft(0.5))
        assertEquals("ZW", Zeilweer.richtingNaam(225.0))
        assertEquals("N", Zeilweer.richtingNaam(355.0))
        assertEquals("Z", Zeilweer.richtingNaam(180.0))
    }

    @Test
    fun plekken() {
        val p = Zeilweer.leesPlekken(
            """{"results":[{"name":"Reeuwijk","latitude":52.04667,"longitude":4.725,"country":"Nederland","admin1":"Zuid-Holland","admin2":"Bodegraven-Reeuwijk"}]}""",
        )
        assertEquals(1, p.size)
        assertEquals("Reeuwijk", p[0].naam)
        assertEquals("Bodegraven-Reeuwijk, Zuid-Holland, Nederland", p[0].uitleg)
        assertTrue(Zeilweer.leesPlekken("{}").isEmpty())
    }

    @Test
    fun links() {
        assertTrue(Zeilweer.weerLink(plek).startsWith("https://api.open-meteo.com/v1/forecast?latitude=52.0&longitude=4.7&"))
        assertEquals(
            "https://geocoding-api.open-meteo.com/v1/search?count=6&language=nl&format=json&name=Reeuwijkse+Plassen",
            Zeilweer.plekLink(" Reeuwijkse Plassen "),
        )
        assertEquals("https://www.google.com/search?q=verslag+%26+boek", Links.zoek("verslag & boek"))
        assertEquals("https://www.google.com/maps/search/?api=1&query=Stationsplein+1%2C+Gouda", Links.route("Stationsplein 1, Gouda"))
    }
}
