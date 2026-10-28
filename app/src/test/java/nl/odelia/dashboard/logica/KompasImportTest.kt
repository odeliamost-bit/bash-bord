package nl.odelia.dashboard.logica

import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KompasImportTest {
    private val zone = ZoneId.of("Europe/Amsterdam")

    private val voorbeeld = """
        {"app":"Kompas","version":2,"gemaakt":"2026-10-27T10:00:00Z","data":{
          "tasks":{
            "a1":{"title":"Fiets maken","list":"vandaag","done":false,"createdAt":1761000000000,"due":"2026-10-30","steps":[{"t":"Band plakken","d":false},{"t":"Pomp zoeken","d":true}]},
            "a2":{"title":"Klaar al","list":"ooit","done":true,"createdAt":1761000000000},
            "a3":{"title":"  ","list":"ooit","done":false}
          },
          "notes":{"n1":{"text":"Idee voor keuzedeel","createdAt":1761000001000}},
          "wishes":{
            "w1":{"title":"Haaknaalden","url":"https://www.example.com/naald","status":"wachten"},
            "w2":{"title":"Jas","url":null,"status":"gekocht"},
            "w3":{"title":"Lamp","url":null,"status":"niet"}
          }
        }}
    """.trimIndent()

    @Test
    fun leestTakenIdeeenEnWensen() {
        val k = KompasImport.lees(voorbeeld, nu = 1761100000000, zone = zone)!!
        assertEquals(listOf("kompas-taak-a1", "kompas-idee-n1"), k.onthoud.map { it.id })

        val taak = k.onthoud[0]
        assertEquals("Fiets maken", taak.tekst)
        assertEquals(LocalDate.of(2026, 10, 30), taak.wanneer)
        assertEquals("Band plakken", taak.wat)
        assertEquals(KompasImport.GROEP, taak.groep)
        assertTrue(taak.seintje)

        assertEquals(false, k.onthoud[1].seintje)

        assertEquals(listOf("kompas-wens-w1", "kompas-wens-w2"), k.wensen.map { it.id })
        assertEquals(listOf("https://www.example.com/naald"), k.wensen[0].links)
        assertTrue(k.wensen[1].gekocht)
    }

    @Test
    fun geenKompasBestand() {
        assertNull(KompasImport.lees("""{"app":"Iets anders","data":{}}""", 0, zone))
        assertNull(KompasImport.lees("geen json", 0, zone))
    }
}
