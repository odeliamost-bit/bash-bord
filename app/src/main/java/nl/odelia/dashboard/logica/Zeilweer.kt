package nl.odelia.dashboard.logica

import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import org.json.JSONObject

/** Een plek om te zeilen. */
data class Plek(val naam: String, val lat: Double, val lon: Double, val uitleg: String = "")

/** Een weermodel. [code] is de naam bij Open-Meteo. */
data class Weermodel(val code: String, val naam: String, val vlag: String)

/** Het weer in één uur volgens één model. Wind en stoten in km/u, regen in mm. */
data class Uurweer(
    val tijd: LocalDateTime,
    val wind: Double?,
    val stoot: Double?,
    val richting: Double?,
    val regen: Double?,
    val temp: Double?,
)

data class Weerbericht(val plek: Plek, val opgehaald: LocalDateTime, val perModel: Map<Weermodel, List<Uurweer>>)

data class Oordeel(val emoji: String, val tekst: String)

/** Een weer-app op de telefoon. [pakket] is de interne naam (zie de link in Google Play). */
data class WeerApp(val naam: String, val pakket: String)

/** Zeilweer: alles uitrekenen uit wat Open-Meteo stuurt. */
object Zeilweer {
    /** Odelia's eigen weer-apps. Nieuwe erbij? Ook in AndroidManifest.xml (<queries>) zetten. */
    val weerApps = listOf(
        WeerApp("Windy", "com.windyty.android"),
        WeerApp("Windfinder", "com.studioeleven.windfinder"),
        WeerApp("Weeronline", "com.xs2theworld.weeronline"),
    )

    val modellen = listOf(
        Weermodel("knmi_harmonie_arome_netherlands", "HARMONIE", "🇳🇱"),
        Weermodel("icon_d2", "ICON", "🇩🇪"),
        Weermodel("ecmwf_ifs025", "ECMWF", "🇪🇺"),
        Weermodel("gfs_seamless", "GFS", "🇺🇸"),
        Weermodel("meteofrance_arome_france_hd", "AROME", "🇫🇷"),
    )

    /** Middag om te zeilen (ontwerp: 12:00 – 16:00). */
    val UREN = 12..16

    /** Windstoten boven deze km/u: te veel wind (ontwerp: 24 knopen). */
    const val TE_VEEL_STOOT = 44.0

    /** Gemiddelde wind onder deze km/u: weinig wind (ontwerp: 5 knopen). */
    const val WEINIG_WIND = 9.0

    /** Meer dan zoveel mm regen in de middag: veel regen. */
    const val VEEL_REGEN = 2.0

    private const val VELDEN = "wind_speed_10m,wind_gusts_10m,wind_direction_10m,precipitation,temperature_2m"

    fun weerLink(plek: Plek): String =
        "https://api.open-meteo.com/v1/forecast?latitude=${plek.lat}&longitude=${plek.lon}" +
            "&hourly=$VELDEN&models=${modellen.joinToString(",") { it.code }}" +
            "&wind_speed_unit=kmh&timezone=Europe%2FAmsterdam&forecast_days=3"

    fun plekLink(naam: String): String =
        "https://geocoding-api.open-meteo.com/v1/search?count=6&language=nl&format=json&name=" +
            URLEncoder.encode(naam.trim().take(100), "UTF-8")

    fun leesPlekken(json: String): List<Plek> {
        val lijst = JSONObject(json).optJSONArray("results") ?: return emptyList()
        return (0 until lijst.length()).map { i ->
            val p = lijst.getJSONObject(i)
            val uitleg = listOf(p.optString("admin2"), p.optString("admin1"), p.optString("country"))
                .filter { it.isNotBlank() }.distinct().joinToString(", ")
            Plek(p.getString("name"), p.getDouble("latitude"), p.getDouble("longitude"), uitleg)
        }
    }

    fun leesWeer(json: String, plek: Plek, nu: LocalDateTime): Weerbericht {
        val uurlijk = JSONObject(json).getJSONObject("hourly")
        val tijden = uurlijk.getJSONArray("time")
        fun reeks(veld: String, model: Weermodel, i: Int): Double? {
            val a = uurlijk.optJSONArray("${veld}_${model.code}") ?: return null
            return if (i < a.length() && !a.isNull(i)) a.getDouble(i) else null
        }
        val perModel = modellen.associateWith { m ->
            (0 until tijden.length()).map { i ->
                Uurweer(
                    tijd = LocalDateTime.parse(tijden.getString(i)),
                    wind = reeks("wind_speed_10m", m, i),
                    stoot = reeks("wind_gusts_10m", m, i),
                    richting = reeks("wind_direction_10m", m, i),
                    regen = reeks("precipitation", m, i),
                    temp = reeks("temperature_2m", m, i),
                )
            }
        }.filterValues { uren -> uren.any { it.wind != null } }
        return Weerbericht(plek, nu, perModel)
    }

    /** Alleen de middag van [dag], per model. */
    fun middag(bericht: Weerbericht, dag: LocalDate): Map<Weermodel, List<Uurweer>> =
        bericht.perModel.mapValues { (_, uren) -> uren.filter { it.tijd.toLocalDate() == dag && it.tijd.hour in UREN } }
            .filterValues { it.isNotEmpty() }

    /** Alle modellen samen: gemiddelde wind, hardste stoot, gemiddelde regen, per uur. */
    fun samen(perModel: Map<Weermodel, List<Uurweer>>): List<Uurweer> {
        val tijden = perModel.values.flatten().map { it.tijd }.distinct().sorted()
        return tijden.map { t ->
            val uur = perModel.values.mapNotNull { uren -> uren.firstOrNull { it.tijd == t } }
            Uurweer(
                tijd = t,
                wind = uur.mapNotNull { it.wind }.gemiddelde(),
                stoot = uur.mapNotNull { it.stoot }.maxOrNull(),
                richting = gemiddeldeRichting(uur.mapNotNull { it.richting }),
                regen = uur.mapNotNull { it.regen }.gemiddelde(),
                temp = uur.mapNotNull { it.temp }.gemiddelde(),
            )
        }
    }

    fun oordeel(uren: List<Uurweer>): Oordeel {
        val wind = uren.mapNotNull { it.wind }.gemiddelde() ?: return Oordeel("❔", "Geen weer gevonden")
        val stoot = uren.mapNotNull { it.stoot }.maxOrNull() ?: 0.0
        val regen = uren.sumOf { it.regen ?: 0.0 }
        return when {
            stoot > TE_VEEL_STOOT -> Oordeel("⚠️", "Te veel wind")
            wind < WEINIG_WIND -> Oordeel("😴", "Weinig wind")
            regen > VEEL_REGEN -> Oordeel("🌧️", "Veel regen")
            else -> Oordeel("👍", "Goed zeilweer!")
        }
    }

    /** Zijn de modellen het eens over de wind? (verschil in gemiddelde wind hooguit 4 km/u) */
    fun eens(perModel: Map<Weermodel, List<Uurweer>>): Boolean {
        val gemiddeld = perModel.values.mapNotNull { uren -> uren.mapNotNull { it.wind }.gemiddelde() }
        return gemiddeld.size < 2 || (gemiddeld.max() - gemiddeld.min()) <= 4.0
    }

    /** Beaufort uit km/u. */
    fun bft(kmu: Double): Int {
        val grenzen = listOf(1, 6, 12, 20, 29, 39, 50, 62, 75, 89, 103, 118)
        return grenzen.indexOfFirst { kmu < it }.let { if (it == -1) 12 else it }
    }

    /** Windrichting als woord: N, NO, O, ZO, Z, ZW, W, NW. */
    fun richtingNaam(graden: Double): String =
        listOf("N", "NO", "O", "ZO", "Z", "ZW", "W", "NW")[((graden / 45.0).roundToInt() % 8 + 8) % 8]

    private fun List<Double>.gemiddelde(): Double? = if (isEmpty()) null else average()

    private fun gemiddeldeRichting(graden: List<Double>): Double? {
        if (graden.isEmpty()) return null
        val x = graden.sumOf { cos(Math.toRadians(it)) }
        val y = graden.sumOf { sin(Math.toRadians(it)) }
        return (Math.toDegrees(atan2(y, x)) + 360) % 360
    }
}
