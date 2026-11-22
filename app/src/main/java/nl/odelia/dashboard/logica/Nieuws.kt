package nl.odelia.dashboard.logica

/**
 * Feitelijk ochtendnieuws (bouwnacht 2): de koppen van de NOS (publieke omroep), zonder mening of uitleg van de app.
 * Alleen ophalen als Odelia op de knop tikt. Er gaat niks van haar mee, alleen de vraag om de nieuwslijst.
 */
object Nieuws {
    const val LINK = "https://feeds.nos.nl/nosnieuwsalgemeen"

    data class Kop(val titel: String, val link: String?)

    private val item = Regex("""<item\b.*?</item>""", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
    private val titel = Regex("""<title\b[^>]*>(.*?)</title>""", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
    private val link = Regex("""<link\b[^>]*>(.*?)</link>""", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))

    /** De eerste [max] koppen uit de RSS-lijst. Kapotte tekst = lege lijst. */
    fun lees(rss: String, max: Int = 6): List<Kop> = item.findAll(rss).mapNotNull { m ->
        val t = titel.find(m.value)?.groupValues?.get(1)?.let(::schoon)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val l = link.find(m.value)?.groupValues?.get(1)?.let(::schoon)?.takeIf { it.startsWith("https://") }
        Kop(t, l)
    }.distinctBy { it.titel }.take(max).toList()

    private fun schoon(t: String) = t.trim()
        .removePrefix("<![CDATA[").removeSuffix("]]>")
        .replace(Regex("<[^>]+>"), "")
        .replace("&amp;", "&").replace("&quot;", "\"").replace("&#39;", "'").replace("&apos;", "'")
        .replace("&lt;", "<").replace("&gt;", ">")
        .trim()
}
