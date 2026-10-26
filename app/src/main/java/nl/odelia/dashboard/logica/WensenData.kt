package nl.odelia.dashboard.logica

import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

/** Alle wensen die op de telefoon bewaard worden. De eerste keer: de startlijst. */
data class WensenData(val lijst: List<Wens> = WensenStart.lijst) {
    /** Nieuwe wens bovenaan, of een bestaande (zelfde id) op dezelfde plek vervangen. */
    fun bewaar(w: Wens): WensenData =
        if (lijst.any { it.id == w.id }) copy(lijst = lijst.map { if (it.id == w.id) w else it })
        else copy(lijst = listOf(w) + lijst)

    fun verwijder(id: String): WensenData = copy(lijst = lijst.filterNot { it.id == id })

    fun wisselGekocht(id: String, nu: Long): WensenData = pas(id) { it.copy(gekocht = !it.gekocht, updatedAt = nu) }

    fun volgendHartje(id: String, nu: Long): WensenData = pas(id) { it.copy(hartjes = Wensen.volgendHartje(it.hartjes), updatedAt = nu) }

    private fun pas(id: String, wat: (Wens) -> Wens) = copy(lijst = lijst.map { if (it.id == id) wat(it) else it })
}

/** Een wens maken of aanpassen uit wat ze in het venster invulde. */
fun maakWens(naam: String, soort: WensSoort, linkTekst: String, oud: Wens?, nu: Long): Wens {
    val links = Wensen.linksUit(linkTekst)
    val emoji = if (oud != null && oud.soort == soort) oud.emoji else soort.emoji
    return Wens(
        id = oud?.id ?: UUID.randomUUID().toString(),
        naam = naam.trim(),
        emoji = emoji,
        soort = soort,
        winkel = links.firstNotNullOfOrNull { Wensen.winkel(it) },
        links = links,
        hartjes = oud?.hartjes ?: 0,
        gekocht = oud?.gekocht ?: false,
        updatedAt = nu,
    )
}

/** Wensen naar tekst (JSON) en terug. Kapotte stukjes worden overgeslagen. */
object WensenJson {
    private const val VERSIE = 1

    fun schrijf(d: WensenData): String = JSONObject().apply {
        put("versie", VERSIE)
        put("wensen", JSONArray().apply {
            d.lijst.forEach { w ->
                put(
                    JSONObject()
                        .put("id", w.id)
                        .put("naam", w.naam)
                        .put("emoji", w.emoji)
                        .put("soort", w.soort.name)
                        .put("winkel", w.winkel ?: JSONObject.NULL)
                        .put("links", JSONArray(w.links))
                        .put("hartjes", w.hartjes)
                        .put("gekocht", w.gekocht)
                        .put("updatedAt", w.updatedAt),
                )
            }
        })
    }.toString()

    /** Kapot bestand? Dan de startlijst, zodat het scherm nooit leeg en raar is. */
    fun lees(tekst: String): WensenData {
        val a = try {
            JSONObject(tekst).getJSONArray("wensen")
        } catch (_: Exception) {
            return WensenData()
        }
        val lijst = (0 until a.length()).mapNotNull { i ->
            try {
                val x = a.getJSONObject(i)
                val links = x.optJSONArray("links")?.let { l -> (0 until l.length()).map { l.getString(it) } }.orEmpty()
                    .filter { it.startsWith("https://") || it.startsWith("http://") }
                Wens(
                    id = x.getString("id"),
                    naam = x.getString("naam"),
                    emoji = x.optString("emoji").ifEmpty { "✨" },
                    soort = WensSoort.entries.firstOrNull { it.name == x.optString("soort") } ?: WensSoort.Overig,
                    winkel = if (x.isNull("winkel")) null else x.optString("winkel").ifEmpty { null },
                    links = links,
                    hartjes = x.optInt("hartjes").coerceIn(0, Wensen.MAX_HARTJES),
                    gekocht = x.optBoolean("gekocht"),
                    updatedAt = x.optLong("updatedAt"),
                )
            } catch (_: Exception) {
                null
            }
        }.distinctBy { it.id }
        return WensenData(lijst)
    }
}
