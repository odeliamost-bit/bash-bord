package nl.odelia.dashboard.logica

import java.time.LocalDate
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

/**
 * Alles van het Geld-scherm: de potjes, alle boekingen en de eigen tags.
 * Potjes zijn virtuele enveloppen. Alleen bij [Potje.echteRekening] hoort er ook een echte bankrekening bij.
 */
data class GeldData(
    val potjes: List<Potje> = GeldStart.potjes,
    val boekingen: List<Boeking> = emptyList(),
    val tags: List<String> = GeldStart.tags,
) {
    fun potje(id: String?): Potje? = potjes.find { it.id == id }

    fun saldo(id: String): Long = potje(id)?.let { Geld.saldo(it, boekingen) } ?: 0L

    val totaal: Long get() = Geld.totaal(potjes, boekingen)
}

/** Verzonnen voorbeeld-potjes voor de eerste keer. Geen echte bedragen; Odelia past ze zelf aan. */
object GeldStart {
    const val ING_SPAAR = "ING-spaarrekening"
    const val GIRO = "Giro-rekening"

    val potjes = listOf(
        Potje("inkomsten", "Inkomsten", "💜"),
        Potje("vast", "Vaste lasten", "📱", vastCent = 50_00),
        Potje("zorg", "Zorg", "🩺", vastCent = 100_00),
        Potje("lekkers", "Snoep/lekkers", "🍬", vastCent = 20_00),
        Potje("vrij", "Vrije geld", "👛", procent = 30),
        Potje("inv", "Investeren", "📈", procent = 20, echteRekening = true, rekening = GIRO),
        Potje("spaar", "Spaarpot", "🐷", procent = 20, echteRekening = true, rekening = ING_SPAAR),
        Potje("nood", "Noodpot", "🛟", procent = 20),
        Potje("kerk", "Goede doelen/kerk", "⛪", procent = 10),
    )

    val tags = listOf("Online shoppen", "School", "Merge Mansion")
}

/** Uitgaven van één dag, voor de lijst "Wat heb ik gekocht?". */
data class GeldDag(val datum: LocalDate, val boekingen: List<Boeking>)

/** Kleine bewerkingen op [GeldData]. Alles geeft een nieuwe kopie terug. */
object GeldBoek {
    fun nieuwId(): String = UUID.randomUUID().toString()

    fun voegToe(d: GeldData, b: Boeking): GeldData = d.copy(boekingen = d.boekingen + b)

    fun verwijder(d: GeldData, boekingId: String): GeldData = d.copy(boekingen = d.boekingen.filterNot { it.id == boekingId })

    /** Potje toevoegen of vervangen (zelfde id = vervangen, op dezelfde plek). */
    fun zetPotje(d: GeldData, p: Potje): GeldData =
        if (d.potjes.any { it.id == p.id }) d.copy(potjes = d.potjes.map { if (it.id == p.id) p else it })
        else d.copy(potjes = d.potjes + p)

    /** Potje weghalen mag alleen als er geen boekingen bij horen (anders raakt er geld zoek). */
    fun magWeg(d: GeldData, potjeId: String): Boolean =
        d.boekingen.none { it.potjeId == potjeId || it.naarPotjeId == potjeId }

    fun haalPotjeWeg(d: GeldData, potjeId: String): GeldData =
        if (magWeg(d, potjeId)) d.copy(potjes = d.potjes.filterNot { it.id == potjeId }) else d

    /** "Er zit nu € X in": past het startbedrag aan, zodat het saldo precies [cent] wordt. */
    fun zetSaldo(d: GeldData, potjeId: String, cent: Long): GeldData {
        val p = d.potje(potjeId) ?: return d
        val nu = Geld.saldo(p, d.boekingen)
        return zetPotje(d, p.copy(startCent = p.startCent + (cent - nu)))
    }

    fun zetProcent(d: GeldData, potjeId: String, procent: Int): GeldData {
        val p = d.potje(potjeId) ?: return d
        return zetPotje(d, p.copy(procent = procent.coerceIn(0, 100)))
    }

    fun voegTagToe(d: GeldData, tag: String): GeldData {
        val t = tag.trim()
        if (t.isEmpty() || d.tags.any { it.equals(t, ignoreCase = true) }) return d
        return d.copy(tags = d.tags + t)
    }

    /**
     * Na "Nu verdelen": voor elk potje dat iets krijgt één inkomen-boeking.
     * Alleen in de app; echt overmaken doet Odelia zelf (zie [Geld.overmaken]).
     */
    fun verdeelBoekingen(
        v: Verdeling,
        datum: LocalDate,
        van: String? = null,
        restNaar: String? = null,
        categorie: String = "loon",
        id: () -> String = ::nieuwId,
    ): List<Boeking> {
        val delen = v.perPotje.filter { (potje, cent) -> cent > 0 && potje != van }.map { (potje, cent) ->
            if (van == null) Boeking(id(), BoekSoort.In, cent, potje, categorie, datum, notitie = "Verdeeld")
            else Boeking(id(), BoekSoort.Over, cent, van, "verdeel", datum, naarPotjeId = potje, notitie = "Verdeeld")
        }
        // Wat over blijft: bij geld uit een potje blijft het daar vanzelf. Bij nieuw geld gaat het naar [restNaar].
        val rest = if (van == null && restNaar != null && v.over > 0) {
            listOf(Boeking(id(), BoekSoort.In, v.over, restNaar, categorie, datum, notitie = "Over na verdelen"))
        } else {
            emptyList()
        }
        return delen + rest
    }

    /** Potjes die niet meedoen met verdelen (geen vast bedrag, geen procent), bv. "Inkomsten". */
    fun lossePotjes(d: GeldData): List<Potje> = d.potjes.filter { it.vastCent == null && (it.procent ?: 0) == 0 }

    private val dagen = listOf("maandag", "dinsdag", "woensdag", "donderdag", "vrijdag", "zaterdag", "zondag")
    val maanden = listOf("januari", "februari", "maart", "april", "mei", "juni", "juli", "augustus", "september", "oktober", "november", "december")

    /** "vandaag", "gisteren" of "woensdag 21 okt". */
    fun dagTekst(d: LocalDate, vandaag: LocalDate): String = when (d) {
        vandaag -> "vandaag"
        vandaag.minusDays(1) -> "gisteren"
        else -> "${dagen[d.dayOfWeek.value - 1]} ${d.dayOfMonth} ${maanden[d.monthValue - 1].take(3)}"
    }

    /** "24-10" */
    fun kort(d: LocalDate): String = "${d.dayOfMonth}-${d.monthValue}"

    /** "zaterdag 24 okt", "19 okt – 25 okt" of "oktober 2026". */
    fun periodeTekst(periode: GeldPeriode, dag: LocalDate): String {
        val r = Geld.bereik(periode, dag)
        fun kortDag(d: LocalDate) = "${d.dayOfMonth} ${maanden[d.monthValue - 1].take(3)}"
        return when (periode) {
            GeldPeriode.Dag -> "${dagen[dag.dayOfWeek.value - 1]} ${kortDag(dag)}"
            GeldPeriode.Week -> "${kortDag(r.start)} – ${kortDag(r.endInclusive)}"
            GeldPeriode.Maand -> "${maanden[dag.monthValue - 1]} ${dag.year}"
        }
    }

    /** Het potje dat ze voor dit soort boeking het laatst gebruikte, anders een logische keuze. */
    fun laatstePotje(d: GeldData, soort: BoekSoort): String? {
        val laatst = d.boekingen.lastOrNull { it.soort == soort }?.potjeId
        if (laatst != null && d.potje(laatst) != null) return laatst
        val los = lossePotjes(d).firstOrNull { !it.echteRekening }
        val vrij = d.potjes.firstOrNull { (it.procent ?: 0) > 0 && !it.echteRekening }
        val keuze = when (soort) {
            BoekSoort.In -> los ?: vrij
            BoekSoort.Uit, BoekSoort.Over -> vrij ?: los
        }
        return (keuze ?: d.potjes.firstOrNull())?.id
    }

    /** Uitgaven in de periode, per dag, nieuwste dag eerst. */
    fun uitgavenPerDag(boekingen: List<Boeking>, periode: GeldPeriode, dag: LocalDate): List<GeldDag> {
        val bereik = Geld.bereik(periode, dag)
        return boekingen
            .filter { it.soort == BoekSoort.Uit && it.datum in bereik }
            .groupBy { it.datum }
            .toSortedMap(compareByDescending { it })
            .map { (datum, l) -> GeldDag(datum, l.reversed()) }
    }

    /** Alle boekingen per dag, nieuwste eerst (voor het overzicht). */
    fun allesPerDag(boekingen: List<Boeking>): List<GeldDag> = boekingen
        .groupBy { it.datum }
        .toSortedMap(compareByDescending { it })
        .map { (datum, l) -> GeldDag(datum, l.reversed()) }

    /** Bedrag om voor te lezen: "4251 euro en 72 cent". */
    fun uitspraak(cent: Long): String {
        val min = if (cent < 0) "min " else ""
        val a = kotlin.math.abs(cent)
        val e = a / 100
        val c = a % 100
        return min + when {
            c == 0L -> "$e euro"
            e == 0L -> "$c cent"
            else -> "$e euro en $c cent"
        }
    }

    /**
     * Ingesproken tekst ("5,50 euro bij de Ah") → bedrag en de rest als notitie ("bij de Ah").
     * Geen getal gevonden: bedrag `null`, alles wordt notitie.
     */
    fun uitGesproken(tekst: String): Pair<Long?, String> {
        val getal = Regex("""(\d+(?:[.,]\d{1,2})?)\s*(euro|eur|€)?""", RegexOption.IGNORE_CASE).find(tekst)
            ?: return null to tekst.trim()
        val cent = Geld.leesBedrag(getal.groupValues[1].replace('.', ','))
        val rest = tekst.removeRange(getal.range).replace("€", "").replace(Regex("\\s+"), " ").trim()
        return cent to rest
    }

    /** Volgende of vorige periode (voor de pijltjes bij Uitgaven). */
    fun schuif(periode: GeldPeriode, dag: LocalDate, stappen: Long): LocalDate = when (periode) {
        GeldPeriode.Dag -> dag.plusDays(stappen)
        GeldPeriode.Week -> dag.plusWeeks(stappen)
        GeldPeriode.Maand -> dag.plusMonths(stappen)
    }
}

/** Geld naar tekst (JSON) en terug. Kapotte stukjes worden overgeslagen, niet de hele boel. */
object GeldJson {
    private const val VERSIE = 1

    fun schrijf(d: GeldData): String = JSONObject().apply {
        put("versie", VERSIE)
        put("potjes", JSONArray().apply {
            d.potjes.forEach { p ->
                put(JSONObject().apply {
                    put("id", p.id)
                    put("naam", p.naam)
                    put("emoji", p.emoji)
                    put("start", p.startCent)
                    p.vastCent?.let { put("vast", it) }
                    p.procent?.let { put("procent", it) }
                    put("echt", p.echteRekening)
                    p.rekening?.let { put("rekening", it) }
                })
            }
        })
        put("boekingen", JSONArray().apply {
            d.boekingen.forEach { b ->
                put(JSONObject().apply {
                    put("id", b.id)
                    put("soort", b.soort.name)
                    put("cent", b.cent)
                    put("potje", b.potjeId)
                    put("categorie", b.categorie)
                    put("datum", b.datum.toString())
                    b.naarPotjeId?.let { put("naar", it) }
                    if (b.tags.isNotEmpty()) put("tags", JSONArray(b.tags))
                    b.notitie?.let { put("notitie", it) }
                })
            }
        })
        put("tags", JSONArray(d.tags))
    }.toString()

    fun lees(tekst: String): GeldData {
        val o = try {
            JSONObject(tekst)
        } catch (_: Exception) {
            return GeldData()
        }
        val potjes = o.optJSONArray("potjes")?.let { a ->
            (0 until a.length()).mapNotNull { i ->
                stukje {
                    val x = a.getJSONObject(i)
                    Potje(
                        id = x.getString("id"),
                        naam = x.getString("naam"),
                        emoji = x.optString("emoji"),
                        startCent = x.optLong("start"),
                        vastCent = if (x.has("vast")) x.getLong("vast") else null,
                        procent = if (x.has("procent")) x.getInt("procent").coerceIn(0, 100) else null,
                        echteRekening = x.optBoolean("echt"),
                        rekening = x.optString("rekening").ifEmpty { null },
                    )
                }
            }.distinctBy { it.id }
        } ?: GeldStart.potjes
        val boekingen = o.optJSONArray("boekingen")?.let { a ->
            (0 until a.length()).mapNotNull { i ->
                stukje {
                    val x = a.getJSONObject(i)
                    val cent = x.getLong("cent")
                    require(cent >= 0)
                    Boeking(
                        id = x.getString("id"),
                        soort = BoekSoort.valueOf(x.getString("soort")),
                        cent = cent,
                        potjeId = x.getString("potje"),
                        categorie = x.getString("categorie"),
                        datum = LocalDate.parse(x.getString("datum")),
                        naarPotjeId = x.optString("naar").ifEmpty { null },
                        tags = x.optJSONArray("tags")?.let { t -> (0 until t.length()).map { t.getString(it) } }.orEmpty(),
                        notitie = x.optString("notitie").ifEmpty { null },
                    )
                }
            }
        }.orEmpty()
        val tags = o.optJSONArray("tags")?.let { t -> (0 until t.length()).mapNotNull { stukje { t.getString(it) } } } ?: GeldStart.tags
        return GeldData(potjes, boekingen, tags)
    }

    private inline fun <T> stukje(maak: () -> T): T? = try {
        maak()
    } catch (_: Exception) {
        null
    }
}
