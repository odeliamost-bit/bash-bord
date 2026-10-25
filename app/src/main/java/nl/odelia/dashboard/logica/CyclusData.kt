package nl.odelia.dashboard.logica

import java.time.LocalDate
import java.time.LocalDateTime
import org.json.JSONArray
import org.json.JSONObject

/** Hoe ze zich voelt tijdens het ongesteld zijn (ontwerp/Cyclus.dc.html). */
enum class Gevoel(val emoji: String, val label: String) {
    Kramp("😣", "Kramp"),
    Moe("😴", "Erg moe"),
    Hoofdpijn("🤕", "Hoofdpijn"),
    Verdrietig("😢", "Verdrietig"),
    VeelBloed("🩸", "Veel bloed"),
    Goed("😊", "Goed"),
}

/**
 * Alles van Cyclus dat op de telefoon bewaard wordt. Gevoelig: gaat nooit online,
 * en staat in een map die Android niet in de cloud-back-up zet.
 */
data class CyclusData(
    val bloedingen: List<Bloeding> = emptyList(),
    val gevoel: Map<LocalDate, Set<Gevoel>> = emptyMap(),
    /** Seintje 2 dagen voor de verwachte dag ("Neem verband mee"). */
    val seintje: Boolean = true,
) {
    fun stand(vandaag: LocalDate): CyclusStand = Cyclus.stand(bloedingen, vandaag)

    /** "Ik ben ongesteld": [dag] is de eerste dag. Twee keer dezelfde dag telt één keer. */
    fun begin(dag: LocalDate): CyclusData {
        if (bloedingen.any { it.begin == dag }) return this
        return copy(bloedingen = (bloedingen + Bloeding(dag, null)).sortedBy { it.begin })
    }

    /**
     * "Ik ben klaar": op [dag] is het over, dus de dag ervoor was de laatste.
     * Klaar op dezelfde dag als begonnen? Dan was het per ongeluk getikt: weghalen.
     */
    fun klaar(dag: LocalDate): CyclusData {
        val laatste = bloedingen.filter { !it.begin.isAfter(dag) }.maxByOrNull { it.begin } ?: return this
        val nieuw = if (dag.isAfter(laatste.begin)) laatste.copy(eind = dag.minusDays(1)) else null
        return copy(bloedingen = bloedingen.mapNotNull { if (it == laatste) nieuw else it })
    }

    fun wisselGevoel(dag: LocalDate, g: Gevoel): CyclusData {
        val nu = gevoel[dag].orEmpty()
        val nieuw = if (g in nu) nu - g else nu + g
        return copy(gevoel = if (nieuw.isEmpty()) gevoel - dag else gevoel + (dag to nieuw))
    }

    /** Wanneer het seintje moet komen, of `null` (uit, al geweest, of al ongesteld). */
    fun seintjeOm(nu: LocalDateTime): LocalDateTime? {
        if (!seintje) return null
        val vandaag = nu.toLocalDate()
        val moment = Cyclus.seintjeDag(stand(vandaag), vandaag)?.atTime(Instellingen.SEINTJE_OM) ?: return null
        return if (moment.isAfter(nu)) moment else null
    }
}

/** Zo veel vakjes heeft de ring: de cycluslengte, of meer als het al langer duurt (maar niet eindeloos). */
fun CyclusStand.ringDagen(): Int = maxOf(lengte, dag?.takeIf { it <= Cyclus.LANGSTE } ?: 0)

/** Tot en met dit vakje is de ring roze (ongesteld). */
fun CyclusStand.roodTot(): Int = if (ongesteld && dag != null) maxOf(duur, dag) else duur

/** Cyclus-gegevens naar tekst (JSON) en terug. Kapotte stukjes worden overgeslagen. */
object CyclusJson {
    private const val VERSIE = 1

    fun schrijf(d: CyclusData): String = JSONObject().apply {
        put("versie", VERSIE)
        put("seintje", d.seintje)
        put("bloedingen", JSONArray().apply {
            d.bloedingen.forEach { b ->
                put(JSONObject().put("begin", b.begin.toString()).apply { b.eind?.let { put("eind", it.toString()) } })
            }
        })
        put("gevoel", JSONObject().apply {
            d.gevoel.forEach { (dag, set) -> put(dag.toString(), JSONArray(set.map { it.name })) }
        })
    }.toString()

    fun lees(tekst: String): CyclusData {
        val o = try {
            JSONObject(tekst)
        } catch (_: Exception) {
            return CyclusData()
        }
        val bloedingen = o.optJSONArray("bloedingen")?.let { a ->
            (0 until a.length()).mapNotNull { i ->
                stukje {
                    val x = a.getJSONObject(i)
                    val begin = LocalDate.parse(x.getString("begin"))
                    val eind = x.optString("eind").takeIf { it.isNotEmpty() }?.let { LocalDate.parse(it) }
                    Bloeding(begin, eind?.takeIf { !it.isBefore(begin) })
                }
            }
        }.orEmpty().distinctBy { it.begin }.sortedBy { it.begin }
        val gevoel = o.optJSONObject("gevoel")?.let { g ->
            g.keys().asSequence().mapNotNull { dag ->
                stukje {
                    val a = g.getJSONArray(dag)
                    val set = (0 until a.length()).mapNotNull { i -> Gevoel.entries.firstOrNull { it.name == a.optString(i) } }.toSet()
                    LocalDate.parse(dag) to set
                }
            }.filter { it.second.isNotEmpty() }.toMap()
        }.orEmpty()
        return CyclusData(bloedingen, gevoel, o.optBoolean("seintje", true))
    }

    private inline fun <T> stukje(maak: () -> T): T? = try {
        maak()
    } catch (_: Exception) {
        null
    }
}
