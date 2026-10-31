package nl.odelia.dashboard.logica

import java.time.LocalDate
import java.time.LocalDateTime
import org.json.JSONArray
import org.json.JSONObject

/**
 * Uren naar Odelia's Google-spreadsheet "Cargill_Registratie Werktijden_Odelia" (gekozen door Odelia, okt 2026).
 *
 * - Alleen afgeronde dagen: gewerkt (begin + eind), vrij of ziek. Lege dagen en "nog bezig" niet.
 * - Tot [TERUG_DAGEN] dagen terug, zodat een gemiste dag (geen internet) later alsnog meegaat.
 * - De spreadsheet vult alleen lege dagen; wat er al staat, blijft staan (zie hulpmiddelen/sheet/Code.gs).
 */
object UrenSheet {
    const val TERUG_DAGEN = 14L

    /** Welke dagen moeten nog naar de spreadsheet? [klaar] = dagen die er al in staan (of bewust zijn overgeslagen). */
    fun teSturen(
        perioden: List<Periode>,
        soorten: Map<LocalDate, DagSoort>,
        nu: LocalDateTime,
        klaar: Set<LocalDate>,
    ): List<Werkdag> {
        val vandaag = nu.toLocalDate()
        return (TERUG_DAGEN downTo 0L)
            .map { vandaag.minusDays(it) }
            .filter { it !in klaar }
            .map { Uren.werkdag(perioden, soorten, it, nu) }
            .filter { d ->
                when (d.soort) {
                    DagSoort.Vrij, DagSoort.Ziek -> true
                    DagSoort.Gewerkt -> !d.bezig && d.begin != null && d.eind != null
                    DagSoort.Leeg -> false
                }
            }
    }

    /** Het bericht voor het script in de spreadsheet. Tijden als "7:45", zoals in de spreadsheet. */
    fun bericht(token: String, dagen: List<Werkdag>): String = JSONObject()
        .put("token", token)
        .put(
            "dagen",
            JSONArray(
                dagen.map { d ->
                    JSONObject()
                        .put("datum", d.datum.toString())
                        .put("soort", d.soort.name.lowercase())
                        .apply {
                            if (d.soort == DagSoort.Gewerkt) {
                                put("begin", d.begin?.let(::alsKlok))
                                put("eind", d.eind?.let(::alsKlok))
                                put("pauze", "0:${PAUZE_MINUTEN.toString().padStart(2, '0')}")
                            }
                        }
                },
            ),
        )
        .toString()

    /** Uitslag van het script. */
    data class Antwoord(val ok: Boolean, val ingevuld: Set<LocalDate>, val bleef: Set<LocalDate>, val niet: Set<LocalDate>, val fout: String?) {
        /** Deze dagen hoeven niet nog eens: ingevuld, of er stond al iets (dan laten we het staan). */
        val klaar get() = ingevuld + bleef
    }

    fun leesAntwoord(tekst: String): Antwoord {
        val o = try {
            JSONObject(tekst)
        } catch (_: Exception) {
            return Antwoord(false, emptySet(), emptySet(), emptySet(), "Onverwacht antwoord van de spreadsheet")
        }
        fun datums(naam: String): Set<LocalDate> = o.optJSONArray(naam)?.let { a ->
            (0 until a.length()).mapNotNull { runCatching { LocalDate.parse(a.optString(it)) }.getOrNull() }.toSet()
        } ?: emptySet()
        return Antwoord(o.optBoolean("ok"), datums("ingevuld"), datums("bleef"), datums("niet"), o.optString("fout").ifEmpty { null })
    }

    /** Dagen ouder dan het venster hoeven we niet meer te onthouden. */
    fun opruimen(klaar: Set<LocalDate>, vandaag: LocalDate): Set<LocalDate> =
        klaar.filterTo(mutableSetOf()) { !it.isBefore(vandaag.minusDays(TERUG_DAGEN)) }
}
