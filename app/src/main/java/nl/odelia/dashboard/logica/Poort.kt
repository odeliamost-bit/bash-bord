package nl.odelia.dashboard.logica

import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Poort-check (bouwnacht 2, gewenst door Odelia): na 16:00, zolang ze nog ingeklokt is, kijkt de app elke 17 minuten
 * één keer waar de telefoon ongeveer is. Buiten de poort van het werk? Dan een zoempje: "Vergeten uit te klokken?".
 * - Staat standaard **uit**. Alleen aan als Odelia het zelf aanzet en Android "Altijd toestaan" geeft.
 * - Geen constante locatie: één korte vraag per keer, en alleen tussen 16:00 en het vangnet (18:00).
 * - De plek van de poort staat alleen op de telefoon (`poort.json` in `noBackupFilesDir`), nooit online of in de code.
 */
data class PoortData(val aan: Boolean = false, val lat: Double? = null, val lon: Double? = null)

object Poort {
    val VANAF: LocalTime = LocalTime.of(16, 0)
    const val ELKE_MINUTEN = 17L
    /** Verder dan dit van de poort = buiten (met marge voor onnauwkeurige locatie). */
    const val STRAAL_M = 250.0

    fun klaar(p: PoortData) = p.aan && p.lat != null && p.lon != null

    /** Afstand in meters (haversine). */
    fun afstand(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return 2 * r * asin(sqrt(a))
    }

    /** Buiten? Alleen als de meting nauwkeurig genoeg is om het zeker te weten. */
    fun buiten(p: PoortData, lat: Double, lon: Double, nauwkeurigM: Float): Boolean {
        val pl = p.lat ?: return false
        val po = p.lon ?: return false
        return afstand(pl, po, lat, lon) - nauwkeurigM > STRAAL_M
    }

    /** Volgende check: vanaf 16:00 om de 17 min, en niet meer na [tot] (het vangnet). */
    fun volgende(nu: LocalDateTime, tot: LocalTime): LocalDateTime? {
        val start = nu.toLocalDate().atTime(VANAF)
        val m = if (nu.isBefore(start)) start else nu.plusMinutes(ELKE_MINUTEN)
        return m.takeIf { it.toLocalTime().isBefore(tot) && it.toLocalDate() == nu.toLocalDate() }
    }

    fun schrijf(p: PoortData): String = org.json.JSONObject().put("aan", p.aan)
        .put("lat", p.lat ?: org.json.JSONObject.NULL).put("lon", p.lon ?: org.json.JSONObject.NULL).toString()

    fun lees(t: String): PoortData = try {
        val o = org.json.JSONObject(t)
        PoortData(o.optBoolean("aan"), if (o.isNull("lat")) null else o.getDouble("lat"), if (o.isNull("lon")) null else o.getDouble("lon"))
    } catch (_: Exception) {
        PoortData()
    }
}
