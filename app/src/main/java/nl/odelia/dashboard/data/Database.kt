package nl.odelia.dashboard.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// Alles staat alleen op de telefoon. Elke rij heeft een UUID en `updated_at`,
// zodat we later kunnen synchroniseren. Weghalen = `verwijderd` aan (dan weet een sync het ook).
// Tijden staan als tekst: "2026-10-23T07:45" en "2026-10-23" (makkelijk te lezen en te sorteren).

/** Eén keer in- en uitklokken. */
@Entity(tableName = "werkblok", indices = [Index("begin")])
data class WerkblokRij(
    @PrimaryKey val id: String,
    val begin: String,
    val eind: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    val verwijderd: Boolean = false,
)

/** Wat voor dag het was (gewerkt, vrij, ziek). Alleen bewaard als het geen gewone werkdag is. */
@Entity(tableName = "dag", indices = [Index(value = ["datum"], unique = true)])
data class DagRij(
    @PrimaryKey val id: String,
    val datum: String,
    val soort: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

@Dao
interface KlokDao {
    @Query("SELECT * FROM werkblok WHERE verwijderd = 0 AND begin >= :van ORDER BY begin")
    fun blokkenVanaf(van: String): Flow<List<WerkblokRij>>

    @Query("SELECT * FROM werkblok WHERE verwijderd = 0 AND begin >= :van ORDER BY begin")
    suspend fun blokkenVanafNu(van: String): List<WerkblokRij>

    @Query("SELECT * FROM werkblok WHERE id = :id")
    suspend fun blok(id: String): WerkblokRij?

    @Upsert
    suspend fun bewaar(blok: WerkblokRij)

    @Query("SELECT * FROM dag WHERE datum >= :van")
    fun dagenVanaf(van: String): Flow<List<DagRij>>

    @Query("SELECT * FROM dag WHERE datum >= :van")
    suspend fun dagenVanafNu(van: String): List<DagRij>

    @Query("SELECT * FROM dag WHERE datum = :datum")
    suspend fun dag(datum: String): DagRij?

    @Upsert
    suspend fun bewaar(dag: DagRij)
}

/** Iemand met wie Odelia afspraken maakt. */
@Entity(tableName = "persoon")
data class PersoonRij(
    @PrimaryKey val id: String,
    val naam: String,
    val groep: String,
    val rol: String,
    val kleur: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    val verwijderd: Boolean = false,
)

/** Iets om te onthouden (stap 3). */
@Entity(tableName = "afspraak", indices = [Index("deadline"), Index("persoon_id")])
data class AfspraakRij(
    @PrimaryKey val id: String,
    val wat: String,
    val notitie: String,
    @ColumnInfo(name = "persoon_id") val persoonId: String?,
    val groep: String?,
    val project: String?,
    val soort: String?,
    val deadline: String?,
    val seintje: Boolean,
    val klaar: Boolean,
    val gemaakt: String,
    val bron: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    val verwijderd: Boolean = false,
    // Nieuw in versie 3; oude afspraken worden geen to-do.
    @ColumnInfo(defaultValue = "0") val todo: Boolean = false,
)

@Dao
interface OnthoudDao {
    @Query("SELECT * FROM afspraak WHERE verwijderd = 0")
    fun afspraken(): Flow<List<AfspraakRij>>

    @Query("SELECT * FROM afspraak WHERE verwijderd = 0")
    suspend fun afsprakenNu(): List<AfspraakRij>

    @Query("SELECT * FROM afspraak WHERE id = :id")
    suspend fun afspraak(id: String): AfspraakRij?

    @Upsert
    suspend fun bewaar(afspraak: AfspraakRij)

    @Query("SELECT * FROM persoon WHERE verwijderd = 0 ORDER BY naam")
    fun personen(): Flow<List<PersoonRij>>

    @Query("SELECT * FROM persoon WHERE verwijderd = 0 ORDER BY naam")
    suspend fun personenNu(): List<PersoonRij>

    @Query("SELECT * FROM persoon WHERE id = :id")
    suspend fun persoon(id: String): PersoonRij?

    @Upsert
    suspend fun bewaar(persoon: PersoonRij)

    /** Afspraken los van een persoon die weg is. Zonder eigen groep krijgen ze de groep van die persoon. */
    @Query("UPDATE afspraak SET persoon_id = NULL, groep = COALESCE(groep, :groep), updated_at = :nu WHERE persoon_id = :id")
    suspend fun losVanPersoon(id: String, groep: String, nu: Long)
}

@Database(
    entities = [WerkblokRij::class, DagRij::class, PersoonRij::class, AfspraakRij::class],
    version = 3,
    // Versie 2 voegt tabellen toe, versie 3 de kolom "todo": dat kan Room zelf (de gegevens blijven staan).
    autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3)],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun klok(): KlokDao
    abstract fun onthoud(): OnthoudDao

    companion object {
        @Volatile private var enige: AppDatabase? = null

        /** Eén database voor de hele app (ook voor de widget en de meldingen). */
        fun van(context: Context): AppDatabase =
            enige ?: synchronized(this) {
                enige ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "dashboard.db")
                    .build()
                    .also { enige = it }
            }
    }
}
