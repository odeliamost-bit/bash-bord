package nl.odelia.dashboard.logica

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import kotlin.math.floor
import org.json.JSONArray
import org.json.JSONObject

/*
 * Beleggen: advies voor Ahold Delhaize (AD.AS), een echte pot en een oefenpot.
 *
 * - Het model geeft alleen een HINT (kopen / houden / verkopen). Odelia beslist zelf.
 * - De app plaatst nooit zelf orders; die zet Odelia in DEGIRO.
 * - Alles blijft op de telefoon (beleggen.json). Gaat niet mee met de sync.
 * - Er staan GEEN echte bedragen van Odelia in de code: de repo is openbaar.
 */

data class Koers(val datum: LocalDate, val prijs: Double)

data class Order(
    val id: String = UUID.randomUUID().toString(),
    val datum: LocalDate,
    val koop: Boolean,
    val aantal: Int,
    val prijs: Double,
    val kosten: Double,
)

/**
 * Een potje: [startGeld] plus/min alle orders.
 * [startAantal] en [startPrijs] = wat er al in zat voordat de app meedeed.
 */
data class Pot(
    val startGeld: Double = 0.0,
    val startAantal: Int = 0,
    val startPrijs: Double = 0.0,
    val orders: List<Order> = emptyList(),
) {
    val aantal: Int get() = startAantal + orders.sumOf { if (it.koop) it.aantal else -it.aantal }

    val geld: Double
        get() = startGeld + orders.sumOf { if (it.koop) -(it.aantal * it.prijs + it.kosten) else it.aantal * it.prijs - it.kosten }

    /** Gemiddelde aankoopprijs van wat je nu hebt (kosten tellen mee). */
    val gemiddeld: Double
        get() {
            var n = startAantal
            var totaal = startAantal * startPrijs
            for (o in orders) {
                if (o.koop) {
                    totaal += o.aantal * o.prijs + o.kosten
                    n += o.aantal
                } else if (n > 0) {
                    totaal -= totaal / n * o.aantal
                    n -= o.aantal
                }
            }
            return if (n > 0) totaal / n else 0.0
        }

    fun waarde(koers: Double) = geld + aantal * koers

    /** Winst of verlies op de aandelen die je nu hebt, in procenten. */
    fun resultaat(koers: Double): Double? = if (aantal > 0 && gemiddeld > 0) (koers / gemiddeld - 1) * 100 else null

    val laatsteOrder: LocalDate? get() = orders.maxOfOrNull { it.datum }
}

data class BeleggenInstellingen(
    /** Kosten per order bij DEGIRO, in euro. Zelf checken in DEGIRO. */
    val kosten: Double = 3.0,
    /** Verkopen als je zoveel % verlies hebt. */
    val stopVerlies: Double = 10.0,
    /** De helft verkopen als je zoveel % winst hebt. */
    val winstNemen: Double = 15.0,
    /** Gemiddeld koersdoel van analisten (nov 2026: ca. €37–38). Zelf bijwerken. */
    val koersdoel: Double = 37.5,
    /** Na een order zoveel dagen niks doen, zodat je niet te veel handelt. */
    val rustDagen: Int = 7,
)

data class BeleggenData(
    val koersen: List<Koers> = emptyList(),
    val echt: Pot = Pot(),
    val oefen: Pot = Pot(startGeld = 400.0),
    val instellingen: BeleggenInstellingen = BeleggenInstellingen(),
) {
    val laatste: Koers? get() = koersen.maxByOrNull { it.datum }

    /** Koers toevoegen of vervangen (één per dag), op datum gesorteerd. */
    fun metKoers(k: Koers): BeleggenData =
        copy(koersen = (koersen.filter { it.datum != k.datum } + k).sortedBy { it.datum }.takeLast(800))

    fun metKoersen(nieuw: List<Koers>): BeleggenData {
        val perDag = (koersen + nieuw).associateBy { it.datum }
        return copy(koersen = perDag.values.sortedBy { it.datum }.takeLast(800))
    }
}

enum class Soort(val woord: String, val emoji: String) {
    Kopen("KOPEN", "🟢"),
    Houden("HOUDEN", "🟡"),
    Verkopen("VERKOPEN", "🔴"),
}

data class Signaal(
    val soort: Soort,
    /** Hoeveel aandelen (0 bij houden). */
    val aantal: Int,
    /** Korte zin, past in een melding: "Ahold €35,30 – HOUDEN. Niks doen." */
    val kort: String,
    /** Redenen met een emoji ervoor. */
    val redenen: List<String>,
    val punten: Int,
)

object Beleggen {
    const val NAAM = "Ahold"
    const val LINK = "https://query1.finance.yahoo.com/v8/finance/chart/AD.AS?range=2y&interval=1d"

    fun gemiddelde(p: List<Double>, n: Int): Double? = if (p.size < n) null else p.takeLast(n).average()

    /** RSI: boven 70 = snel gestegen, onder 30 = hard gedaald. */
    fun rsi(p: List<Double>, n: Int = 14): Double? {
        if (p.size < n + 1) return null
        val stukje = p.takeLast(n + 1)
        var op = 0.0
        var neer = 0.0
        for (i in 1 until stukje.size) {
            val d = stukje[i] - stukje[i - 1]
            if (d > 0) op += d else neer -= d
        }
        if (neer == 0.0) return 100.0
        val rs = (op / n) / (neer / n)
        return 100 - 100 / (1 + rs)
    }

    fun euro(x: Double): String = "€" + String.format(java.util.Locale.GERMANY, "%.2f", x)
    private fun pct(x: Double): String = String.format(java.util.Locale.GERMANY, "%+.1f%%", x)

    /**
     * Het model. Kijkt alleen naar koersen tot en met vandaag (dus ook bruikbaar voor de test).
     * Punten: trend, RSI en koersdoel. Stop-verlies en winst nemen gaan altijd voor.
     */
    fun signaal(koersen: List<Koers>, pot: Pot, inst: BeleggenInstellingen, vandaag: LocalDate? = null): Signaal? {
        val laatste = koersen.lastOrNull() ?: return null
        val p = koersen.map { it.prijs }
        val prijs = laatste.prijs
        val dag = vandaag ?: laatste.datum
        val redenen = mutableListOf<String>()
        var punten = 0

        val g20 = gemiddelde(p, 20)
        val g50 = gemiddelde(p, 50)
        if (g20 != null && g50 != null) {
            if (g20 > g50) { punten++; redenen += "📈 De trend gaat omhoog." } else { punten--; redenen += "📉 De trend gaat omlaag." }
        } else if (g20 != null) {
            if (prijs > g20) { punten++; redenen += "📈 Koers boven het gemiddelde van 20 dagen." } else { punten--; redenen += "📉 Koers onder het gemiddelde van 20 dagen." }
        } else {
            redenen += "⏳ Nog te weinig koersen voor de trend (haal de koers op)."
        }

        rsi(p)?.let {
            when {
                it < 30 -> { punten++; redenen += "🧊 Hard gedaald, misschien goedkoop (RSI ${it.toInt()})." }
                it > 70 -> { punten--; redenen += "🔥 Snel gestegen, misschien te duur (RSI ${it.toInt()})." }
            }
        }

        val ruimte = (inst.koersdoel / prijs - 1) * 100
        when {
            ruimte >= 12 -> { punten++; redenen += "🎯 Analisten zien ${pct(ruimte)} ruimte (doel ${euro(inst.koersdoel)})." }
            ruimte <= 0 -> { punten--; redenen += "🎯 Koers zit al boven het koersdoel (${euro(inst.koersdoel)})." }
        }

        // Eerst je eigen grenzen.
        val res = pot.resultaat(prijs)
        if (res != null && res <= -inst.stopVerlies) {
            return Signaal(Soort.Verkopen, pot.aantal, "$NAAM ${euro(prijs)} – VERKOPEN (${pot.aantal}). Stop-verlies.",
                listOf("🛑 Je staat ${pct(res)}. Je grens is -${inst.stopVerlies.toInt()}%.") + redenen, punten)
        }
        if (res != null && res >= inst.winstNemen && pot.aantal >= 2) {
            val helft = pot.aantal / 2
            return Signaal(Soort.Verkopen, helft, "$NAAM ${euro(prijs)} – VERKOPEN ($helft). Winst nemen.",
                listOf("💰 Je staat ${pct(res)}. Zet de helft van de winst veilig.") + redenen, punten)
        }

        // Rustig aan: niet te vaak handelen.
        val rust = pot.laatsteOrder?.let { java.time.temporal.ChronoUnit.DAYS.between(it, dag) < inst.rustDagen } ?: false

        if (punten >= 2 && !rust) {
            val kan = floor((pot.geld - inst.kosten) / prijs).toInt()
            if (kan >= 1) {
                val kostenPct = inst.kosten / (kan * prijs) * 100
                if (kostenPct <= 2.0) {
                    return Signaal(Soort.Kopen, kan, "$NAAM ${euro(prijs)} – KOPEN ($kan). Signaal positief.",
                        redenen + "🧾 Kosten ${euro(inst.kosten)} = ${String.format(java.util.Locale.GERMANY, "%.1f", kostenPct)}% van de order.", punten)
                }
                redenen += "🧾 Order te klein: de kosten eten de winst op."
            } else {
                redenen += "👛 Positief, maar er is geen geld vrij in deze pot."
            }
        }
        if (punten <= -2 && pot.aantal > 0 && !rust) {
            return Signaal(Soort.Verkopen, pot.aantal, "$NAAM ${euro(prijs)} – VERKOPEN (${pot.aantal}). Signaal negatief.", redenen, punten)
        }
        if (rust && (punten >= 2 || punten <= -2)) redenen += "😌 Je hebt net gehandeld. Even rustig aan."
        if (redenen.isEmpty()) redenen += "😐 Geen duidelijk signaal."
        return Signaal(Soort.Houden, 0, "$NAAM ${euro(prijs)} – HOUDEN. Niks doen.", redenen, punten)
    }

    fun voerUit(pot: Pot, s: Signaal, prijs: Double, datum: LocalDate, kosten: Double): Pot =
        if (s.soort == Soort.Houden || s.aantal <= 0) pot
        else pot.copy(orders = pot.orders + Order(datum = datum, koop = s.soort == Soort.Kopen, aantal = s.aantal, prijs = prijs, kosten = kosten))

    data class Test(
        val van: LocalDate,
        val tot: LocalDate,
        val model: Double,
        val vasthouden: Double,
        val orders: Int,
        val start: Double,
    ) {
        val modelPct get() = (model / start - 1) * 100
        val vasthoudenPct get() = (vasthouden / start - 1) * 100
    }

    /**
     * Test het model op oude koersen: begin met [start] euro, volg elke dag het advies.
     * Vergelijk met: op dag één alles kopen en niks meer doen.
     */
    fun test(koersen: List<Koers>, inst: BeleggenInstellingen, start: Double = 400.0, aanloop: Int = 50): Test? {
        if (koersen.size < aanloop + 10) return null
        var pot = Pot(startGeld = start)
        for (i in aanloop until koersen.size) {
            val tot = koersen.subList(0, i + 1)
            val k = tot.last()
            val s = signaal(tot, pot, inst) ?: continue
            pot = voerUit(pot, s, k.prijs, k.datum, inst.kosten)
        }
        val eerste = koersen[aanloop]
        val laatste = koersen.last()
        val n = floor((start - inst.kosten) / eerste.prijs).toInt()
        val vast = start - inst.kosten - n * eerste.prijs + n * laatste.prijs
        return Test(eerste.datum, laatste.datum, pot.waarde(laatste.prijs), vast, pot.orders.size, start)
    }

    /** Leest het antwoord van Yahoo Finance (chart, dagkoersen). Lege of kapotte dagen slaat hij over. */
    fun leesYahoo(tekst: String, zone: ZoneId = ZoneId.of("Europe/Amsterdam")): List<Koers> {
        val r = JSONObject(tekst).getJSONObject("chart").getJSONArray("result").getJSONObject(0)
        val tijden = r.getJSONArray("timestamp")
        val slot = r.getJSONObject("indicators").getJSONArray("quote").getJSONObject(0).getJSONArray("close")
        return (0 until minOf(tijden.length(), slot.length())).mapNotNull { i ->
            if (slot.isNull(i)) return@mapNotNull null
            val p = slot.optDouble(i)
            if (p.isNaN() || p <= 0) return@mapNotNull null
            Koers(Instant.ofEpochSecond(tijden.getLong(i)).atZone(zone).toLocalDate(), p)
        }.associateBy { it.datum }.values.sortedBy { it.datum }
    }

    /** Ideeën voor andere aandelen. Algemene uitleg, geen advies: zelf checken. */
    data class Idee(val naam: String, val emoji: String, val wat: String, val waarom: String, val risico: String)

    val ideeen = listOf(
        Idee("ASML", "🔬", "Maakt machines om computerchips te maken.", "Wereldleider, bijna geen concurrenten.", "Schommelt hard; China-regels kunnen pijn doen."),
        Idee("Wolters Kluwer", "📚", "Software en informatie voor artsen, juristen en boekhouders.", "Vaste abonnementen, stabiel.", "AI-tools kunnen klanten afpakken."),
        Idee("Unilever", "🧴", "Dove, Knorr, Ben & Jerry's-achtige merken.", "Rustig aandeel met dividend.", "Groeit langzaam."),
        Idee("Heineken", "🍺", "Bier over de hele wereld.", "Sterk merk, dividend.", "Mensen drinken minder alcohol."),
        Idee("KPN", "📱", "Telefoon en internet in Nederland.", "Stabiel dividend.", "Weinig groei, veel schulden."),
        Idee("Adyen", "💳", "Betalingen voor webwinkels en winkels.", "Groeit snel.", "Duur en schommelt heel hard."),
    )

    val etfTips = listOf(
        "🌍 All-World is je basis. Ahold en ASML zitten daar al in.",
        "📅 Elke maand een vast bedrag bijleggen werkt het best.",
        "🧘 Niet verkopen als de koers zakt.",
        "⚖️ Eén keer per jaar je verdeling aandelen/obligaties checken.",
        "🧾 Weinig handelen = weinig kosten.",
    )
}

object BeleggenJson {
    private fun potJson(p: Pot) = JSONObject()
        .put("startGeld", p.startGeld).put("startAantal", p.startAantal).put("startPrijs", p.startPrijs)
        .put("orders", JSONArray().apply {
            p.orders.forEach {
                put(JSONObject().put("id", it.id).put("datum", it.datum.toString()).put("koop", it.koop)
                    .put("aantal", it.aantal).put("prijs", it.prijs).put("kosten", it.kosten))
            }
        })

    private fun leesPot(o: JSONObject?, standaard: Pot): Pot {
        if (o == null) return standaard
        val a = o.optJSONArray("orders") ?: JSONArray()
        val orders = (0 until a.length()).mapNotNull { i ->
            val x = a.optJSONObject(i) ?: return@mapNotNull null
            try {
                Order(x.getString("id"), LocalDate.parse(x.getString("datum")), x.getBoolean("koop"), x.getInt("aantal"), x.getDouble("prijs"), x.optDouble("kosten", 0.0))
            } catch (_: Exception) { null }
        }
        return Pot(o.optDouble("startGeld", standaard.startGeld), o.optInt("startAantal", 0), o.optDouble("startPrijs", 0.0), orders)
    }

    fun schrijf(d: BeleggenData): String = JSONObject()
        .put("versie", 1)
        .put("koersen", JSONArray().apply { d.koersen.forEach { put(JSONArray().put(it.datum.toString()).put(it.prijs)) } })
        .put("echt", potJson(d.echt))
        .put("oefen", potJson(d.oefen))
        .put("instellingen", JSONObject()
            .put("kosten", d.instellingen.kosten).put("stopVerlies", d.instellingen.stopVerlies)
            .put("winstNemen", d.instellingen.winstNemen).put("koersdoel", d.instellingen.koersdoel)
            .put("rustDagen", d.instellingen.rustDagen))
        .toString()

    fun lees(tekst: String): BeleggenData {
        val o = try { JSONObject(tekst) } catch (_: Exception) { return BeleggenData() }
        val k = o.optJSONArray("koersen") ?: JSONArray()
        val koersen = (0 until k.length()).mapNotNull { i ->
            try { val r = k.getJSONArray(i); Koers(LocalDate.parse(r.getString(0)), r.getDouble(1)) } catch (_: Exception) { null }
        }.sortedBy { it.datum }
        val s = BeleggenInstellingen()
        val i = o.optJSONObject("instellingen")
        val inst = if (i == null) s else BeleggenInstellingen(
            i.optDouble("kosten", s.kosten), i.optDouble("stopVerlies", s.stopVerlies),
            i.optDouble("winstNemen", s.winstNemen), i.optDouble("koersdoel", s.koersdoel), i.optInt("rustDagen", s.rustDagen),
        )
        return BeleggenData(koersen, leesPot(o.optJSONObject("echt"), Pot()), leesPot(o.optJSONObject("oefen"), Pot(startGeld = 400.0)), inst)
    }
}
