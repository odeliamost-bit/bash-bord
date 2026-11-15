package nl.odelia.dashboard.logica

import java.time.LocalDate
import org.json.JSONArray
import org.json.JSONObject

// Geld uit bank-meldingen (nov 2026). Hier alleen tekst lezen en rekenen: geen Android, geen netwerk.
// De app leest alleen meldingen van de ING-app (meldingen/BankLuisteraar.kt), en alleen als Odelia het aanzet.
// Let op: de precieze tekst van ING-meldingen is niet bekend. Daarom leest [BankMelding] ruim,
// en bewaart [BankData.nietHerkend] wat hij niet begreep, zodat we het later kunnen verbeteren.

/** Wat er in één bank-melding staat. [soort] is Uit (afschrijving) of In (bijschrijving). */
data class BankBericht(val soort: BoekSoort, val cent: Long, val naam: String)

/** Onthouden keuze: deze winkel (of afzender) hoort bij dit potje en deze categorie. */
data class BankRegel(val soort: BoekSoort, val sleutel: String, val naam: String, val potjeId: String, val categorie: String)

/** Een betaling die wacht tot Odelia een potje (bij Uit) of een soort inkomen (bij In) kiest. */
data class BankWacht(val id: String, val soort: BoekSoort, val cent: Long, val naam: String, val datum: LocalDate)

/** Een melding die al verwerkt is (Android stuurt dezelfde melding soms twee keer). */
data class BankGezien(val merk: String, val tijd: Long)

/** Eén keuze-knopje: [id] is een potje (bij Uit) of een categorie (bij In). */
data class BankKeuze(val id: String, val label: String)

/**
 * Alles van "Geld uit meldingen". Staat alleen op dit apparaat (bank.json), gaat nooit online.
 * Alleen de boeking zelf komt bij Geld terecht (en gaat zo mee in de sync).
 */
data class BankData(
    val aan: Boolean = false,
    val regels: List<BankRegel> = emptyList(),
    val wachtrij: List<BankWacht> = emptyList(),
    val gezien: List<BankGezien> = emptyList(),
    val nietHerkend: List<String> = emptyList(),
)

/** Een bank-melding lezen: bedrag, af of bij, en de naam van de winkel of afzender. */
object BankMelding {
    /** De ING-app (ING Bankieren). Alleen meldingen van deze app worden gelezen. */
    const val ING = "com.ing.mobile"

    private const val GETAL = """((?:\d{1,3}(?:\.\d{3})+|\d+)(?:,\d{1,2})?)"""
    private val bedragVoor = Regex("""(?:€|EUR)\s?([-−])?\s?$GETAL""", RegexOption.IGNORE_CASE)
    private val bedragNa = Regex("""$GETAL\s?(?:€|EUR\b|euro\b)""", RegexOption.IGNORE_CASE)

    private val woordenIn = listOf("bijgeschreven", "bijschrijving", "ontvangen", "gestort", "storting", "terugbetal", "teruggestort", "teruggeboekt")
    private val woordenUit = listOf("afgeschreven", "afschrijving", "betaald", "betaling", "gepind", "afgeboekt", "incasso", "overgemaakt", "overboeking", "uitgegeven")

    /** Meldingen die wel een bedrag noemen, maar geen echte af- of bijschrijving zijn. */
    private val geenBoeking = listOf(
        "betaalverzoek", "verzoek", "bevestig", "goedkeur", "akkoord", "autoris", "code", "limiet",
        "reserver", "gepland", "herinnering", "mislukt", "geweigerd", "niet gelukt", "aanbieding", "korting",
    )

    private val naamUit = Regex("""\b(?:bij|aan|naar|door)\s+(.+)""", RegexOption.IGNORE_CASE)
    private val naamIn = Regex("""\b(?:van|door)\s+(.+)""", RegexOption.IGNORE_CASE)
    private val naamStop = Regex(
        """\s+(?:op|van|naar|met|via|voor)\s+(?:je|jouw|uw|de|het|rekening|betaalrekening|spaarrekening|betaalpas|pas|nl\d\d)\b.*|\s+met\s.*|(?<=[\p{L}\d]{3})\.\s.*|:\s.*|\s*[,(–-]?\s*(?:€|EUR\b).*|[\n|].*""",
        RegexOption.IGNORE_CASE,
    )
    private val geenNaam = Regex("""^(?:€|eur\b|\d|je\b|jouw\b|uw\b|de\b|het\b|rekening|betaalrekening|spaarrekening|nl\d\d).*""", RegexOption.IGNORE_CASE)

    private fun samen(titel: String?, tekst: String?) = listOfNotNull(titel, tekst).joinToString("\n").trim()

    private fun bedrag(alles: String): Pair<Long, Boolean>? {
        bedragVoor.find(alles)?.let { m ->
            Geld.leesBedrag(m.groupValues[2])?.takeIf { it > 0 }?.let { return it to m.groupValues[1].isNotEmpty() }
        }
        bedragNa.find(alles)?.let { m ->
            Geld.leesBedrag(m.groupValues[1])?.takeIf { it > 0 }?.let { return it to false }
        }
        return null
    }

    private fun overslaan(klein: String) = geenBoeking.any { it in klein }

    /** `null` = geen af- of bijschrijving (of niet begrepen, zie [nietBegrepen]). */
    fun lees(titel: String?, tekst: String?): BankBericht? {
        val alles = samen(titel, tekst)
        val klein = alles.lowercase()
        if (overslaan(klein)) return null
        val (cent, min) = bedrag(alles) ?: return null
        val soort = when {
            woordenIn.any { it in klein } -> BoekSoort.In
            woordenUit.any { it in klein } -> BoekSoort.Uit
            min -> BoekSoort.Uit
            else -> return null
        }
        return BankBericht(soort, cent, naam(titel, tekst, soort))
    }

    /** Er staat een bedrag in, het is geen reclame of verzoek, maar af of bij is niet duidelijk. */
    fun nietBegrepen(titel: String?, tekst: String?): Boolean {
        val alles = samen(titel, tekst)
        return !overslaan(alles.lowercase()) && bedrag(alles) != null && lees(titel, tekst) == null
    }

    private fun naam(titel: String?, tekst: String?, soort: BoekSoort): String {
        val zoek = if (soort == BoekSoort.In) naamIn else naamUit
        for (bron in listOfNotNull(tekst, titel)) {
            // Elke "bij/aan/van …" proberen, ook als ze in elkaar zitten ("van € 5 van Jan").
            var m = zoek.find(bron)
            while (m != null) {
                val ruw = m.groupValues[1].replace(naamStop, "").trim().trimEnd('.', ',', '!', ':', ';').trim()
                if (ruw.length >= 2 && !geenNaam.matches(ruw)) return netjes(ruw)
                m = zoek.find(bron, m.range.first + 1)
            }
        }
        // Geen naam in de zin: soms is de titel de naam van de winkel.
        val t = titel?.trim().orEmpty()
        val kleinT = t.lowercase()
        if (t.length >= 2 && bedrag(t) == null && "ing" != kleinT && (woordenIn + woordenUit).none { it in kleinT }) return netjes(t)
        return "Onbekend"
    }

    /** "ALBERT HEIJN 1234" → "Albert Heijn 1234". Namen met kleine letters blijven zoals ze zijn. */
    private fun netjes(naam: String): String {
        val n = naam.replace(Regex("\\s+"), " ").take(40).trim()
        if (n.any { it.isLowerCase() }) return n
        return n.lowercase().split(' ').joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }
    }

    /** Waaraan de app een winkel herkent: de eerste twee woorden, kleine letters, zonder cijfers. Leeg = niet te herkennen. */
    fun sleutel(naam: String): String {
        val woorden = naam.lowercase().map { if (it.isLetter()) it else ' ' }.joinToString("").split(' ').filter { it.isNotEmpty() }
        if (woorden.isEmpty() || naam == "Onbekend") return ""
        return woorden.take(2).joinToString(" ")
    }

    private val gok = listOf(
        "bood" to listOf("albert heijn", "ah to go", "jumbo", "lidl", "aldi", "plus ", "coop", "spar", "dirk", "picnic", "poiesz", "supermarkt"),
        "verz" to listOf("kruidvat", "etos", "trekpleister", "rituals", "kapper", "drogist"),
        "ov" to listOf("ns ", "ns-", "ov-chip", "ovpay", "arriva", "qbuzz", "gvb", "ret ", "connexxion", "flixbus"),
        "zorg" to listOf("apotheek", "tandarts", "huisarts", "ziekenhuis", "fysio", "zilveren kruis", "menzis", "vgz", "zorgverzekering"),
        "kleding" to listOf("h&m", "c&a", "zeeman", "primark", "wibra", "zalando", "vinted", "we fashion"),
        "abo" to listOf("simyo", "kpn", "odido", "vodafone", "spotify", "netflix", "disney", "videoland", "youtube", "google one"),
        "plant" to listOf("intratuin", "tuincentrum", "groenrijk", "bloemen"),
        "shop" to listOf("bol.com", "bol com", "amazon", "action", "hema", "ikea", "coolblue", "mediamarkt"),
    )

    /** Een eerste gok voor de categorie, op de naam van de winkel. `null` = geen idee. */
    fun gokCategorie(naam: String): String? {
        // Alleen aan het begin van een woord: "ns " past niet op "Jansens".
        val n = " " + naam.lowercase() + " "
        return gok.firstOrNull { (_, woorden) -> woorden.any { " $it" in n } }?.first
    }
}

/** De regels van "Geld uit meldingen": wanneer vanzelf boeken, wanneer vragen, en wat er na een keuze gebeurt. */
object Bank {
    private const val GEZIEN_MAX = 40
    private const val NIET_HERKEND_MAX = 5
    private const val DUBBEL_MS = 60_000L

    /** Zelfde melding, binnen een minuut nog een keer: dubbel. */
    fun alGezien(d: BankData, merk: String, tijd: Long): Boolean =
        d.gezien.any { it.merk == merk && kotlin.math.abs(tijd - it.tijd) < DUBBEL_MS }

    fun zetGezien(d: BankData, merk: String, tijd: Long): BankData =
        d.copy(gezien = (d.gezien.filterNot { it.merk == merk } + BankGezien(merk, tijd)).takeLast(GEZIEN_MAX))

    fun onthoudNietHerkend(d: BankData, tekst: String): BankData =
        d.copy(nietHerkend = (d.nietHerkend + tekst.take(300)).takeLast(NIET_HERKEND_MAX))

    /** De onthouden keuze voor deze winkel, als het potje nog bestaat. */
    fun regelVoor(d: BankData, geld: GeldData, bericht: BankBericht): BankRegel? {
        val s = BankMelding.sleutel(bericht.naam)
        if (s.isEmpty()) return null
        return d.regels.firstOrNull { it.soort == bericht.soort && it.sleutel == s && geld.potje(it.potjeId) != null }
    }

    /** De boeking die bij Geld komt. "🔔" in de notitie = kwam uit een melding. */
    fun boeking(id: String, soort: BoekSoort, cent: Long, naam: String, datum: LocalDate, potjeId: String, categorie: String) =
        Boeking(id, soort, cent, potjeId, categorie, datum, notitie = "🔔 $naam")

    /** Het potje waar nieuw geld binnenkomt: het losse potje ("Inkomsten"), anders het eerste. */
    fun inkomstenPotje(geld: GeldData): String? =
        (GeldBoek.lossePotjes(geld).firstOrNull { !it.echteRekening } ?: geld.potjes.firstOrNull())?.id

    /** Categorie bij een uitgave: eerst gokken op de naam, dan wat ze bij dit potje het vaakst koos, anders Shoppen. */
    fun categorieVoor(geld: GeldData, potjeId: String, naam: String): String =
        BankMelding.gokCategorie(naam)
            ?: geld.boekingen.filter { it.soort == BoekSoort.Uit && it.potjeId == potjeId }
                .groupingBy { it.categorie }.eachCount().maxByOrNull { it.value }?.key
            ?: "shop"

    /** Alle keuzes voor een wachtende betaling. Bij Uit: de potjes. Bij In: het soort inkomen. */
    fun keuzes(geld: GeldData, soort: BoekSoort): List<BankKeuze> =
        if (soort == BoekSoort.In) Geld.inkomen.map { BankKeuze(it.id, "${it.emoji} ${it.label}") }
        else {
            val los = GeldBoek.lossePotjes(geld).map { it.id }.toSet()
            val vaak = geld.boekingen.filter { it.soort == BoekSoort.Uit }.groupingBy { it.potjeId }.eachCount()
            geld.potjes
                .sortedWith(compareBy<Potje> { it.echteRekening || it.id in los }.thenByDescending { vaak[it.id] ?: 0 })
                .map { BankKeuze(it.id, "${it.emoji} ${it.naam}".trim()) }
        }

    /**
     * Odelia koos [keuze] voor de wachtende betaling [wachtId]: maak de boeking en onthoud de keuze.
     * Geeft de nieuwe stand terug, en de boeking (of `null` als er niks te boeken valt).
     */
    fun kies(d: BankData, geld: GeldData, wachtId: String, keuze: String, boekId: String): Pair<BankData, Boeking?> {
        val w = d.wachtrij.firstOrNull { it.id == wachtId } ?: return d to null
        val (potjeId, categorie) = if (w.soort == BoekSoort.In) {
            if (Geld.inkomen.none { it.id == keuze }) return d to null
            (inkomstenPotje(geld) ?: return d to null) to keuze
        } else {
            if (geld.potje(keuze) == null) return d to null
            keuze to categorieVoor(geld, keuze, w.naam)
        }
        val sleutel = BankMelding.sleutel(w.naam)
        val regels = if (sleutel.isEmpty()) d.regels
        else d.regels.filterNot { it.soort == w.soort && it.sleutel == sleutel } + BankRegel(w.soort, sleutel, w.naam, potjeId, categorie)
        val nieuw = d.copy(regels = regels, wachtrij = d.wachtrij.filterNot { it.id == wachtId })
        return nieuw to boeking(boekId, w.soort, w.cent, w.naam, w.datum, potjeId, categorie)
    }

    fun gooiWeg(d: BankData, wachtId: String): BankData = d.copy(wachtrij = d.wachtrij.filterNot { it.id == wachtId })

    fun vergeet(d: BankData, regel: BankRegel): BankData =
        d.copy(regels = d.regels.filterNot { it.soort == regel.soort && it.sleutel == regel.sleutel })

    /** Titel van de vraag-melding: "€ 12,50 bij Albert Heijn" of "€ 100,00 van J. Jansen". */
    fun vraagTitel(w: BankWacht): String = "${Geld.bedrag(w.cent)} ${if (w.soort == BoekSoort.In) "van" else "bij"} ${w.naam}"
}

/** "Geld uit meldingen" naar tekst (JSON) en terug. Kapotte stukjes worden overgeslagen. */
object BankJson {
    fun schrijf(d: BankData): String = JSONObject().apply {
        put("aan", d.aan)
        put("regels", JSONArray().apply {
            d.regels.forEach { r ->
                put(JSONObject().apply {
                    put("soort", r.soort.name); put("sleutel", r.sleutel); put("naam", r.naam); put("potje", r.potjeId); put("categorie", r.categorie)
                })
            }
        })
        put("wachtrij", JSONArray().apply {
            d.wachtrij.forEach { w ->
                put(JSONObject().apply {
                    put("id", w.id); put("soort", w.soort.name); put("cent", w.cent); put("naam", w.naam); put("datum", w.datum.toString())
                })
            }
        })
        put("gezien", JSONArray().apply { d.gezien.forEach { put(JSONObject().apply { put("merk", it.merk); put("tijd", it.tijd) }) } })
        put("nietHerkend", JSONArray(d.nietHerkend))
    }.toString()

    fun lees(tekst: String): BankData {
        val o = try {
            JSONObject(tekst)
        } catch (_: Exception) {
            return BankData()
        }
        fun <T> lijst(naam: String, maak: (JSONObject) -> T): List<T> = o.optJSONArray(naam)?.let { a ->
            (0 until a.length()).mapNotNull { i ->
                try {
                    maak(a.getJSONObject(i))
                } catch (_: Exception) {
                    null
                }
            }
        }.orEmpty()
        return BankData(
            aan = o.optBoolean("aan"),
            regels = lijst("regels") { x ->
                BankRegel(BoekSoort.valueOf(x.getString("soort")), x.getString("sleutel"), x.getString("naam"), x.getString("potje"), x.getString("categorie"))
            },
            wachtrij = lijst("wachtrij") { x ->
                val cent = x.getLong("cent")
                require(cent > 0)
                BankWacht(x.getString("id"), BoekSoort.valueOf(x.getString("soort")), cent, x.getString("naam"), LocalDate.parse(x.getString("datum")))
            },
            gezien = lijst("gezien") { x -> BankGezien(x.getString("merk"), x.getLong("tijd")) },
            nietHerkend = o.optJSONArray("nietHerkend")?.let { a -> (0 until a.length()).mapNotNull { a.optString(it).ifEmpty { null } } }.orEmpty(),
        )
    }
}
