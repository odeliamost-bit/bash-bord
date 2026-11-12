package nl.odelia.dashboard.ui.beleggen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import nl.odelia.dashboard.logica.Beleggen
import nl.odelia.dashboard.logica.BeleggenData
import nl.odelia.dashboard.logica.Koers
import nl.odelia.dashboard.logica.Pot
import nl.odelia.dashboard.logica.Soort
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.AppStaat
import nl.odelia.dashboard.ui.onderdelen.EmojiRondje
import nl.odelia.dashboard.ui.onderdelen.Invoer
import nl.odelia.dashboard.ui.onderdelen.Keuze
import nl.odelia.dashboard.ui.onderdelen.KopBalk
import nl.odelia.dashboard.ui.onderdelen.OnlineVraag
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.VolleKnop
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.onderdelen.rememberVraag
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.theme.Pastels

private enum class BTab(val label: String) { Vandaag("📍 Nu"), Potten("👛 Potten"), Ideeen("💡 Ideeën"), Test("🧪 Test") }

private val Bruin = Color(0xFF5A3426)
private val dagNotatie = DateTimeFormatter.ofPattern("EEEE d MMM", Locale.forLanguageTag("nl"))

private fun getal(t: String): Double? = t.trim().replace("€", "").replace(',', '.').toDoubleOrNull()
private fun euro(x: Double) = Beleggen.euro(x)
private fun pct(x: Double) = String.format(Locale.GERMANY, "%+.1f%%", x)

/**
 * Beleggen: advies voor Ahold, je echte pot (DEGIRO) en een oefenpot.
 * Het model geeft een hint. De app plaatst nooit zelf orders.
 */
@Composable
fun BeleggenScherm(
    staat: AppStaat,
    acties: AppActies,
    onTerug: () -> Unit,
    onMelding: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tab by remember { mutableStateOf(BTab.Vandaag) }
    val vraag = rememberVraag()
    val d = staat.beleggen

    fun wijzig(wat: (BeleggenData) -> BeleggenData) = acties.wijzigBeleggen(wat)

    Box(modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 24.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                KopBalk("Beleggen", "Ahold en je potjes", onTerug) {
                    EmojiRondje("📈", Pastels.Roze.licht, Pastels.Roze.donker)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BTab.entries.forEach { t -> Keuze(t.label, tab == t, { tab = t }, Modifier.weight(1f)) }
                }
                when (tab) {
                    BTab.Vandaag -> Vandaag(d, ::wijzig, onMelding) {
                        vraag.stel(OnlineVraag(
                            titel = "Koers ophalen?",
                            uitleg = "De app haalt de koers van Ahold op bij Yahoo Finance.\nEr gaat alleen de code AD.AS mee.",
                            ja = "Ja, ophalen",
                        ) { onMelding("⏳ Bezig…"); acties.haalKoers(onMelding) })
                    }
                    BTab.Potten -> Potten(d, ::wijzig, onMelding)
                    BTab.Ideeen -> Ideeen()
                    BTab.Test -> TestTab(d, ::wijzig, onMelding)
                }
            }
        }
        vraag.Toon()
    }
}

@Composable
private fun Kaart(kleur: Color = Kleur.Kaart, inhoud: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().klei(kleur, RoundedCornerShape(26.dp), Kleur.KaartSchaduw).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = inhoud,
    )
}

@Composable
private fun Regel(tekst: String) = Tekst(tekst, 17.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht)

@Composable
private fun Vandaag(d: BeleggenData, wijzig: ((BeleggenData) -> BeleggenData) -> Unit, onMelding: (String) -> Unit, onHaal: () -> Unit) {
    var echt by remember { mutableStateOf(true) }
    var invoer by remember { mutableStateOf("") }
    val pot = if (echt) d.echt else d.oefen
    val s = Beleggen.signaal(d.koersen, pot, d.instellingen, LocalDate.now())
    val laatste = d.laatste

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Keuze("🏦 Echte pot", echt, { echt = true }, Modifier.weight(1f))
        Keuze("🎮 Oefenpot", !echt, { echt = false }, Modifier.weight(1f))
    }

    if (s == null || laatste == null) {
        Kaart { Tekst("Nog geen koers.", 22.sp, kleur = Bruin); Regel("Haal de koers op, of tik hem zelf in.") }
    } else {
        val kleur = when (s.soort) {
            Soort.Kopen -> Pastels.Salie.kaart
            Soort.Houden -> Pastels.Geel.kaart
            Soort.Verkopen -> Pastels.Koraal.kaart
        }
        Kaart(kleur) {
            Tekst("${s.soort.emoji} ${s.soort.woord}" + if (s.aantal > 0) " ${s.aantal}" else "", 32.sp, kleur = Bruin)
            Regel("Koers ${euro(laatste.prijs)} · ${laatste.datum.format(dagNotatie)}")
            s.redenen.forEach { Regel(it) }
        }
        if (s.soort != Soort.Houden) {
            val tekst = if (echt) "✅ Ik heb dit gedaan in DEGIRO" else "✅ Doe dit in de oefenpot"
            VolleKnop(tekst, {
                wijzig { b ->
                    val p = if (echt) b.echt else b.oefen
                    val nieuw = Beleggen.voerUit(p, s, laatste.prijs, LocalDate.now(), b.instellingen.kosten)
                    if (echt) b.copy(echt = nieuw) else b.copy(oefen = nieuw)
                }
                onMelding("Bewaard 👍")
            }, Modifier.fillMaxWidth(), vulling = hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal), tekstKleur = Color.White)
        }
    }

    VolleKnop("🌐 Koers ophalen", onHaal, Modifier.fillMaxWidth())
    Invoer(invoer, { invoer = it }, "Of tik de koers in, bv. 35,30", toetsen = KeyboardType.Decimal) {
        VolleKnop("Bewaar", {
            val p = getal(invoer)
            if (p == null || p <= 0) onMelding("Dat is geen koers.") else {
                wijzig { it.metKoers(Koers(LocalDate.now(), p)) }
                invoer = ""
                onMelding("Koers bewaard 👍")
            }
        })
    }
    Tekst("ℹ️ Dit is een hint, geen advies. Jij beslist en jij zet de order in DEGIRO.", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
}

@Composable
private fun PotKaart(titel: String, pot: Pot, koers: Double?) {
    Kaart {
        Tekst(titel, 22.sp, kleur = Bruin)
        Regel("📦 ${pot.aantal} aandelen" + if (pot.aantal > 0) " · gem. ${euro(pot.gemiddeld)}" else "")
        Regel("👛 Geld vrij: ${euro(pot.geld)}")
        if (koers != null) {
            Regel("💶 Totaal: ${euro(pot.waarde(koers))}")
            pot.resultaat(koers)?.let { Regel((if (it >= 0) "😊 " else "😕 ") + pct(it)) }
        }
        pot.orders.takeLast(3).reversed().forEach {
            Tekst("${if (it.koop) "🟢 koop" else "🔴 verkoop"} ${it.aantal} × ${euro(it.prijs)} · ${it.datum}", 14.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
        }
    }
}

@Composable
private fun Potten(d: BeleggenData, wijzig: ((BeleggenData) -> BeleggenData) -> Unit, onMelding: (String) -> Unit) {
    val koers = d.laatste?.prijs
    PotKaart("🏦 Echte pot", d.echt, koers)

    var aantal by remember { mutableStateOf("") }
    var prijs by remember { mutableStateOf("") }
    var geld by remember { mutableStateOf("") }
    Kaart {
        Tekst("✏️ Wat heb je nu in DEGIRO?", 19.sp, kleur = Bruin)
        Invoer(aantal, { aantal = it }, "Aantal aandelen", toetsen = KeyboardType.Number)
        Invoer(prijs, { prijs = it }, "Gem. aankoopprijs, bv. 34,50", toetsen = KeyboardType.Decimal)
        Invoer(geld, { geld = it }, "Geld vrij voor Ahold (mag 0)", toetsen = KeyboardType.Decimal)
        VolleKnop("Bewaar", {
            val a = aantal.trim().toIntOrNull()
            val p = getal(prijs)
            val g = getal(geld.ifBlank { "0" })
            if (a == null || a < 0 || p == null || g == null) onMelding("Vul aantal, prijs en geld in.") else {
                wijzig { it.copy(echt = Pot(startGeld = g, startAantal = a, startPrijs = p)) }
                aantal = ""; prijs = ""; geld = ""
                onMelding("Echte pot bewaard 👍")
            }
        }, Modifier.fillMaxWidth())
    }

    PotKaart("🎮 Oefenpot", d.oefen, koers)
    VolleKnop("🔄 Oefenpot opnieuw (€400)", {
        wijzig { it.copy(oefen = Pot(startGeld = 400.0)) }
        onMelding("Oefenpot staat weer op €400")
    }, Modifier.fillMaxWidth())
}

@Composable
private fun Ideeen() {
    Kaart(Pastels.Blauw.kaart) {
        Tekst("🧺 Je ETF's", 22.sp, kleur = Bruin)
        Beleggen.etfTips.forEach { Regel(it) }
    }
    Beleggen.ideeen.forEach { i ->
        Kaart {
            Tekst("${i.emoji} ${i.naam}", 22.sp, kleur = Bruin)
            Regel("❓ ${i.wat}")
            Regel("👍 ${i.waarom}")
            Regel("⚠️ ${i.risico}")
        }
    }
    Tekst("ℹ️ Ideeën om zelf te checken, geen advies.", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
}

@Composable
private fun TestTab(d: BeleggenData, wijzig: ((BeleggenData) -> BeleggenData) -> Unit, onMelding: (String) -> Unit) {
    val t = remember(d.koersen, d.instellingen) { Beleggen.test(d.koersen, d.instellingen) }
    Kaart {
        Tekst("🧪 Werkt het model?", 22.sp, kleur = Bruin)
        if (t == null) {
            Regel("Nog te weinig koersen (${d.koersen.size}). Haal eerst de koers op bij 📍 Nu.")
        } else {
            Regel("Met €400, van ${t.van} tot ${t.tot}:")
            Regel("🤖 Model: ${euro(t.model)} (${pct(t.modelPct)}) · ${t.orders} orders")
            Regel("🪨 Gewoon vasthouden: ${euro(t.vasthouden)} (${pct(t.vasthoudenPct)})")
            Regel(if (t.model >= t.vasthouden) "🏆 Het model deed het beter." else "🪨 Vasthouden deed het beter.")
        }
    }

    val i = d.instellingen
    var kosten by remember(i) { mutableStateOf(i.kosten.toString().replace('.', ',')) }
    var doel by remember(i) { mutableStateOf(i.koersdoel.toString().replace('.', ',')) }
    var stop by remember(i) { mutableStateOf(i.stopVerlies.toInt().toString()) }
    var winst by remember(i) { mutableStateOf(i.winstNemen.toInt().toString()) }
    Kaart {
        Tekst("⚙️ Instellingen", 22.sp, kleur = Bruin)
        Regel("🧾 Kosten per order (€)"); Invoer(kosten, { kosten = it }, "3", toetsen = KeyboardType.Decimal)
        Regel("🎯 Koersdoel analisten (€)"); Invoer(doel, { doel = it }, "37,5", toetsen = KeyboardType.Decimal)
        Regel("🛑 Verkopen bij verlies (%)"); Invoer(stop, { stop = it }, "10", toetsen = KeyboardType.Number)
        Regel("💰 Helft verkopen bij winst (%)"); Invoer(winst, { winst = it }, "15", toetsen = KeyboardType.Number)
        VolleKnop("Bewaar", {
            val k = getal(kosten); val dl = getal(doel); val st = getal(stop); val w = getal(winst)
            if (k == null || dl == null || st == null || w == null || dl <= 0) onMelding("Vul alles in met getallen.") else {
                wijzig { it.copy(instellingen = it.instellingen.copy(kosten = k, koersdoel = dl, stopVerlies = st, winstNemen = w)) }
                onMelding("Bewaard 👍")
            }
        }, Modifier.fillMaxWidth())
    }
}
