package nl.odelia.dashboard.logica

import java.time.LocalDate

// ING-import voor Geld. Geen Android, geen netwerk: alleen tekst lezen en rekenen.
// De luisteraar (meldingen/IngLuisteraar.kt) geeft de tekst van een ING-melding door.
// Let op: het precieze formaat van ING-meldingen is niet getest met een echte melding.
// Daarom is het lezen ruim, en bewaren we meldingen die we niet snappen (zie [GeldData.ingOnbegrepen]).

/** Welke pot (en categorie) hoort bij een winkel. [naam] = hoe Odelia hem noemt, bv. "Jumbo". */
data class WinkelPot(val naam: String, val potjeId: String, val categorie: String)

/** Een ING-uitgave bij een winkel die de app nog niet kent: wacht op Odelia's keuze. */
data class IngWacht(val id: String, val cent: Long, val winkel: String, val datum: LocalDate)

/** Wat er in een melding stond. */
sealed interface IngLees {
    data class Betaling(val cent: Long, val winkel: String) : IngLees
    /** Geen uitgave (bijv. geld ontvangen, of een melding zonder bedrag). Niks mee doen. */
    data object Geen : IngLees
    /** Lijkt op een betaling, maar bedrag of winkel is niet te vinden. */
    data class Onbegrepen(val tekst: String) : IngLees
}

/** Wat de app met een melding deed. */
sealed interface IngUitkomst {
    data class Geboekt(val boeking: Boeking, val potje: Potje) : IngUitkomst
    data class Gevraagd(val wacht: IngWacht) : IngUitkomst
    data object AlGezien : IngUitkomst
    data object Onbegrepen : IngUitkomst
    data object Niks : IngUitkomst
}

object IngImport {
    /** De ING Bankieren-app. Alleen meldingen van deze app worden gelezen. */
    val PAKKETTEN = setOf("com.ing.mobile")

    const val TAG = "ING"
    private const val MAX_GEZIEN = 100
    private const val MAX_ONBEGREPEN = 3

    private val inkomenWoorden = listOf(
        "ontvangen", "bijgeschreven", "bijschrijving", "gestort", "teruggestort", "terugbetaald", "terugboeking", "storting",
    )
    private val uitgaveWoorden = listOf(
        "betaald", "betaling", "afgeschreven", "afschrijving", "gepind", "pinbetaling", "uitgegeven", "incasso", "aankoop", "contactloos",
    )
    /** Titels die geen winkelnaam zijn. */
    private val algemeneTitels = listOf("ing", "ing bankieren", "betaling", "afschrijving", "pinbetaling", "betaald", "uitgave", "nieuwe betaling")

    private val bedragRegex = Regex(
        """(?:€|\beur\b|\beuro\b)\s*-?\s*(\d[\d.]*(?:,\d{1,2})?)|(\d[\d.]*(?:,\d{1,2})?)\s*(?:€|\beur\b|\beuro\b)""",
        RegexOption.IGNORE_CASE,
    )
    private val winkelRegex = Regex("""\b(?:bij|aan|door|naar)\s+(.+)""", RegexOption.IGNORE_CASE)
    /** Waar de winkelnaam ophoudt: een datum/tijd, "met je pas", een zin-einde. */
    private val winkelEinde = Regex(
        """\s+(?:op|om|met|via|van|is|in de)\s+(?=\d|je\b|uw\b|de\b|apple|google|pas|kaart)|(?<!\b\p{L})[.!?]\s|\n|\s[-–|]\s|,\s""",
        RegexOption.IGNORE_CASE,
    )

    /** Lees de tekst van een melding. */
    fun lees(titel: String?, tekst: String?): IngLees {
        val delen = listOfNotNull(titel, tekst).map { it.replace(' ', ' ').trim() }.filter { it.isNotEmpty() }
        if (delen.isEmpty()) return IngLees.Geen
        val alles = delen.joinToString("\n")
        val klein = alles.lowercase()
        if (inkomenWoorden.any { it in klein }) return IngLees.Geen
        val uitgave = uitgaveWoorden.any { it in klein }

        val getal = bedragRegex.find(alles)
        val cent = getal?.let { m -> Geld.leesBedrag(m.groupValues[1].ifEmpty { m.groupValues[2] }) }
        if (cent == null || cent <= 0) return if (uitgave) IngLees.Onbegrepen(alles.take(300)) else IngLees.Geen

        val winkel = winkelUit(alles.removeRange(getal.range)) ?: titelAlsWinkel(titel)
        if (winkel == null) return if (uitgave) IngLees.Onbegrepen(alles.take(300)) else IngLees.Geen
        return IngLees.Betaling(cent, winkel)
    }

    private fun winkelUit(tekst: String): String? {
        val m = winkelRegex.find(tekst) ?: return null
        var w = m.groupValues[1]
        winkelEinde.find(w)?.let { w = w.substring(0, it.range.first) }
        w = w.trim().trimEnd(',', '!', ':', ';').trim()
        // Punt aan het eind weg, behalve bij een afkorting als "B.V.".
        if (w.endsWith('.') && !Regex("""(?:\b\p{L}\.){2,}$""").containsMatchIn(w)) w = w.trimEnd('.').trim()
        return w.take(60).takeIf { it.any(Char::isLetter) }
    }

    private fun titelAlsWinkel(titel: String?): String? {
        val t = titel?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        if (t.lowercase() in algemeneTitels || bedragRegex.containsMatchIn(t) || uitgaveWoorden.any { it in t.lowercase() }) return null
        return t.take(60).takeIf { it.any(Char::isLetter) }
    }

    /**
     * Vaste sleutel voor een winkel: "CCV*ALBERT HEIJN 1234 UTRECHT" → "albert heijn".
     * Betaal-tussenpartijen eraf, alles vanaf het eerste cijfer weg, kleine letters.
     */
    fun sleutel(winkel: String): String {
        var s = winkel.lowercase().trim()
        s = s.replace(Regex("""^(?:ccv|sumup|sum up|zettle|izettle|pay\.nl|mollie|adyen|buckaroo|stripe|paypal|tikkie|myparcel|sq)\s*[*_]\s*"""), "")
        s = s.replace(Regex("""[*_/]"""), " ")
        val cijfer = s.indexOfFirst { it.isDigit() }
        if (cijfer > 0) s = s.substring(0, cijfer)
        s = s.replace(Regex("""\b(?:b\.?v\.?|n\.?v\.?|v\.o\.f\.?|vof)(?=\s|$)"""), " ")
        s = s.replace(Regex("""[^\p{L}\s&'-]"""), " ")
        s = s.replace(Regex("""\s+"""), " ").trim().trim('-', '\'', '&').trim()
        return s.ifEmpty { winkel.lowercase().replace(Regex("""\s+"""), " ").trim() }
    }

    /** Mooie naam voor in de app: "albert heijn" → "Albert Heijn". */
    fun mooi(winkel: String): String = sleutel(winkel).split(' ').joinToString(" ") { w ->
        if (w.length <= 2 || (w.length == 3 && w.none { it in "aeiouy" })) w.uppercase() else w.replaceFirstChar(Char::uppercase)
    }

    /**
     * De bekende winkel bij deze naam, of `null`.
     * Eerst precies; anders een winkel waarvan de naam het begin is ("jumbo" past op "jumbo utrecht").
     */
    fun zoek(d: GeldData, winkel: String): WinkelPot? {
        val k = sleutel(winkel)
        val bestaat = d.winkels.filterValues { d.potje(it.potjeId) != null }
        bestaat[k]?.let { return it }
        return bestaat.entries
            .filter { (s, _) -> k.startsWith("$s ") || s.startsWith("$k ") }
            .maxByOrNull { it.key.length }
            ?.value
    }

    /** Een gok voor de categorie, op winkelnaam. Odelia kan het altijd veranderen. */
    fun gokCategorie(winkel: String): String {
        val woorden = sleutel(winkel).split(' ').toSet()
        val s = sleutel(winkel)
        fun heeft(vararg l: String) = l.any { it in woorden || (it.contains(' ') && s.contains(it)) }
        return when {
            heeft("ah", "albert heijn", "jumbo", "lidl", "aldi", "plus", "dirk", "coop", "spar", "vomar", "hoogvliet", "ekoplaza", "picnic", "deen", "poiesz", "dekamarkt") -> "bood"
            heeft("kruidvat", "etos", "trekpleister", "douglas", "rituals", "ici paris") -> "verz"
            heeft("apotheek", "apotheken", "tandarts", "huisarts", "fysio") -> "zorg"
            heeft("ns", "ovpay", "ov-chipkaart", "ret", "gvb", "htm", "arriva", "qbuzz", "connexxion", "keolis", "ebs", "u-ov") -> "ov"
            heeft("spotify", "netflix", "kpn", "vodafone", "odido", "ziggo", "videoland", "disney", "simyo", "lebara", "youfone") -> "abo"
            heeft("primark", "zeeman", "h&m", "c&a", "wibra", "zara") -> "kleding"
            heeft("intratuin", "tuincentrum", "groenrijk", "pokon") -> "plant"
            heeft("bakker", "bakkerij", "mcdonald's", "mcdonalds", "subway", "starbucks", "kiosk", "smullers", "febo", "burger king") -> "lekkers"
            heeft("wol", "breiwinkel", "stoffen") -> "hand"
            else -> "shop"
        }
    }

    /**
     * Verwerk één ING-melding. Bekende winkel: meteen een uitgave in het goede potje.
     * Onbekende winkel: in de wachtrij, Odelia kiest het potje.
     * [bron] = vaste code van de melding, zodat dezelfde melding nooit twee keer telt.
     */
    fun verwerk(
        d: GeldData,
        lees: IngLees,
        bron: String,
        datum: LocalDate,
        id: () -> String = GeldBoek::nieuwId,
    ): Pair<GeldData, IngUitkomst> {
        when (lees) {
            IngLees.Geen -> return d to IngUitkomst.Niks
            is IngLees.Onbegrepen -> {
                if (lees.tekst in d.ingOnbegrepen) return d to IngUitkomst.AlGezien
                return d.copy(ingOnbegrepen = (d.ingOnbegrepen + lees.tekst).takeLast(MAX_ONBEGREPEN)) to IngUitkomst.Onbegrepen
            }
            is IngLees.Betaling -> {
                if (bron in d.ingGezien) return d to IngUitkomst.AlGezien
                val gezien = d.copy(ingGezien = (d.ingGezien + bron).takeLast(MAX_GEZIEN))
                val bekend = zoek(gezien, lees.winkel)
                if (bekend != null) {
                    val b = boeking(id(), lees.cent, lees.winkel, datum, bekend)
                    val nieuw = GeldBoek.voegToe(GeldBoek.voegTagToe(gezien, TAG), b)
                    return nieuw to IngUitkomst.Geboekt(b, nieuw.potje(bekend.potjeId)!!)
                }
                val w = IngWacht(id(), lees.cent, lees.winkel, datum)
                return gezien.copy(ingWacht = gezien.ingWacht + w) to IngUitkomst.Gevraagd(w)
            }
        }
    }

    /**
     * Odelia koos een potje voor een onbekende winkel. Onthoud de winkel onder [naam]
     * en boek de uitgave. Andere wachtende uitgaven bij dezelfde winkel gaan meteen mee.
     */
    fun kies(d: GeldData, wachtId: String, naam: String, potjeId: String, categorie: String, id: () -> String = GeldBoek::nieuwId): GeldData {
        val w = d.ingWacht.find { it.id == wachtId } ?: return d
        if (d.potje(potjeId) == null) return d
        val schoon = naam.trim().ifEmpty { mooi(w.winkel) }.take(40)
        val koppeling = WinkelPot(schoon, potjeId, categorie)
        var nieuw = GeldBoek.voegTagToe(d.copy(winkels = d.winkels + (sleutel(schoon) to koppeling)), TAG)
        // Deze uitgave altijd in het gekozen potje, ook als de naam anders is ingevuld.
        nieuw = GeldBoek.voegToe(nieuw, boeking(id(), w.cent, w.winkel, w.datum, koppeling))
        val rest = mutableListOf<IngWacht>()
        for (ander in nieuw.ingWacht) {
            if (ander.id == w.id) continue
            val k = zoek(nieuw, ander.winkel)
            if (k != null) nieuw = GeldBoek.voegToe(nieuw, boeking(id(), ander.cent, ander.winkel, ander.datum, k)) else rest += ander
        }
        return nieuw.copy(ingWacht = rest)
    }

    /** Niet boeken (bijv. was geen echte uitgave). */
    fun slaOver(d: GeldData, wachtId: String): GeldData = d.copy(ingWacht = d.ingWacht.filterNot { it.id == wachtId })

    /** Ander potje voor een bekende winkel. */
    fun zetPot(d: GeldData, sleutel: String, potjeId: String): GeldData {
        val w = d.winkels[sleutel] ?: return d
        if (d.potje(potjeId) == null) return d
        return d.copy(winkels = d.winkels + (sleutel to w.copy(potjeId = potjeId)))
    }

    /** Winkel vergeten: de volgende keer vraagt de app het weer. */
    fun vergeet(d: GeldData, sleutel: String): GeldData = d.copy(winkels = d.winkels - sleutel)

    fun vergeetOnbegrepen(d: GeldData, tekst: String): GeldData = d.copy(ingOnbegrepen = d.ingOnbegrepen - tekst)

    private fun boeking(id: String, cent: Long, winkel: String, datum: LocalDate, k: WinkelPot) = Boeking(
        id = id,
        soort = BoekSoort.Uit,
        cent = cent,
        potjeId = k.potjeId,
        categorie = k.categorie,
        datum = datum,
        tags = listOf(TAG),
        notitie = winkel.trim().take(60),
    )

    /** Uitleg in één zin, voor de proef-knop: "€ 4,50 bij Jumbo → 👛 Vrije geld". */
    fun proef(d: GeldData, titel: String?, tekst: String?): String = when (val l = lees(titel, tekst)) {
        IngLees.Geen -> "Dit ziet de app niet als uitgave. Er gebeurt niks."
        is IngLees.Onbegrepen -> "🤔 Lijkt op een betaling, maar ik vind geen bedrag of winkel."
        is IngLees.Betaling -> {
            val k = zoek(d, l.winkel)
            val pot = k?.let { d.potje(it.potjeId) }
            "${Geld.bedrag(l.cent)} bij ${l.winkel} → " +
                if (pot != null) "${pot.emoji} ${pot.naam} (bekend als ${k.naam})" else "❓ nieuwe winkel, de app vraagt welk potje"
        }
    }
}
