package nl.odelia.dashboard.logica

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

// Rekenwerk voor het Geld-scherm. Geen Android, geen netwerk: alleen rekenen.
// Bedragen zijn altijd hele centen (Long), nooit Double. Zo raakt er geen cent zoek.

enum class BoekSoort { Uit, In, Over }

/**
 * Een potje. [echteRekening] = het is ook echt een (spaar)rekening bij de bank.
 * De app verplaatst nooit echt geld; bij zo'n potje komt alleen een geheugensteuntje.
 */
data class Potje(
    val id: String,
    val naam: String,
    val emoji: String,
    val startCent: Long = 0,
    /** Vast bedrag bij verdelen, bv. Zorg € 180. */
    val vastCent: Long? = null,
    /** Deel van de rest bij verdelen, bv. Investeren 30. */
    val procent: Int? = null,
    val echteRekening: Boolean = false,
    /** Naam van die echte rekening, bv. "ING-spaarrekening" of "Giro-rekening". */
    val rekening: String? = null,
)

/**
 * Eén keer geld. Uit: van [potjeId] af. In: bij [potjeId] erbij.
 * Over: van [potjeId] naar [naarPotjeId].
 */
data class Boeking(
    val id: String,
    val soort: BoekSoort,
    val cent: Long,
    val potjeId: String,
    val categorie: String,
    val datum: LocalDate,
    val naarPotjeId: String? = null,
    val tags: List<String> = emptyList(),
    val notitie: String? = null,
)

data class Categorie(val id: String, val label: String, val emoji: String, val kleur: Long, val vaak: Boolean = false)

enum class GeldPeriode { Dag, Week, Maand }

data class CategorieTotaal(val categorie: String, val cent: Long, val procent: Int)

/** Uitkomst van verdelen. [over] = wat niet verdeeld is (procenten samen onder 100). */
data class Verdeling(val perPotje: Map<String, Long>, val rest: Long, val over: Long, val procentSamen: Int) {
    val klopt get() = procentSamen == 100
}

object Geld {
    /** Categorieën uit het ontwerp (ontwerp/Geld.dc.html). [Categorie.vaak] = meteen zichtbaar. */
    val uitgaven = listOf(
        Categorie("lekkers", "Lekkers", "🍫", 0xFF8EC5F5, vaak = true),
        Categorie("werk", "Lunch werk", "🥪", 0xFFF6CF5A, vaak = true),
        Categorie("kleding", "Kleding", "👕", 0xFFF7A8C4, vaak = true),
        Categorie("shop", "Shoppen", "🛍️", 0xFFF2A7D8, vaak = true),
        Categorie("abo", "Abonnement", "📱", 0xFFB9A3F0, vaak = true),
        Categorie("bood", "Boodschap", "🧺", 0xFF9ACFAE, vaak = true),
        Categorie("verz", "Verzorging", "🧴", 0xFFF8C29C, vaak = true),
        Categorie("zorg", "Zorg", "🩺", 0xFF9AD1D4),
        Categorie("cadeau", "Cadeaus", "🎁", 0xFFA9C99A),
        Categorie("uitje", "Uitjes", "⛵", 0xFF8EC5F5),
        Categorie("hand", "Handwerk", "🧶", 0xFFF7B7AE),
        Categorie("kerk", "Kerk/goed doel", "⛪", 0xFFD2BDF0),
        Categorie("wens", "Wensenlijst", "✨", 0xFFF7DA8A),
        Categorie("ov", "Vervoer", "🚆", 0xFF9AA8F0),
        Categorie("plant", "Planten", "🪴", 0xFFB9DDB0),
        Categorie("rek", "Rekeningen", "📄", 0xFFD9CFC6),
    )
    val inkomen = listOf(
        Categorie("loon", "Loon", "💶", 0xFF9ACFAE, vaak = true),
        Categorie("toeslag", "Zorgtoeslag", "🏥", 0xFF9AD1D4, vaak = true),
        Categorie("terug", "Terug", "↩️", 0xFF8EC5F5, vaak = true),
        Categorie("oma", "Gekregen", "🎁", 0xFFF7A8C4, vaak = true),
        Categorie("verkocht", "Verkocht", "🏷️", 0xFFF6CF5A, vaak = true),
    )
    val sparen = listOf(
        Categorie("spaar", "Spaarrekening", "🐷", 0xFFF7A8C4, vaak = true),
        Categorie("nood", "Noodrekening", "🛟", 0xFFF49A8E, vaak = true),
        Categorie("beleg", "Beleggen", "📈", 0xFF8E9CF0, vaak = true),
    )

    fun categorieen(soort: BoekSoort) = when (soort) {
        BoekSoort.Uit -> uitgaven
        BoekSoort.In -> inkomen
        BoekSoort.Over -> sparen
    }

    /** Wat er nu in [potje] zit. */
    fun saldo(potje: Potje, boekingen: List<Boeking>): Long =
        potje.startCent + boekingen.sumOf { b ->
            when {
                b.soort == BoekSoort.In && b.potjeId == potje.id -> b.cent
                b.soort == BoekSoort.Uit && b.potjeId == potje.id -> -b.cent
                b.soort == BoekSoort.Over && b.potjeId == potje.id && b.naarPotjeId != potje.id -> -b.cent
                b.soort == BoekSoort.Over && b.naarPotjeId == potje.id && b.potjeId != potje.id -> b.cent
                else -> 0L
            }
        }

    /** Alles bij elkaar ("Totaal" bovenaan). */
    fun totaal(potjes: List<Potje>, boekingen: List<Boeking>): Long = potjes.sumOf { saldo(it, boekingen) }

    /**
     * Verdeel [cent] over de potjes: eerst de vaste bedragen (in volgorde), dan de rest in procenten.
     * Is er te weinig voor alle vaste bedragen, dan krijgen de laatste minder.
     * Afronden: de losse centen gaan naar de potjes met het grootste stukje achter de komma,
     * zodat het samen precies klopt.
     */
    fun verdeel(cent: Long, potjes: List<Potje>): Verdeling {
        val uit = linkedMapOf<String, Long>()
        var nog = maxOf(cent, 0L)
        for (p in potjes) {
            val vast = p.vastCent ?: continue
            val deel = minOf(vast, nog)
            uit[p.id] = deel
            nog -= deel
        }
        val rest = nog
        val metProcent = potjes.filter { it.vastCent == null && (it.procent ?: 0) > 0 }
        val procentSamen = metProcent.sumOf { it.procent ?: 0 }
        // Bij meer dan 100% delen we naar verhouding, zodat er nooit meer uitgaat dan er is.
        val noemer = maxOf(procentSamen, 100).toLong()
        val delen = metProcent.map { p -> p.id to rest * (p.procent ?: 0) }
        val heel = delen.associate { (id, teller) -> id to teller / noemer }.toMutableMap()
        val doel = rest * minOf(procentSamen, 100) / 100
        var losse = doel - heel.values.sum()
        delen.sortedByDescending { (_, teller) -> teller % noemer }.forEach { (id, _) ->
            if (losse > 0) { heel[id] = heel.getValue(id) + 1; losse-- }
        }
        uit.putAll(heel)
        return Verdeling(uit, rest, rest - heel.values.sum(), procentSamen)
    }

    /** Eerste en laatste dag van de periode rond [dag]. Een week is maandag t/m zondag. */
    fun bereik(periode: GeldPeriode, dag: LocalDate): ClosedRange<LocalDate> = when (periode) {
        GeldPeriode.Dag -> dag..dag
        GeldPeriode.Week -> dag.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).let { it..it.plusDays(6) }
        GeldPeriode.Maand -> dag.withDayOfMonth(1)..dag.with(TemporalAdjusters.lastDayOfMonth())
    }

    /** Uitgaven per categorie in de periode, grootste eerst. Procenten samen precies 100. */
    fun perCategorie(boekingen: List<Boeking>, periode: GeldPeriode, dag: LocalDate): List<CategorieTotaal> {
        val bereik = bereik(periode, dag)
        val som = boekingen
            .filter { it.soort == BoekSoort.Uit && it.datum in bereik }
            .groupBy { it.categorie }
            .mapValues { (_, l) -> l.sumOf { it.cent } }
            .filterValues { it > 0 }
        val totaal = som.values.sum()
        if (totaal == 0L) return emptyList()
        val procent = som.mapValues { (_, c) -> (c * 100 / totaal).toInt() }.toMutableMap()
        var losse = 100 - procent.values.sum()
        som.entries.sortedByDescending { (_, c) -> c * 100 % totaal }.forEach { (cat, _) ->
            if (losse > 0) { procent[cat] = procent.getValue(cat) + 1; losse-- }
        }
        return som.entries
            .sortedByDescending { it.value }
            .map { (cat, c) -> CategorieTotaal(cat, c, procent.getValue(cat)) }
    }

    /** "€ 4.251,72". Min ervoor bij een tekort: "− € 3,50". */
    fun bedrag(cent: Long): String {
        val positief = kotlin.math.abs(cent)
        val euro = (positief / 100).toString().reversed().chunked(3).joinToString(".").reversed()
        val c = (positief % 100).toString().padStart(2, '0')
        return (if (cent < 0) "− " else "") + "€ $euro,$c"
    }

    /**
     * Lees een getypt bedrag: "5", "5,5", "5,50", "5.50", "1.000", "1.000,00", "€ 12".
     * Een punt met precies drie cijfers erachter is een duizendtal-punt.
     * Geeft `null` als het geen bedrag is.
     */
    fun leesBedrag(tekst: String): Long? {
        val t = tekst.replace("€", "").replace(" ", "").trim()
        if (t.isEmpty() || !t.all { it.isDigit() || it == ',' || it == '.' } || t.count { it == ',' } > 1) return null
        val komma = t.lastIndexOf(',')
        val punt = t.lastIndexOf('.')
        val decimaal = when {
            komma >= 0 -> ','
            punt >= 0 && t.length - punt - 1 != 3 -> '.'
            else -> null
        }
        val heel: String
        val dec: String
        if (decimaal == null) {
            heel = t.replace(".", ""); dec = ""
        } else {
            val i = t.lastIndexOf(decimaal)
            heel = t.substring(0, i).replace(".", "")
            dec = t.substring(i + 1)
        }
        if (dec.length > 2 || dec.any { !it.isDigit() } || heel.length > 12) return null
        if (heel.isEmpty() && dec.isEmpty()) return null
        return heel.ifEmpty { "0" }.toLong() * 100 + dec.padEnd(2, '0').toLong()
    }

    /**
     * Geheugensteuntje als een echte bankrekening meedoet. De app maakt zelf nooit geld over.
     * `null` als alles alleen in de app is.
     */
    fun geheugensteuntje(boeking: Boeking, potjes: List<Potje>): String? {
        if (boeking.soort != BoekSoort.Over) return null
        val van = potjes.find { it.id == boeking.potjeId }
        val naar = potjes.find { it.id == boeking.naarPotjeId }
        if (van?.echteRekening != true && naar?.echteRekening != true) return null
        return "Vergeet niet: maak ${bedrag(boeking.cent)} over in de ING-app, " +
            "van ${van?.let(::naamMetRekening) ?: "?"} naar ${naar?.let(::naamMetRekening) ?: "?"}."
    }

    /** "Spaarpot (ING-spaarrekening)", of alleen de naam als er geen rekening bij hoort. */
    fun naamMetRekening(p: Potje): String = p.rekening?.let { "${p.naam} ($it)" } ?: p.naam

    /** Na verdelen: wat moet ze echt overmaken in de ING-app? Eén regel per echte rekening. */
    fun overmaken(v: Verdeling, potjes: List<Potje>): List<String> = potjes
        .filter { it.echteRekening && (v.perPotje[it.id] ?: 0L) > 0L }
        .map { p -> "${bedrag(v.perPotje.getValue(p.id))} naar ${p.rekening ?: "de rekening"} (${p.naam})" }

    /**
     * Alle boekingen als tabel (CSV) om te delen met Google Sheets of Drive.
     * Uitgaven zijn negatief. Tekst die met = + - @ begint krijgt een ' ervoor,
     * zodat een spreadsheet het niet als formule uitvoert.
     */
    fun csv(boekingen: List<Boeking>, potjes: List<Potje>): String {
        val naam = potjes.associate { it.id to it.naam }
        val alleCat = (uitgaven + inkomen + sparen).associate { it.id to it.label }
        val regels = boekingen.sortedWith(compareBy({ it.datum }, { it.id })).map { b ->
            val bedrag = if (b.soort == BoekSoort.Uit) -b.cent else b.cent
            listOf(
                b.datum.toString(),
                when (b.soort) { BoekSoort.Uit -> "uitgave"; BoekSoort.In -> "inkomen"; BoekSoort.Over -> "overboeken" },
                "\"" + (if (bedrag < 0) "-" else "") + bedrag(kotlin.math.abs(bedrag)).removePrefix("€ ").replace(".", "") + "\"",
                tekstVeld(alleCat[b.categorie] ?: b.categorie),
                tekstVeld(naam[b.potjeId] ?: ""),
                tekstVeld(b.naarPotjeId?.let { naam[it] } ?: ""),
                tekstVeld(b.tags.joinToString(", ")),
                tekstVeld(b.notitie ?: ""),
            ).joinToString(",")
        }
        return (listOf("datum,soort,bedrag,categorie,potje,naar,tags,notitie") + regels).joinToString("\r\n", postfix = "\r\n")
    }

    private fun tekstVeld(t: String): String {
        val veilig = if (t.isNotEmpty() && t[0] in "=+-@\t\r") "'$t" else t
        return "\"" + veilig.replace("\"", "\"\"") + "\""
    }
}
