package nl.odelia.dashboard.logica

import java.net.URI

/** Soorten wensen, zoals in ontwerp/Wensen.dc.html. */
enum class WensSoort(val label: String, val emoji: String) {
    Kleding("Kleding", "👕"),
    Haken("Haken", "🧶"),
    Huis("Huis", "🏠"),
    Verzorging("Verzorging", "🧴"),
    Overig("Overig", "✨"),
}

/** Eén wens. `hartjes` is 0 t/m 3: hoe graag ze het wil. */
data class Wens(
    val id: String,
    val naam: String,
    val emoji: String,
    val soort: WensSoort,
    val winkel: String?,
    val links: List<String>,
    val hartjes: Int = 0,
    val gekocht: Boolean = false,
)

/** Wat de app uit geplakte, gedeelde of ingesproken tekst haalt. */
data class NieuweWens(val naam: String?, val links: List<String>, val winkel: String?)

data class WensTelling(val open: Int, val gekocht: Int)

/**
 * Regels voor Wensen. Gaat niet online: links worden alleen als tekst bekeken.
 * Openen gebeurt later in Chrome.
 */
object Wensen {
    const val MAX_HARTJES = 3

    private val linkPatroon = Regex("""https?://[^\s<>"]+""", RegexOption.IGNORE_CASE)

    /** Stukjes in een link die alleen bijhouden wie je bent of waar je vandaan komt. */
    private val volgParameters = setOf(
        "fbclid", "gclid", "dclid", "msclkid", "igshid", "si", "mc_cid", "mc_eid",
        "ref", "ref_", "tag", "linkcode", "linkid", "ascsubtag", "psc", "th", "smid", "pd_rd_r",
        "pd_rd_w", "pd_rd_wg", "pf_rd_p", "pf_rd_r", "content-id", "click_key", "click_sum",
        "ga_order", "ga_search_type", "ga_view_type", "ga_search_query", "sessionid", "sid",
        "affiliate", "aff", "aff_id", "affid", "partner", "_ga", "_gl", "srsltid",
    )

    /** Winkels met een herkenbare naam. Andere: naam uit het webadres. */
    private val bekendeWinkels = mapOf(
        "amazon" to "Amazon", "amzn" to "Amazon", "etsy" to "Etsy", "bol" to "Bol",
        "action" to "Action", "ah" to "Albert Heijn", "lidl" to "Lidl", "jumbo" to "Jumbo",
        "hema" to "HEMA", "kruidvat" to "Kruidvat", "ikea" to "IKEA",
    )

    /** Alle links uit een tekst, opgeschoond en zonder dubbele. */
    fun linksUit(tekst: String): List<String> =
        linkPatroon.findAll(tekst)
            .map { schoon(it.value.trimEnd('.', ',', ')', '!', '?', ';', ':')) }
            .distinct()
            .toList()

    /** Haalt volg-stukjes uit een link (utm_…, ref=, affiliate-codes, sessie-id's). */
    fun schoon(link: String): String {
        var schoon = link.substringBefore('#')
        // Amazon zet volg-info ook in het pad: /dp/B0…/ref=sr_1_1
        schoon = Regex("""/ref=[^/?]*""").replace(schoon, "")
        val vraag = schoon.indexOf('?')
        if (vraag < 0) return schoon
        val houden = schoon.substring(vraag + 1).split('&').filter { deel ->
            val sleutel = deel.substringBefore('=').lowercase()
            deel.isNotEmpty() && !sleutel.startsWith("utm_") && sleutel !in volgParameters
        }
        val basis = schoon.substring(0, vraag)
        return if (houden.isEmpty()) basis else basis + "?" + houden.joinToString("&")
    }

    /** Winkelnaam uit een link: "https://www.etsy.com/…" → "Etsy". */
    fun winkel(link: String): String? {
        val host = try {
            URI(link).host
        } catch (e: Exception) {
            null
        } ?: return null
        val delen = host.lowercase().removePrefix("www.").split('.')
        if (delen.size < 2) return null
        val naam = delen[delen.size - 2]
        return bekendeWinkels[naam] ?: naam.replaceFirstChar { it.uppercase() }
    }

    /** Naam, links en winkel uit geplakte of gedeelde tekst. */
    fun uitTekst(tekst: String): NieuweWens {
        val links = linksUit(tekst)
        val naam = linkPatroon.replace(tekst, " ")
            .lines().joinToString(" ") { it.trim() }
            .replace(Regex("""\s+"""), " ")
            .trim(' ', '-', ':', '|')
            .takeIf { it.isNotEmpty() }
        return NieuweWens(naam, links, links.firstNotNullOfOrNull { winkel(it) })
    }

    /** Tik op de hartjes: 0 → 1 → 2 → 3 → 0. */
    fun volgendHartje(hartjes: Int): Int = (hartjes.coerceIn(0, MAX_HARTJES) + 1) % (MAX_HARTJES + 1)

    /** Filter op soort en/of winkel. `null` = alles. Winkel zonder hoofdletter-verschil. */
    fun filter(lijst: List<Wens>, soort: WensSoort? = null, winkel: String? = null): List<Wens> =
        lijst.filter { w ->
            (soort == null || w.soort == soort) &&
                (winkel == null || w.winkel.equals(winkel, ignoreCase = true))
        }

    /** Open wensen eerst (meeste hartjes bovenaan), gekochte onderaan. */
    fun sorteer(lijst: List<Wens>): List<Wens> =
        lijst.sortedWith(compareBy<Wens> { it.gekocht }.thenByDescending { it.hartjes })

    fun tel(lijst: List<Wens>): WensTelling {
        val gekocht = lijst.count { it.gekocht }
        return WensTelling(open = lijst.size - gekocht, gekocht = gekocht)
    }

    /** Winkels die in de lijst voorkomen, voor de filter-knopjes. */
    fun winkels(lijst: List<Wens>): List<String> =
        lijst.mapNotNull { it.winkel }.distinctBy { it.lowercase() }.sortedBy { it.lowercase() }
}
