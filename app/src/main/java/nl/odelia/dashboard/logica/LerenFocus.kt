package nl.odelia.dashboard.logica

import java.time.Duration
import java.time.Instant
import java.time.LocalDate

// Leren: doelen voor vandaag en de focus-klok (20 min leren, 5 min pauze). Testjes in LerenFocusTest.

/** Een doel voor vandaag, zoals "Opdracht maken". Afvinken geldt alleen voor die dag. */
data class Doel(val id: String, val emoji: String, val wat: String, val sub: String)

object Doelen {
    /** Vaste doelen (zoals in het ontwerp). Later misschien zelf in te stellen. */
    val vast = listOf(
        Doel("lezen", "📖", "Lezen in je boek", "een stukje"),
        Doel("opdracht", "✏️", "Opdracht maken", "in je schrift"),
        Doel("kaartjes", "🃏", "10 kaartjes leren", "met kaartjes"),
    )

    fun isKlaar(data: LerenData, id: String, dag: LocalDate): Boolean = id in data.doelenKlaar[dag].orEmpty()

    fun aantalKlaar(data: LerenData, dag: LocalDate): Int = vast.count { isKlaar(data, it.id, dag) }

    /** Vink aan of uit. Alleen de laatste 2 weken bewaren. */
    fun wissel(data: LerenData, id: String, vandaag: LocalDate): LerenData {
        val nu = data.doelenKlaar[vandaag].orEmpty()
        val nieuw = if (id in nu) nu - id else nu + id
        val alle = (data.doelenKlaar + (vandaag to nieuw))
            .filter { (dag, ids) -> ids.isNotEmpty() && !dag.isBefore(vandaag.minusDays(13)) }
        return data.copy(doelenKlaar = alle)
    }

    /** Een ster op die dag: kaartjes of een quiz geoefend, of een doel afgevinkt. */
    fun geleerd(data: LerenData, dag: LocalDate): Boolean =
        (data.geoefend[dag] ?: 0) > 0 || data.doelenKlaar[dag].orEmpty().isNotEmpty()
}

enum class FocusFase(val duur: Duration) {
    Leren(Duration.ofMinutes(20)),
    Pauze(Duration.ofMinutes(5)),
}

/**
 * Focus-klok: 20 minuten leren, daarna vanzelf 5 minuten pauze.
 * [sinds] = wanneer de klok (weer) liep; null = stil. [over] = tijd die over was op dat moment.
 */
data class FocusKlok(
    val fase: FocusFase = FocusFase.Leren,
    val over: Duration = FocusFase.Leren.duur,
    val sinds: Instant? = null,
) {
    val loopt: Boolean get() = sinds != null

    fun overOp(nu: Instant): Duration {
        val s = sinds ?: return over
        return over.minus(Duration.between(s, nu)).coerceAtLeast(Duration.ZERO)
    }

    /** Hoeveel van de ring nog vol is (1 = helemaal). */
    fun deel(nu: Instant): Float = overOp(nu).toMillis().toFloat() / fase.duur.toMillis()

    fun start(nu: Instant): FocusKlok = if (loopt) this else copy(sinds = nu)

    fun stil(nu: Instant): FocusKlok = copy(over = overOp(nu), sinds = null)

    /**
     * Kijk of de tijd om is. Na leren begint de pauze vanzelf; na de pauze staat de klok
     * weer klaar op 20 minuten (stil). Geeft de fase die net klaar is, of null.
     */
    fun bijwerken(nu: Instant): Pair<FocusKlok, FocusFase?> {
        val s = sinds ?: return this to null
        if (!overOp(nu).isZero) return this to null
        val klaarOp = s.plus(over)
        return when (fase) {
            FocusFase.Leren -> FocusKlok(FocusFase.Pauze, FocusFase.Pauze.duur, klaarOp).let {
                // Telefoon lang weg geweest? Dan is de pauze ook al voorbij.
                if (it.overOp(nu).isZero) FocusKlok() to FocusFase.Pauze else it to FocusFase.Leren
            }
            FocusFase.Pauze -> FocusKlok() to FocusFase.Pauze
        }
    }

    companion object {
        /** "20:00" */
        fun alsTekst(d: Duration): String {
            val s = (d.toMillis() + 999) / 1000
            return "%d:%02d".format(s / 60, s % 60)
        }
    }
}
