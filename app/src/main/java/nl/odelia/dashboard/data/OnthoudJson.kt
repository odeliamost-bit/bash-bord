package nl.odelia.dashboard.data

import org.json.JSONArray
import org.json.JSONObject

/** Onthoud-rijen naar tekst (JSON) en terug, voor de sync. Kapotte stukjes worden overgeslagen. */
object OnthoudJson {
    private const val VERSIE = 1

    fun schrijf(rijen: List<OnthoudRij>): String = JSONObject()
        .put("versie", VERSIE)
        .put("onthoud", JSONArray().apply {
            rijen.forEach { r ->
                put(
                    JSONObject()
                        .put("id", r.id)
                        .put("tekst", r.tekst)
                        .put("wie", r.wie ?: JSONObject.NULL)
                        .put("wat", r.wat ?: JSONObject.NULL)
                        .put("waar", r.waar ?: JSONObject.NULL)
                        .put("wanneer", r.wanneer ?: JSONObject.NULL)
                        .put("groep", r.groep ?: JSONObject.NULL)
                        .put("seintje", r.seintje)
                        .put("klaar", r.klaar)
                        .put("gemaakt", r.gemaakt)
                        .put("updatedAt", r.updatedAt)
                        .put("verwijderd", r.verwijderd),
                )
            }
        })
        .toString()

    /** Gooit een fout als het helemaal geen Onthoud is, zodat de sync niks leeg maakt. */
    fun lees(tekst: String): List<OnthoudRij> {
        val a = JSONObject(tekst).getJSONArray("onthoud")
        fun JSONObject.tekstOfNull(naam: String) = if (isNull(naam)) null else optString(naam).ifEmpty { null }
        return (0 until a.length()).mapNotNull { i ->
            try {
                val x = a.getJSONObject(i)
                OnthoudRij(
                    id = x.getString("id"),
                    tekst = x.getString("tekst"),
                    wie = x.tekstOfNull("wie"),
                    wat = x.tekstOfNull("wat"),
                    waar = x.tekstOfNull("waar"),
                    wanneer = x.tekstOfNull("wanneer"),
                    groep = x.tekstOfNull("groep"),
                    seintje = x.optBoolean("seintje"),
                    klaar = x.optBoolean("klaar"),
                    gemaakt = x.optLong("gemaakt"),
                    updatedAt = x.optLong("updatedAt"),
                    verwijderd = x.optBoolean("verwijderd"),
                )
            } catch (_: Exception) {
                null
            }
        }.distinctBy { it.id }
    }
}
