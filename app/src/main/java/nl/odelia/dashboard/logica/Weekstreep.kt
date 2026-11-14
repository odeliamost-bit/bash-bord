package nl.odelia.dashboard.logica

import java.time.LocalDate

/** Eén bolletje in de weekstreep op Thuis (ma t/m vr). */
enum class Bolletje(val teken: String, val uitleg: String) {
    Gewerkt("✓", "gewerkt"),
    Bezig("●", "nu aan het werk"),
    Vrij("🌴", "vrij"),
    Ziek("🤒", "ziek"),
    /** Een werkdag die al voorbij is en waar niks op staat. */
    Gemist("?", "niks ingevuld"),
    Komt("", "komt nog"),
}

/** Weekstreep en vrij/ziek met één tik (nov 2026). Alleen op de telefoon, net als de uren. */
object Weekstreep {
    fun bolletje(dag: Werkdag, vandaag: LocalDate): Bolletje = when (dag.soort) {
        DagSoort.Gewerkt -> if (dag.bezig) Bolletje.Bezig else Bolletje.Gewerkt
        DagSoort.Vrij -> Bolletje.Vrij
        DagSoort.Ziek -> Bolletje.Ziek
        DagSoort.Leeg -> if (dag.datum.isBefore(vandaag)) Bolletje.Gemist else Bolletje.Komt
    }

    fun bolletjes(week: List<Werkdag>, vandaag: LocalDate): List<Bolletje> = week.map { bolletje(it, vandaag) }

    /** Mag je vandaag met één tik op vrij of ziek zetten? Alleen op een werkdag waar nog niks op staat. */
    fun kanVrijOfZiek(klok: KlokStaat): Boolean =
        Instellingen.isWerkdag(klok.nu.toLocalDate()) && !klok.isIn && klok.vandaag.soort == DagSoort.Leeg

    /** Staat vandaag op vrij of ziek? Dan kan het met één tik weer terug. */
    fun kanTerug(klok: KlokStaat): Boolean = klok.vandaag.soort == DagSoort.Vrij || klok.vandaag.soort == DagSoort.Ziek
}
