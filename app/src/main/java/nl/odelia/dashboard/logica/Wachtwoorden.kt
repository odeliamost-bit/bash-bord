package nl.odelia.dashboard.logica

import java.time.Duration
import java.time.Instant
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

/** De drie groepen uit ontwerp/Wachtwoorden.dc.html. */
enum class WachtwoordGroep(val emoji: String, val label: String) {
    School("🎒", "School"),
    Werk("🏭", "Werk"),
    Thuis("🏠", "Thuis"),
}

/**
 * Eén inlog: website of app, gebruikersnaam en wachtwoord.
 * Staat alleen versleuteld op de telefoon (wachtwoorden.json.enc). Gaat nooit online.
 */
data class Inlog(
    val id: String,
    val groep: WachtwoordGroep,
    val site: String,
    val gebruiker: String,
    val wachtwoord: String,
    val emoji: String = groep.emoji,
    val updatedAt: Long = 0,
) {
    companion object {
        fun nieuw(groep: WachtwoordGroep, site: String, gebruiker: String, wachtwoord: String, emoji: String = groep.emoji, nu: Long = System.currentTimeMillis()) =
            Inlog(UUID.randomUUID().toString(), groep, site.trim(), gebruiker.trim(), wachtwoord, emoji, nu)
    }
}

data class WachtwoordenData(val inlogs: List<Inlog> = emptyList())

/** Wat het Wachtwoorden-scherm ziet. Zolang [open] false is, zijn de gegevens leeg (niet ontsleuteld). */
data class KluisStaat(
    val open: Boolean = false,
    val data: WachtwoordenData = WachtwoordenData(),
    val fout: String? = null,
)

object Wachtwoorden {
    /** Na zoveel tijd niks doen gaat het scherm vanzelf weer op slot. */
    val SLOT_NA: Duration = Duration.ofMinutes(3)

    /** Na zoveel tijd haalt de app een gekopieerd wachtwoord weer van het klembord. */
    val KLEMBORD_WEG: Duration = Duration.ofSeconds(60)

    fun moetOpSlot(laatsteActie: Instant, nu: Instant): Boolean =
        Duration.between(laatsteActie, nu) >= SLOT_NA

    /** Inlogs in een groep. Met zoekwoord: in álle groepen zoeken op site en gebruikersnaam. */
    fun zoek(data: WachtwoordenData, groep: WachtwoordGroep, zoek: String): List<Inlog> {
        val z = zoek.trim().lowercase()
        val lijst = if (z.isEmpty()) {
            data.inlogs.filter { it.groep == groep }
        } else {
            data.inlogs.filter { z in it.site.lowercase() || z in it.gebruiker.lowercase() }
        }
        return lijst.sortedBy { it.site.lowercase() }
    }

    fun bewaar(data: WachtwoordenData, inlog: Inlog): WachtwoordenData {
        val rest = data.inlogs.filterNot { it.id == inlog.id }
        return data.copy(inlogs = rest + inlog)
    }

    fun wis(data: WachtwoordenData, id: String): WachtwoordenData =
        data.copy(inlogs = data.inlogs.filterNot { it.id == id })

    /** Bolletjes in plaats van het wachtwoord. Altijd 8, zodat je de lengte niet ziet. */
    const val VERBORGEN = "••••••••"
}

/** Wachtwoorden naar tekst (JSON) en terug. Dit wordt daarna versleuteld; nooit zo bewaren. */
object WachtwoordenJson {
    private const val VERSIE = 1

    fun schrijf(d: WachtwoordenData): String = JSONObject().apply {
        put("versie", VERSIE)
        put("inlogs", JSONArray().apply {
            d.inlogs.forEach { i ->
                put(
                    JSONObject()
                        .put("id", i.id)
                        .put("groep", i.groep.name)
                        .put("site", i.site)
                        .put("gebruiker", i.gebruiker)
                        .put("wachtwoord", i.wachtwoord)
                        .put("emoji", i.emoji)
                        .put("updatedAt", i.updatedAt),
                )
            }
        })
    }.toString()

    fun lees(tekst: String): WachtwoordenData {
        val o = try {
            JSONObject(tekst)
        } catch (_: Exception) {
            return WachtwoordenData()
        }
        val a = o.optJSONArray("inlogs") ?: return WachtwoordenData()
        val inlogs = (0 until a.length()).mapNotNull { n ->
            try {
                val x = a.getJSONObject(n)
                val groep = WachtwoordGroep.entries.firstOrNull { it.name == x.optString("groep") } ?: WachtwoordGroep.Thuis
                Inlog(
                    id = x.getString("id"),
                    groep = groep,
                    site = x.getString("site"),
                    gebruiker = x.optString("gebruiker"),
                    wachtwoord = x.getString("wachtwoord"),
                    emoji = x.optString("emoji").ifBlank { groep.emoji },
                    updatedAt = x.optLong("updatedAt"),
                )
            } catch (_: Exception) {
                null
            }
        }
        return WachtwoordenData(inlogs)
    }
}
