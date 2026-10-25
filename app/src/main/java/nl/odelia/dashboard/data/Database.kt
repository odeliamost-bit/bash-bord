package nl.odelia.dashboard.data

import android.content.Context
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

// Elk record heeft een UUID, `updatedAt` en `verwijderd`, zodat we later kunnen synchroniseren.

/** Eén keer in- en uitklokken. Tijden in milliseconden sinds 1970. `eind` is null = nog bezig. */
@Entity(tableName = "werktijd", indices = [Index("begin")])
data class WerktijdRij(
    @PrimaryKey val id: String,
    val begin: Long,
    val eind: Long?,
    val updatedAt: Long,
    val verwijderd: Boolean = false,
)

/** Een dag die vrij of ziek is. `datum` als "2026-10-23", `soort` als "Vrij" of "Ziek". */
@Entity(tableName = "dagsoort", indices = [Index("datum")])
data class DagSoortRij(
    @PrimaryKey val id: String,
    val datum: String,
    val soort: String,
    val updatedAt: Long,
    val verwijderd: Boolean = false,
)

/** Iets om te onthouden. Alles behalve `tekst` mag leeg zijn. */
@Entity(tableName = "onthoud", indices = [Index("wanneer")])
data class OnthoudRij(
    @PrimaryKey val id: String,
    val tekst: String,
    val wie: String?,
    val wat: String?,
    val waar: String?,
    /** Datum als "2026-10-26". */
    val wanneer: String?,
    /** Persoon, organisatie of project: "Werk", "School", "PVB" … */
    val groep: String?,
    /** Melding de dag ervoor. */
    val seintje: Boolean,
    val klaar: Boolean,
    val gemaakt: Long,
    val updatedAt: Long,
    val verwijderd: Boolean = false,
)

@Dao
interface WerktijdDao {
    @Query("SELECT * FROM werktijd WHERE verwijderd = 0 AND begin >= :vanaf ORDER BY begin")
    fun vanaf(vanaf: Long): Flow<List<WerktijdRij>>

    @Query("SELECT * FROM werktijd WHERE verwijderd = 0 AND begin >= :vanaf ORDER BY begin")
    suspend fun lijstVanaf(vanaf: Long): List<WerktijdRij>

    @Query("SELECT * FROM werktijd WHERE verwijderd = 0 AND eind IS NULL ORDER BY begin DESC LIMIT 1")
    suspend fun open(): WerktijdRij?

    @Query("SELECT * FROM werktijd WHERE verwijderd = 0 AND begin >= :van AND begin < :tot")
    suspend fun tussen(van: Long, tot: Long): List<WerktijdRij>

    @Upsert
    suspend fun bewaar(rij: WerktijdRij)
}

@Dao
interface DagSoortDao {
    @Query("SELECT * FROM dagsoort WHERE verwijderd = 0")
    fun alles(): Flow<List<DagSoortRij>>

    @Query("SELECT * FROM dagsoort WHERE verwijderd = 0")
    suspend fun lijst(): List<DagSoortRij>

    @Query("SELECT * FROM dagsoort WHERE verwijderd = 0 AND datum = :datum")
    suspend fun op(datum: String): List<DagSoortRij>

    @Upsert
    suspend fun bewaar(rij: DagSoortRij)
}

@Dao
interface OnthoudDao {
    @Query("SELECT * FROM onthoud WHERE verwijderd = 0 ORDER BY klaar, wanneer IS NULL, wanneer, gemaakt DESC")
    fun alles(): Flow<List<OnthoudRij>>

    @Query("SELECT * FROM onthoud WHERE verwijderd = 0 ORDER BY klaar, wanneer IS NULL, wanneer, gemaakt DESC")
    suspend fun lijst(): List<OnthoudRij>

    @Query("SELECT * FROM onthoud WHERE id = :id")
    suspend fun met(id: String): OnthoudRij?

    @Upsert
    suspend fun bewaar(rij: OnthoudRij)
}

@Database(entities = [WerktijdRij::class, DagSoortRij::class, OnthoudRij::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun werktijd(): WerktijdDao
    abstract fun dagSoort(): DagSoortDao
    abstract fun onthoud(): OnthoudDao

    companion object {
        @Volatile private var instantie: AppDatabase? = null

        fun van(context: Context): AppDatabase = instantie ?: synchronized(this) {
            instantie ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "dashboard.db")
                .build()
                .also { instantie = it }
        }
    }
}
