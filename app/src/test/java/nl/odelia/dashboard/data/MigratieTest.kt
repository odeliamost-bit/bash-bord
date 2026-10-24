package nl.odelia.dashboard.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import java.io.File
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Een update van de app mag niets weggooien: oude gegevens moeten na de nieuwe database-versie nog staan. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MigratieTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `gegevens van versie 0_3 blijven staan na de update`() = runBlocking {
        val naam = "migratie.db"
        context.deleteDatabase(naam)
        maakOud(naam, versie = 2) // zoals in app-versie 0.3, met verzonnen gegevens
        context.openOrCreateDatabase(naam, Context.MODE_PRIVATE, null).use { db ->
            db.execSQL("INSERT INTO werkblok VALUES ('b1', '2026-10-23T07:45', '2026-10-23T15:45', 0, 0)")
            db.execSQL("INSERT INTO persoon VALUES ('p1', 'Dhr. Jansen', 'School', 'docent', 0, 0, 0)")
            db.execSQL(
                "INSERT INTO afspraak VALUES ('a1', 'Verslag inleveren', '', 'p1', NULL, 'PVB', NULL, '2026-10-30', 1, 0, " +
                    "'2026-10-23T09:00', 'Getypt', 0, 0)",
            )
        }

        // Openen zoals de nieuwe app doet: Room past de database zelf aan (en controleert hem).
        val room = Room.databaseBuilder(context, AppDatabase::class.java, naam).build()
        try {
            assertEquals("2026-10-23T15:45", room.klok().blok("b1")?.eind)
            assertEquals("Dhr. Jansen", room.onthoud().personenNu().single().naam)
            val a = room.onthoud().afsprakenNu().single()
            assertEquals("Verslag inleveren", a.wat)
            assertEquals("p1", a.persoonId)
            assertFalse(a.todo) // oude afspraken zijn geen to-do
        } finally {
            room.close()
        }
    }

    /** Lege database van een oude [versie], gemaakt met de beschrijving die Room in app/schemas bewaart. */
    private fun maakOud(naam: String, versie: Int) {
        val schema = JSONObject(File("schemas/${AppDatabase::class.java.name}/$versie.json").readText()).getJSONObject("database")
        context.openOrCreateDatabase(naam, Context.MODE_PRIVATE, null).use { db ->
            val tabellen = schema.getJSONArray("entities")
            for (i in 0 until tabellen.length()) {
                val t = tabellen.getJSONObject(i)
                val sql = buildList {
                    add(t.getString("createSql"))
                    t.optJSONArray("indices")?.let { ix -> for (j in 0 until ix.length()) add(ix.getJSONObject(j).getString("createSql")) }
                }
                sql.forEach { db.execSQL(it.replace("\${TABLE_NAME}", t.getString("tableName"))) }
            }
            val setup = schema.getJSONArray("setupQueries")
            for (i in 0 until setup.length()) db.execSQL(setup.getString(i))
            db.version = versie
        }
    }
}
