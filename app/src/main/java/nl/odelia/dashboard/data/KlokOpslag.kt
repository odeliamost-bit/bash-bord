package nl.odelia.dashboard.data

import android.content.Context
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import nl.odelia.dashboard.Bijwerken
import nl.odelia.dashboard.meldingen.Herinneringen
import nl.odelia.dashboard.uren.Blok
import nl.odelia.dashboard.uren.DagSoort
import nl.odelia.dashboard.uren.KlokStaat
import nl.odelia.dashboard.uren.Uren

/** Alle blokken en bijzondere dagen (vrij/ziek) uit de database. */
data class KlokGegevens(val blokken: List<Blok>, val soorten: Map<LocalDate, DagSoort>) {
    fun staat(nu: LocalDateTime, weekdoel: Float) = KlokStaat.maak(blokken, soorten, nu, weekdoel)
}

/**
 * In- en uitklokken en tijden verbeteren.
 * Na elke wijziging worden de widget en de herinneringen bijgewerkt.
 */
class KlokOpslag(context: Context) {
    private val app = context.applicationContext
    private val dao = AppDatabase.van(app).klok()

    /** Gegevens vanaf 6 weken terug (genoeg voor deze week, vergeten blokken en de urenmail). */
    fun gegevens(vandaag: LocalDate = LocalDate.now()): Flow<KlokGegevens> {
        val van = Uren.maandag(vandaag).minusWeeks(6)
        return combine(dao.blokkenVanaf(van.toString()), dao.dagenVanaf(van.toString())) { b, d -> alsGegevens(b, d) }
    }

    /** Hetzelfde, maar één keer (voor de widget en de meldingen). */
    suspend fun nu(vandaag: LocalDate = LocalDate.now()): KlokGegevens {
        val van = Uren.maandag(vandaag).minusWeeks(6).toString()
        return alsGegevens(dao.blokkenVanafNu(van), dao.dagenVanafNu(van))
    }

    /** In als ze uit is, uit als ze in is. Geeft `true` als ze nu ingeklokt is. */
    suspend fun wissel(nu: LocalDateTime = LocalDateTime.now()): Boolean {
        val open = openVandaag(nu)
        if (open == null) klokIn(nu) else klokUit(nu)
        return open == null
    }

    suspend fun klokIn(nu: LocalDateTime = LocalDateTime.now()) {
        if (openVandaag(nu) != null) return
        val eersteVanDeDag = nu().blokken.none { it.datum == nu.toLocalDate() }
        bewaar(Blok(UUID.randomUUID().toString(), nu.opMinuut(), null))
        if (eersteVanDeDag) Herinneringen.planOchtend(app, nu)
    }

    suspend fun klokUit(nu: LocalDateTime = LocalDateTime.now()) {
        val open = openVandaag(nu) ?: return
        bewaar(open.copy(eind = maxOf(nu.opMinuut(), open.begin)))
    }

    /** Nieuw of verbeterd blok. */
    suspend fun bewaar(blok: Blok) {
        dao.bewaar(
            WerkblokRij(
                id = blok.id,
                begin = blok.begin.opMinuut().toString(),
                eind = blok.eind?.opMinuut()?.toString(),
                updatedAt = System.currentTimeMillis(),
            ),
        )
        Bijwerken.alles(app)
    }

    suspend fun verwijder(blokId: String) {
        val rij = dao.blok(blokId) ?: return
        dao.bewaar(rij.copy(verwijderd = true, updatedAt = System.currentTimeMillis()))
        Bijwerken.alles(app)
    }

    suspend fun zetSoort(datum: LocalDate, soort: DagSoort) {
        val oud = dao.dag(datum.toString())
        dao.bewaar(
            DagRij(
                id = oud?.id ?: UUID.randomUUID().toString(),
                datum = datum.toString(),
                soort = soort.name,
                updatedAt = System.currentTimeMillis(),
            ),
        )
        Bijwerken.alles(app)
    }

    private suspend fun openVandaag(nu: LocalDateTime): Blok? =
        nu().blokken.filter { it.eind == null && it.datum == nu.toLocalDate() }.maxByOrNull { it.begin }

    private fun alsGegevens(blokken: List<WerkblokRij>, dagen: List<DagRij>) = KlokGegevens(
        blokken = blokken.map { Blok(it.id, LocalDateTime.parse(it.begin), it.eind?.let(LocalDateTime::parse)) },
        soorten = dagen.associate { LocalDate.parse(it.datum) to (DagSoort.entries.firstOrNull { s -> s.name == it.soort } ?: DagSoort.Werk) },
    )
}

private fun LocalDateTime.opMinuut(): LocalDateTime = truncatedTo(ChronoUnit.MINUTES)
