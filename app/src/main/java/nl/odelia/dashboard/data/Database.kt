package nl.odelia.dashboard.data

import android.content.Context
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

@Database(entities = [WerkblokRij::class, DagRij::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun klok(): KlokDao

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
