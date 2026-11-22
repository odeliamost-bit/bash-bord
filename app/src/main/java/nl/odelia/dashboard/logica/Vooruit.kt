package nl.odelia.dashboard.logica

import java.time.LocalDate
import java.time.YearMonth

/** Een vaste last die elke maand terugkomt, bijv. telefoon € 12 op de 25e uit Vaste lasten (bouwnacht 2). */
data class VasteLast(val id: String, val naam: String, val cent: Long, val dag: Int, val potjeId: String)

/** Eén staafje in een grafiek. */
data class Staaf(val label: String, val waarde: Float)

/**
 * Bouwnacht 2 (nov 2026): grafieken, maandafsluiting, vaste lasten vooruit en wensen bij een potje.
 * Alleen rekenen; de schermen staan in `ui/geld/` en `ui/thuis/`.
 */
object Vooruit {
    private val kortMaand = listOf("jan", "feb", "mrt", "apr", "mei", "jun", "jul", "aug", "sep", "okt", "nov", "dec")

    // ---------- A. Grafieken ----------

    /** Uren per dag, de laatste [aantal] werkdagen (ma–vr) tot en met [vandaag]. */
    fun urenPerDag(dagen: List<Werkdag>, vandaag: LocalDate, aantal: Int = 10): List<Staaf> {
        val perDag = dagen.associateBy { it.datum }
        return generateSequence(vandaag) { it.minusDays(1) }
            .filter { it.dayOfWeek.value <= 5 }
            .take(aantal).toList().reversed()
            .map { d -> Staaf("${d.dayOfMonth}", perDag[d]?.uren ?: 0f) }
    }

    /** Uren per maand, de laatste [aantal] maanden. */
    fun urenPerMaand(dagen: List<Werkdag>, nu: YearMonth, aantal: Int = 6): List<Staaf> =
        maanden(nu, aantal).map { m -> Staaf(kortMaand[m.monthValue - 1], UrenStats.maand(dagen, m).uren) }

    /** Inkomsten (alle "In"-boekingen) per maand, in euro's. */
    fun inkomenPerMaand(boekingen: List<Boeking>, nu: YearMonth, aantal: Int = 6): List<Staaf> =
        maanden(nu, aantal).map { m -> Staaf(kortMaand[m.monthValue - 1], inkomen(boekingen, m) / 100f) }

    /** Uitgaven per maand, in euro's. */
    fun uitgavenPerMaand(boekingen: List<Boeking>, nu: YearMonth, aantal: Int = 6): List<Staaf> =
        maanden(nu, aantal).map { m ->
            Staaf(kortMaand[m.monthValue - 1], boekingen.filter { it.soort == BoekSoort.Uit && YearMonth.from(it.datum) == m }.sumOf { it.cent } / 100f)
        }

    fun inkomen(boekingen: List<Boeking>, m: YearMonth): Long =
        boekingen.filter { it.soort == BoekSoort.In && YearMonth.from(it.datum) == m }.sumOf { it.cent }

    private fun maanden(nu: YearMonth, aantal: Int) = (aantal - 1 downTo 0).map { nu.minusMonths(it.toLong()) }

    // ---------- B. Maandafsluiting ----------

    data class Afsluiting(val maand: YearMonth, val inkomen: Long, val uitgaven: Long, val potjes: List<Pair<Potje, Long>>, val rest: Long, val restPotje: Potje?)

    /** Welke maand moet nog afgesloten worden? De vorige maand, in de eerste 7 dagen; of deze maand, in de laatste 2 dagen. */
    fun teAfsluiten(d: GeldData, vandaag: LocalDate): YearMonth? {
        val m = when {
            vandaag.dayOfMonth <= 7 -> YearMonth.from(vandaag).minusMonths(1)
            vandaag.dayOfMonth >= vandaag.lengthOfMonth() - 1 -> YearMonth.from(vandaag)
            else -> return null
        }
        return if (m.toString() in d.afgesloten || d.boekingen.none { YearMonth.from(it.datum) == m }) null else m
    }

    /** Wat er over is. De "rest" is wat er nog in Vrije geld staat (of anders in Inkomsten). */
    fun afsluiting(d: GeldData, m: YearMonth): Afsluiting {
        val tot = d.boekingen.filter { !it.datum.isAfter(m.atEndOfMonth()) }
        val potjes = d.potjes.map { it to Geld.saldo(it, tot) }
        val rest = potjes.firstOrNull { it.first.id == "vrij" && it.second > 0 }
            ?: potjes.firstOrNull { it.first.id == "inkomsten" && it.second > 0 }
        return Afsluiting(
            maand = m,
            inkomen = inkomen(d.boekingen, m),
            uitgaven = d.boekingen.filter { it.soort == BoekSoort.Uit && YearMonth.from(it.datum) == m }.sumOf { it.cent },
            potjes = potjes,
            rest = rest?.second ?: 0,
            restPotje = rest?.first,
        )
    }

    /** Afsluiten: eventueel de rest verschuiven naar [naar] (sparen of beleggen), en de maand onthouden. */
    fun sluitAf(d: GeldData, a: Afsluiting, naar: String?, id: String, vandaag: LocalDate): GeldData {
        val verschuif = if (naar != null && a.restPotje != null && a.rest > 0 && naar != a.restPotje.id) {
            listOf(Boeking(id, BoekSoort.Over, a.rest, a.restPotje.id, "Maandafsluiting", vandaag, naarPotjeId = naar, notitie = "Rest van ${UrenStats.naam(a.maand)}"))
        } else {
            emptyList()
        }
        return d.copy(boekingen = d.boekingen + verschuif, afgesloten = (d.afgesloten + a.maand.toString()).distinct().takeLast(36))
    }

    // ---------- C. Vaste lasten vooruit ----------

    data class Komend(val datum: LocalDate, val last: VasteLast)
    data class Waarschuwing(val potje: Potje, val saldo: Long, val nodig: Long, val eerste: LocalDate)

    /** Vaste lasten in de komende [dagen] dagen (vandaag telt mee), op datum. */
    fun komend(lasten: List<VasteLast>, vandaag: LocalDate, dagen: Int = 31): List<Komend> =
        (0 until dagen).map { vandaag.plusDays(it.toLong()) }
            .flatMap { d -> lasten.filter { it.dag.coerceAtMost(d.lengthOfMonth()) == d.dayOfMonth }.map { Komend(d, it) } }

    /** Potjes die de vaste lasten van de komende maand niet kunnen betalen. */
    fun waarschuwingen(d: GeldData, vandaag: LocalDate): List<Waarschuwing> =
        komend(d.vasteLasten, vandaag).groupBy { it.last.potjeId }.mapNotNull { (id, k) ->
            val p = d.potje(id) ?: return@mapNotNull null
            val saldo = Geld.saldo(p, d.boekingen)
            val nodig = k.sumOf { it.last.cent }
            if (saldo < nodig) Waarschuwing(p, saldo, nodig, k.first().datum) else null
        }

    // ---------- D. Wensen bij een potje ----------

    data class Sparen(val potje: Potje, val saldo: Long, val prijs: Long, val perMaand: Long, val maanden: Int?) {
        val genoeg: Boolean get() = saldo >= prijs
    }

    /** Hoe staat het sparen voor deze wens? [maanden] = null als er niks bij komt. Gemiddelde van de laatste 3 maanden. */
    fun sparen(w: Wens, d: GeldData, nu: YearMonth): Sparen? {
        val p = d.potje(w.potjeId) ?: return null
        val prijs = w.prijsCent ?: return null
        val saldo = Geld.saldo(p, d.boekingen)
        val erbij = (1..3).sumOf { i ->
            val m = nu.minusMonths(i.toLong())
            d.boekingen.filter { YearMonth.from(it.datum) == m }.sumOf { b ->
                when {
                    b.soort == BoekSoort.In && b.potjeId == p.id -> b.cent
                    b.soort == BoekSoort.Over && b.naarPotjeId == p.id -> b.cent
                    else -> 0L
                }
            }
        } / 3
        val tekort = prijs - saldo
        val maanden = when {
            tekort <= 0 -> 0
            erbij <= 0 -> null
            else -> ((tekort + erbij - 1) / erbij).toInt()
        }
        return Sparen(p, saldo, prijs, erbij, maanden)
    }

    fun sparenTekst(s: Sparen, nu: YearMonth): String = when {
        s.genoeg -> "✅ Genoeg gespaard in ${s.potje.emoji} ${s.potje.naam}!"
        s.maanden == null -> "${Geld.bedrag(s.saldo)} van ${Geld.bedrag(s.prijs)} in ${s.potje.emoji} ${s.potje.naam}. Er komt nu niks bij."
        else -> "${Geld.bedrag(s.saldo)} van ${Geld.bedrag(s.prijs)} in ${s.potje.emoji} ${s.potje.naam}. Genoeg rond ${UrenStats.naam(nu.plusMonths(s.maanden.toLong()))}."
    }
}
