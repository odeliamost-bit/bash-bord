package nl.odelia.dashboard.logica

import java.time.LocalDate
import org.json.JSONArray
import org.json.JSONObject

/** Leren-gegevens naar tekst (JSON) en terug. Kapotte stukjes worden overgeslagen, niet de hele boel. */
object LerenJson {
    private const val VERSIE = 1

    fun schrijf(d: LerenData): String = JSONObject().apply {
        put("versie", VERSIE)
        put("vak", d.vak)
        put("standen", JSONObject().apply {
            d.standen.forEach { (id, s) -> put(id, JSONObject().put("bakje", s.bakje).put("terug", s.terug.toString())) }
        })
        put("quiz", JSONObject().apply {
            d.quiz.forEach { (id, q) ->
                put(id, JSONObject().put("laatst", q.laatst).put("van", q.van).put("beste", q.beste).put("datum", q.datum.toString()))
            }
        })
        put("eigen", JSONArray().apply {
            d.eigen.forEach { k ->
                put(JSONObject().put("id", k.id).put("onderwerp", k.onderwerp).put("vraag", k.vraag).put("antwoord", k.antwoord).put("updatedAt", k.updatedAt))
            }
        })
        put("geoefend", JSONObject().apply { d.geoefend.forEach { (dag, n) -> put(dag.toString(), n) } })
        put("doelen", JSONObject().apply { d.doelenKlaar.forEach { (dag, ids) -> put(dag.toString(), JSONArray(ids.sorted())) } })
    }.toString()

    fun lees(tekst: String): LerenData {
        val o = try {
            JSONObject(tekst)
        } catch (_: Exception) {
            return LerenData()
        }
        val standen = o.optJSONObject("standen")?.let { s ->
            s.keys().asSequence().mapNotNull { id ->
                stukje {
                    val x = s.getJSONObject(id)
                    id to KaartStand(x.getInt("bakje").coerceIn(1, 5), LocalDate.parse(x.getString("terug")))
                }
            }.toMap()
        }.orEmpty()
        val quiz = o.optJSONObject("quiz")?.let { s ->
            s.keys().asSequence().mapNotNull { id ->
                stukje {
                    val x = s.getJSONObject(id)
                    id to QuizScore(x.getInt("laatst"), x.getInt("van"), x.getInt("beste"), LocalDate.parse(x.getString("datum")))
                }
            }.toMap()
        }.orEmpty()
        val eigen = o.optJSONArray("eigen")?.let { a ->
            (0 until a.length()).mapNotNull { i ->
                stukje {
                    val x = a.getJSONObject(i)
                    Kaart(x.getString("id"), x.getString("onderwerp"), x.getString("vraag"), x.getString("antwoord"), eigen = true, updatedAt = x.optLong("updatedAt"))
                }
            }
        }.orEmpty()
        val geoefend = o.optJSONObject("geoefend")?.let { s ->
            s.keys().asSequence().mapNotNull { dag -> stukje { LocalDate.parse(dag) to s.getInt(dag) } }.toMap()
        }.orEmpty()
        val doelen = o.optJSONObject("doelen")?.let { s ->
            s.keys().asSequence().mapNotNull { dag ->
                stukje {
                    val a = s.getJSONArray(dag)
                    LocalDate.parse(dag) to (0 until a.length()).map { a.getString(it) }.toSet()
                }
            }.toMap()
        }.orEmpty()
        val vak = o.optString("vak").takeIf { v -> LerenInhoud.vakken.any { it.id == v } } ?: LerenData().vak
        return LerenData(standen, quiz, eigen, geoefend, vak, doelen)
    }

    private inline fun <T> stukje(maak: () -> T): T? = try {
        maak()
    } catch (_: Exception) {
        null
    }
}
