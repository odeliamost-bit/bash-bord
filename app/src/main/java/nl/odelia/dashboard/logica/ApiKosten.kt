package nl.odelia.dashboard.logica

import java.time.YearMonth
import org.json.JSONObject

/**
 * Tegel "Claude-kosten deze maand" (bouwnacht 1, nov 2026).
 * De app kan de echte kosten niet zelf zien (de sleutel staat alleen in de backend).
 * Odelia vult het bedrag uit de Anthropic Console zelf in; de app let op de limiet.
 * Alleen op dit apparaat (`noBackupFilesDir`).
 */
data class ApiKostenData(
    val maand: YearMonth? = null,
    val cent: Long = 0,
    /** Limiet per maand. Standaard € 5. */
    val limietCent: Long = 5_00,
)

object ApiKosten {
    /** Nieuwe maand? Dan begint het bedrag weer bij 0 (de limiet blijft). */
    fun voorMaand(d: ApiKostenData, nu: YearMonth): ApiKostenData = if (d.maand == nu) d else d.copy(maand = nu, cent = 0)

    fun procent(d: ApiKostenData): Int = if (d.limietCent <= 0) 0 else ((d.cent * 100) / d.limietCent).toInt()

    fun bericht(d: ApiKostenData): String = when {
        d.limietCent > 0 && d.cent >= d.limietCent -> "🛑 Limiet bereikt. Zet de vertaler even uit of verhoog de limiet."
        procent(d) >= 80 -> "⚠️ Bijna bij je limiet (${procent(d)}%)."
        else -> "✅ Ruim binnen je limiet (${procent(d)}%)."
    }

    fun schrijf(d: ApiKostenData): String = JSONObject()
        .put("maand", d.maand?.toString()).put("cent", d.cent).put("limiet", d.limietCent).toString()

    fun lees(t: String): ApiKostenData = try {
        val o = JSONObject(t)
        ApiKostenData(o.optString("maand").takeIf { it.isNotEmpty() && it != "null" }?.let(YearMonth::parse), o.optLong("cent"), o.optLong("limiet", 5_00))
    } catch (_: Exception) {
        ApiKostenData()
    }
}
