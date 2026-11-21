package nl.odelia.dashboard.logica

import java.time.LocalDateTime
import org.json.JSONArray
import org.json.JSONObject

/**
 * Logboek "laatst gebeurd" (bouwnacht 1, nov 2026): sync, ING-uitgave in een potje, uitgeklokt.
 * Alleen op dit apparaat (`noBackupFilesDir`), niet in de sync. Maximaal [MAX] regels.
 */
data class LogRegel(val tijd: LocalDateTime, val icoon: String, val tekst: String)

data class LogboekData(val regels: List<LogRegel> = emptyList())

object Logboek {
    const val MAX = 30

    /** Nieuwste bovenaan. Zelfde tekst binnen 2 minuten = één regel (sync kan vaak gaan). */
    fun voegToe(d: LogboekData, r: LogRegel): LogboekData {
        val vorige = d.regels.firstOrNull()
        val rest = if (vorige != null && vorige.tekst == r.tekst && !r.tijd.isAfter(vorige.tijd.plusMinutes(2))) d.regels.drop(1) else d.regels
        return LogboekData((listOf(r) + rest).take(MAX))
    }

    fun schrijf(d: LogboekData): String = JSONObject().put("regels", JSONArray().apply {
        d.regels.forEach { put(JSONObject().put("tijd", it.tijd.toString()).put("icoon", it.icoon).put("tekst", it.tekst)) }
    }).toString()

    fun lees(tekst: String): LogboekData = try {
        val a = JSONObject(tekst).getJSONArray("regels")
        LogboekData((0 until a.length()).map { i ->
            val o = a.getJSONObject(i)
            LogRegel(LocalDateTime.parse(o.getString("tijd")), o.optString("icoon"), o.optString("tekst"))
        })
    } catch (_: Exception) {
        LogboekData()
    }
}
