package nl.odelia.dashboard.logica

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.WeekFields
import java.util.Locale

/** Welke uren gaan er naar Kim? Odelia kiest dit zelf voordat de mail opent. */
enum class UrenBereik(val knop: String) { Vandaag("Vandaag"), Week("Deze week"), Maand("Deze maand"), Zelf("Zelf kiezen") }

/**
 * De periode van de urenmail.
 * [naam] staat in het onderwerp ("Uren Odelia – oktober 2026"), [zin] in de mail ("Hier de uren van oktober 2026.").
 */
data class UrenPeriode(val van: LocalDate, val tot: LocalDate, val naam: String, val zin: String)

/** Eén regel van de tabel: Datum | Begin | Eind | Pauze | Totaal. Bij vrij/ziek staat dat woord bij Begin. */
data class UrenRij(val datum: String, val begin: String, val eind: String, val pauze: String, val totaal: String)

/** De urenmail aan Kim (Randstad). De app zet alleen een concept klaar; versturen doet Odelia zelf. */
object Urenmail {
    /** Onderwerp van de oude week-mail (nog gebruikt door Mail-hulp). */
    const val ONDERWERP = "Urenregistratie"

    val KOPJES = listOf("Datum", "Begin", "Eind", "Pauze", "Totaal")

    /** Over welke week gaat de mail? Vrijdag t/m zondag: deze week. Maandag t/m donderdag: vorige week. */
    fun maandag(vandaag: LocalDate): LocalDate {
        val dezeWeek = Uren.maandagVan(vandaag)
        return if (vandaag.dayOfWeek >= DayOfWeek.FRIDAY) dezeWeek else dezeWeek.minusWeeks(1)
    }

    fun onderwerp(p: UrenPeriode) = "Uren Odelia – ${p.naam}"

    /**
     * De periode bij een keuze. Bij [UrenBereik.Zelf] gelden [van] en [tot] (omgedraaid mag ook).
     * Een week loopt van maandag t/m zondag, maar in de naam staat ma t/m vr.
     */
    fun periode(bereik: UrenBereik, vandaag: LocalDate, van: LocalDate? = null, tot: LocalDate? = null): UrenPeriode = when (bereik) {
        UrenBereik.Vandaag -> UrenPeriode(vandaag, vandaag, langeDatum(vandaag), "vandaag")
        UrenBereik.Week -> {
            val ma = Uren.maandagVan(vandaag)
            val nr = ma.get(WeekFields.ISO.weekOfWeekBasedYear())
            UrenPeriode(ma, ma.plusDays(6), "week $nr (${korteDatum(ma)} t/m ${korteDatum(ma.plusDays(4))})", "deze week")
        }
        UrenBereik.Maand -> {
            val eerste = vandaag.withDayOfMonth(1)
            val naam = "${MAANDEN[vandaag.monthValue - 1]} ${vandaag.year}"
            UrenPeriode(eerste, eerste.plusMonths(1).minusDays(1), naam, naam)
        }
        UrenBereik.Zelf -> {
            val a = van ?: vandaag
            val b = tot ?: a
            val (begin, eind) = if (b.isBefore(a)) b to a else a to b
            val naam = when {
                begin == eind -> langeDatum(begin)
                begin.year == eind.year -> "${korteDatum(begin)} t/m ${korteDatum(eind)}-${eind.year}"
                else -> "${korteDatum(begin)}-${begin.year} t/m ${korteDatum(eind)}-${eind.year}"
            }
            UrenPeriode(begin, eind, naam, naam)
        }
    }

    /**
     * De dagen in de tabel. Ma–vr staan er altijd in, een weekenddag alleen als er iets is.
     * Dagen na vandaag tellen niet mee (die zijn er nog niet). Kies je één dag, dan staat die er altijd in.
     */
    fun dagen(perioden: List<Periode>, soorten: Map<LocalDate, DagSoort>, p: UrenPeriode, nu: LocalDateTime): List<Werkdag> {
        val laatste = minOf(p.tot, nu.toLocalDate())
        if (laatste.isBefore(p.van)) return emptyList()
        return generateSequence(p.van) { it.plusDays(1) }
            .takeWhile { !it.isAfter(laatste) }
            .map { Uren.werkdag(perioden, soorten, it, nu) }
            .filter { p.van == p.tot || it.datum.dayOfWeek < DayOfWeek.SATURDAY || it.soort != DagSoort.Leeg }
            .toList()
    }

    fun rij(d: Werkdag): UrenRij {
        val datum = "${korteDagNaam(d.datum)} ${korteDatum(d.datum)}"
        return when (d.soort) {
            DagSoort.Vrij -> UrenRij(datum, "vrij", "", "", "")
            DagSoort.Ziek -> UrenRij(datum, "ziek", "", "", "")
            DagSoort.Leeg -> UrenRij(datum, "–", "", "", "")
            DagSoort.Gewerkt -> UrenRij(
                datum,
                d.begin?.let(::alsKlok) ?: "–",
                d.eind?.let(::alsKlok) ?: if (d.bezig) "bezig" else "–",
                "0:${PAUZE_MINUTEN.toString().padStart(2, '0')}",
                decimaal(d.uren),
            )
        }
    }

    fun totaal(dagen: List<Werkdag>): Float = dagen.sumOf { it.uren.toDouble() }.toFloat()

    fun totaalRegel(dagen: List<Werkdag>) = "Totaal: ${decimaal(totaal(dagen))} uur"

    /** De hele mail. [waarover] komt na "Hier de uren van". */
    fun tekst(dagen: List<Werkdag>, opmerking: String, waarover: String = "afgelopen week", bijlage: Boolean = false): String = buildString {
        appendLine(brief(opmerking, waarover))
        appendLine()
        appendLine(tabel(dagen))
        appendLine()
        append(totaalRegel(dagen))
        if (bijlage) {
            appendLine()
            appendLine()
            append("De tabel zit ook als PDF in de bijlage.")
        }
    }

    /** Het briefje boven de tabel: aanhef, eventuele opmerking en groet. */
    fun brief(opmerking: String, waarover: String): String = buildString {
        appendLine("Hoi Kim,")
        appendLine()
        appendLine("Hier de uren van $waarover.")
        if (opmerking.isNotBlank()) appendLine(opmerking.trim())
        appendLine()
        appendLine("Met hartelijke groet,")
        append("Odelia van der Most")
    }

    /** Tekst-tabel, één regel per dag (smal genoeg voor een telefoon). Lege vakjes aan het eind vallen weg. */
    fun tabel(dagen: List<Werkdag>): String = buildString {
        append(KOPJES.joinToString(" | "))
        dagen.forEach { d ->
            val r = rij(d)
            appendLine()
            append(listOf(r.datum, r.begin, r.eind, r.pauze, r.totaal).dropLastWhile { it.isEmpty() }.joinToString(" | "))
        }
    }

    /** Naam van het PDF-bestand, zonder rare tekens: "Uren Odelia - oktober 2026.pdf". */
    fun bestandsnaam(p: UrenPeriode) = onderwerp(p).replace(" t/m ", " tot ").replace('–', '-').replace(Regex("[^A-Za-z0-9 ()\\-]"), "-") + ".pdf"

    /** 7.25 → "7,25" */
    fun decimaal(uren: Float): String = String.format(Locale.forLanguageTag("nl-NL"), "%.2f", uren)

    /** 27-10 */
    fun korteDatum(d: LocalDate) = "${d.dayOfMonth}-${d.monthValue}"

    /** vr 30-10-2026 */
    fun langeDatum(d: LocalDate) = "${korteDagNaam(d)} ${korteDatum(d)}-${d.year}"

    private val MAANDEN = listOf(
        "januari", "februari", "maart", "april", "mei", "juni",
        "juli", "augustus", "september", "oktober", "november", "december",
    )

    private fun korteDagNaam(datum: LocalDate) = listOf("ma", "di", "wo", "do", "vr", "za", "zo")[datum.dayOfWeek.value - 1]
}
