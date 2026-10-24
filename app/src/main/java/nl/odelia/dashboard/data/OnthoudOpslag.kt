package nl.odelia.dashboard.data

import android.content.Context
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import nl.odelia.dashboard.Bijwerken
import nl.odelia.dashboard.onthoud.Afspraak
import nl.odelia.dashboard.onthoud.Bron
import nl.odelia.dashboard.onthoud.Groep
import nl.odelia.dashboard.onthoud.OnthoudStaat
import nl.odelia.dashboard.onthoud.Persoon
import nl.odelia.dashboard.onthoud.Soort

/** Afspraken en personen bewaren en ophalen (stap 3). */
class OnthoudOpslag(context: Context) {
    private val app = context.applicationContext
    private val dao = AppDatabase.van(app).onthoud()

    fun gegevens(): Flow<Pair<List<Afspraak>, List<Persoon>>> =
        combine(dao.afspraken(), dao.personen()) { a, p -> a.map(::alsAfspraak) to p.map(::alsPersoon) }

    /** Eén keer (voor de meldingen). */
    suspend fun staat(vandaag: LocalDate = LocalDate.now()) =
        OnthoudStaat(dao.afsprakenNu().map(::alsAfspraak), dao.personenNu().map(::alsPersoon), vandaag)

    suspend fun bewaar(a: Afspraak) {
        dao.bewaar(
            AfspraakRij(
                id = a.id, wat = a.wat.trim(), notitie = a.notitie.trim(), persoonId = a.persoonId,
                groep = a.groep?.name, project = a.project?.trim()?.takeIf(String::isNotEmpty), soort = a.soort?.name,
                deadline = a.deadline?.toString(), seintje = a.seintje, klaar = a.klaar,
                gemaakt = a.gemaakt.toString(), bron = a.bron.name, updatedAt = System.currentTimeMillis(),
            ),
        )
        Bijwerken.alles(app)
    }

    suspend fun zetKlaar(id: String, klaar: Boolean) {
        val rij = dao.afspraak(id) ?: return
        dao.bewaar(rij.copy(klaar = klaar, updatedAt = System.currentTimeMillis()))
        Bijwerken.alles(app)
    }

    suspend fun verwijder(id: String) {
        val rij = dao.afspraak(id) ?: return
        dao.bewaar(rij.copy(verwijderd = true, updatedAt = System.currentTimeMillis()))
        Bijwerken.alles(app)
    }

    /** Nieuwe persoon; geeft de persoon terug (met een nieuwe UUID). */
    suspend fun nieuwePersoon(naam: String, groep: Groep, rol: String = ""): Persoon {
        val aantal = dao.personenNu().size
        val p = Persoon(UUID.randomUUID().toString(), naam.trim(), groep, rol.trim(), kleur = aantal)
        dao.bewaar(PersoonRij(p.id, p.naam, p.groep.name, p.rol, p.kleur, System.currentTimeMillis()))
        return p
    }

    private fun alsAfspraak(r: AfspraakRij) = Afspraak(
        id = r.id, wat = r.wat, notitie = r.notitie, persoonId = r.persoonId,
        groep = r.groep?.let { g -> Groep.entries.firstOrNull { it.name == g } },
        project = r.project,
        soort = r.soort?.let { s -> Soort.entries.firstOrNull { it.name == s } },
        deadline = r.deadline?.let(LocalDate::parse), seintje = r.seintje, klaar = r.klaar,
        gemaakt = LocalDateTime.parse(r.gemaakt),
        bron = Bron.entries.firstOrNull { it.name == r.bron } ?: Bron.Getypt,
    )

    private fun alsPersoon(r: PersoonRij) = Persoon(
        r.id, r.naam, Groep.entries.firstOrNull { it.name == r.groep } ?: Groep.Thuis, r.rol, r.kleur,
    )
}
