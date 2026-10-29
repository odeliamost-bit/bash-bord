package nl.odelia.dashboard.data

import android.content.Context
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.odelia.dashboard.logica.DagSoort
import nl.odelia.dashboard.logica.KlokStaat
import nl.odelia.dashboard.logica.Onthoud
import nl.odelia.dashboard.logica.Periode

/** Eén plek om alles te bewaren en op te halen. Gebruikt door app, widgets en meldingen. */
class Opslag private constructor(context: Context) {
    private val db = AppDatabase.van(context)
    private val app = context.applicationContext

    // ---- Klokken ----

    fun perioden(): Flow<List<Periode>> = db.werktijd().vanaf(vanaf()).map { rijen -> rijen.map { it.alsPeriode() } }

    fun dagSoorten(): Flow<Map<LocalDate, DagSoort>> = db.dagSoort().alles().map { it.alsKaart() }

    suspend fun klokNu(nu: LocalDateTime = LocalDateTime.now()): KlokStaat = KlokStaat.maak(
        perioden = db.werktijd().lijstVanaf(vanaf()).map { it.alsPeriode() },
        soorten = db.dagSoort().lijst().alsKaart(),
        nu = nu,
        weekdoel = Bestanden.instellingen(app).nu().weekdoel,
    )

    /** In of uit, net wat er nu niet is. Geeft `true` als je nu ingeklokt bent. */
    suspend fun wisselKlok(nu: LocalDateTime = LocalDateTime.now()): Boolean {
        val open = db.werktijd().open()
        return if (open != null && open.begin.alsTijd().toLocalDate() == nu.toLocalDate()) {
            db.werktijd().bewaar(open.copy(eind = nu.alsMillis(), updatedAt = nu.alsMillis()))
            false
        } else {
            val ms = nu.alsMillis()
            db.werktijd().bewaar(WerktijdRij(nieuwId(), begin = ms, eind = null, updatedAt = ms))
            // Toch aan het werk op een dag die op vrij/ziek stond: dan is hij niet meer vrij/ziek.
            db.dagSoort().op(nu.toLocalDate().toString()).forEach { db.dagSoort().bewaar(it.copy(verwijderd = true, updatedAt = ms)) }
            true
        }
    }

    /** Vergeten uit te klokken: zet de eindtijd van de open periode van een eerdere dag. */
    suspend fun sluitVergeten(eind: LocalDateTime) {
        val nu = LocalDateTime.now()
        val vandaagBegin = nu.toLocalDate().atStartOfDay().alsMillis()
        db.werktijd().lijstVanaf(vanaf())
            .filter { it.eind == null && it.begin < vandaagBegin }
            .forEach { db.werktijd().bewaar(it.copy(eind = eind.alsMillis(), updatedAt = nu.alsMillis())) }
    }

    /**
     * Een dag aanpassen. Gewerkt: de dag krijgt precies één periode van [begin] tot [eind]
     * ([eind] leeg = nog bezig, alleen voor vandaag). Vrij/Ziek/Leeg: de periodes vervallen.
     */
    suspend fun zetDag(datum: LocalDate, soort: DagSoort, begin: LocalTime?, eind: LocalTime?) {
        val nu = LocalDateTime.now().alsMillis()
        val van = datum.atStartOfDay().alsMillis()
        val tot = datum.plusDays(1).atStartOfDay().alsMillis()
        db.werktijd().tussen(van, tot).forEach { db.werktijd().bewaar(it.copy(verwijderd = true, updatedAt = nu)) }
        db.dagSoort().op(datum.toString()).forEach { db.dagSoort().bewaar(it.copy(verwijderd = true, updatedAt = nu)) }

        when (soort) {
            DagSoort.Gewerkt -> if (begin != null) {
                db.werktijd().bewaar(
                    WerktijdRij(nieuwId(), datum.atTime(begin).alsMillis(), eind?.let { datum.atTime(it).alsMillis() }, nu),
                )
            }
            DagSoort.Vrij, DagSoort.Ziek -> db.dagSoort().bewaar(DagSoortRij(nieuwId(), datum.toString(), soort.name, nu))
            DagSoort.Leeg -> Unit
        }
    }

    // ---- Onthoud ----

    fun onthoud(): Flow<List<Onthoud>> = db.onthoud().alles().map { rijen -> rijen.map { it.alsOnthoud() } }

    suspend fun onthoudNu(): List<Onthoud> = db.onthoud().lijst().map { it.alsOnthoud() }

    suspend fun onthoudMet(id: String): Onthoud? = db.onthoud().met(id)?.alsOnthoud()

    suspend fun bewaar(o: Onthoud) {
        db.onthoud().bewaar(
            OnthoudRij(
                id = o.id, tekst = o.tekst, wie = o.wie, wat = o.wat, waar = o.waar,
                wanneer = o.wanneer?.toString(), groep = o.groep, seintje = o.seintje, klaar = o.klaar,
                gemaakt = o.gemaakt.alsMillis(), updatedAt = LocalDateTime.now().alsMillis(),
            ),
        )
    }

    suspend fun verwijder(id: String) {
        val rij = db.onthoud().met(id) ?: return
        db.onthoud().bewaar(rij.copy(verwijderd = true, updatedAt = LocalDateTime.now().alsMillis()))
    }

    // ---- Omzetten ----

    private fun vanaf(): Long = LocalDate.now().minusDays(BEWAAR_DAGEN).atStartOfDay().alsMillis()

    private fun WerktijdRij.alsPeriode() = Periode(begin.alsTijd(), eind?.alsTijd())

    private fun List<DagSoortRij>.alsKaart(): Map<LocalDate, DagSoort> =
        associate { LocalDate.parse(it.datum) to DagSoort.valueOf(it.soort) }

    private fun OnthoudRij.alsOnthoud() = Onthoud(
        id = id, tekst = tekst, wie = wie, wat = wat, waar = waar,
        wanneer = wanneer?.let(LocalDate::parse), groep = groep, seintje = seintje, klaar = klaar,
        gemaakt = gemaakt.alsTijd(),
    )

    companion object {
        /** Zoveel dagen terug halen we werktijden op (genoeg voor deze en vorige week). */
        private const val BEWAAR_DAGEN = 21L

        @Volatile private var instantie: Opslag? = null

        fun van(context: Context): Opslag = instantie ?: synchronized(this) {
            instantie ?: Opslag(context.applicationContext).also { instantie = it }
        }

        private fun nieuwId() = UUID.randomUUID().toString()
        private fun LocalDateTime.alsMillis() = atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        private fun Long.alsTijd(): LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
    }
}
