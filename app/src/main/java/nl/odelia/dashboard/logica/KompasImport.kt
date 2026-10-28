package nl.odelia.dashboard.logica

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.json.JSONObject

/** Wat er uit een Kompas-back-up komt. */
data class KompasInhoud(val onthoud: List<Onthoud>, val wensen: List<Wens>) {
    val leeg: Boolean get() = onthoud.isEmpty() && wensen.isEmpty()
}

/**
 * Leest de back-up van de oude Kompas-pagina (kompas-backup-JJJJ-MM-DD.json).
 *
 * - Open taken → Onthoud (met datum als die er was). Stapjes komen in "wat".
 * - Ideeën/notities → Onthoud, zonder seintje.
 * - Wensen die nog wachten of gekocht zijn → Wensen. "Wil ik niet meer" valt weg.
 * - Klaar-taken vallen weg.
 *
 * Elk item krijgt een vaste id ("kompas-…"), dus twee keer inlezen geeft geen dubbele.
 * Geeft `null` als het geen Kompas-bestand is.
 */
object KompasImport {
    const val GROEP = "Kompas"

    fun lees(tekst: String, nu: Long, zone: ZoneId = ZoneId.systemDefault()): KompasInhoud? {
        val data = try {
            val root = JSONObject(tekst)
            if (root.optString("app") != "Kompas") return null
            root.getJSONObject("data")
        } catch (_: Exception) {
            return null
        }

        val onthoud = mutableListOf<Onthoud>()

        data.optJSONObject("tasks")?.let { taken ->
            taken.keys().forEach { id ->
                val t = taken.optJSONObject(id) ?: return@forEach
                if (t.optBoolean("done")) return@forEach
                val titel = t.optString("title").trim().take(MAX_TEKST)
                if (titel.isEmpty()) return@forEach
                val stapjes = t.optJSONArray("steps")?.let { a ->
                    (0 until a.length()).mapNotNull { j ->
                        a.optJSONObject(j)?.takeUnless { it.optBoolean("d") }?.optString("t")?.trim()?.ifEmpty { null }
                    }
                }.orEmpty()
                val datum = datum(t.optString("due"))
                onthoud += Onthoud(
                    id = "kompas-taak-$id",
                    tekst = titel,
                    wat = stapjes.takeIf { it.isNotEmpty() }?.joinToString(" · ")?.take(MAX_TEKST),
                    wanneer = datum,
                    groep = GROEP,
                    seintje = datum != null,
                    gemaakt = tijd(t.optLong("createdAt"), nu, zone),
                )
            }
        }

        data.optJSONObject("notes")?.let { notities ->
            notities.keys().forEach { id ->
                val n = notities.optJSONObject(id) ?: return@forEach
                val tekstje = n.optString("text").trim().take(MAX_TEKST)
                if (tekstje.isEmpty()) return@forEach
                onthoud += Onthoud(
                    id = "kompas-idee-$id",
                    tekst = tekstje,
                    groep = GROEP,
                    seintje = false,
                    gemaakt = tijd(n.optLong("createdAt"), nu, zone),
                )
            }
        }

        val wensen = mutableListOf<Wens>()
        data.optJSONObject("wishes")?.let { lijst ->
            lijst.keys().forEach { id ->
                val w = lijst.optJSONObject(id) ?: return@forEach
                val status = w.optString("status")
                if (status != "wachten" && status != "gekocht") return@forEach
                val naam = w.optString("title").trim().take(MAX_TEKST)
                if (naam.isEmpty()) return@forEach
                val link = if (w.isNull("url")) "" else w.optString("url")
                val nieuw = maakWens(naam, WensSoort.Overig, link, oud = null, nu = nu)
                wensen += nieuw.copy(id = "kompas-wens-$id", gekocht = status == "gekocht")
            }
        }

        return KompasInhoud(onthoud.sortedBy { it.gemaakt }, wensen)
    }

    private fun datum(s: String?): LocalDate? = try {
        if (s.isNullOrBlank() || s == "null") null else LocalDate.parse(s)
    } catch (_: Exception) {
        null
    }

    private fun tijd(millis: Long, nu: Long, zone: ZoneId): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(if (millis > 0) millis else nu), zone)

    private const val MAX_TEKST = 500
}
