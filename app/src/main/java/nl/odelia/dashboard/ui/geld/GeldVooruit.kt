package nl.odelia.dashboard.ui.geld

import androidx.compose.foundation.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import java.time.YearMonth
import java.util.UUID
import nl.odelia.dashboard.logica.Geld
import nl.odelia.dashboard.logica.GeldData
import nl.odelia.dashboard.logica.VasteLast
import nl.odelia.dashboard.logica.Vooruit
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.onderdelen.Invoer
import nl.odelia.dashboard.ui.onderdelen.Keuze
import nl.odelia.dashboard.ui.onderdelen.Staafjes
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop

private fun euro(f: Float) = "€${f.toInt()}"

/** 📊 Inkomsten en uitgaven per maand (laatste 6). */
@Composable
internal fun GeldGrafiekKaart(data: GeldData, nu: YearMonth) {
    Paneel {
        GeldKopje("📊 Inkomsten per maand")
        Staafjes(Vooruit.inkomenPerMaand(data.boekingen, nu), Color(0xFFB79CE0), ::euro, tekstKleur = Bruin)
        GeldKopje("📉 Uitgaven per maand")
        Staafjes(Vooruit.uitgavenPerMaand(data.boekingen, nu), Color(0xFFF4A58B), ::euro, tekstKleur = Bruin)
    }
}

/** ⚠️ Waarschuwing: een potje kan de vaste lasten van de komende maand niet betalen. Ook op Thuis. */
@Composable
fun VasteLastenWaarschuwing(data: GeldData, vandaag: LocalDate) {
    Vooruit.waarschuwingen(data, vandaag).forEach { w ->
        Tekst(
            "⚠️ ${w.potje.emoji} ${w.potje.naam} heeft ${Geld.bedrag(w.saldo)}, maar de vaste lasten tot ${w.eerste.plusDays(30).dayOfMonth}-${w.eerste.plusDays(30).monthValue} kosten ${Geld.bedrag(w.nodig)}. Zet er ${Geld.bedrag(w.nodig - w.saldo)} bij.",
            15.sp, kleur = Color(0xFFB4442E),
        )
    }
}

/** 📅 Vaste lasten: wat komt eraan, en kan het potje het betalen? */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun VasteLastenKaart(data: GeldData, vandaag: LocalDate, acties: AppActies) {
    var naam by remember { mutableStateOf("") }
    var bedrag by remember { mutableStateOf("") }
    var dag by remember { mutableStateOf("") }
    var pot by remember { mutableStateOf("vast") }
    var nieuw by remember { mutableStateOf(false) }
    Paneel {
        GeldKopje("📅 Vaste lasten vooruit")
        VasteLastenWaarschuwing(data, vandaag)
        val komend = Vooruit.komend(data.vasteLasten, vandaag)
        if (data.vasteLasten.isEmpty()) {
            Tekst("Nog geen vaste lasten. Voeg ze toe, dan waarschuwt de app op tijd.", 15.sp, dikte = FontWeight.Bold, kleur = BruinZacht)
        } else {
            Tekst("Komende maand:", 15.sp, dikte = FontWeight.Bold, kleur = BruinZacht)
            komend.forEach { k ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Tekst("${k.datum.dayOfMonth}-${k.datum.monthValue}  ${k.last.naam}", 15.sp, Modifier.weight(1f), kleur = Bruin)
                    Tekst(Geld.bedrag(k.last.cent), 15.sp, kleur = Bruin)
                }
            }
            data.vasteLasten.forEach { v ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Tekst("${data.potje(v.potjeId)?.emoji ?: "•"} ${v.naam} · elke ${v.dag}e", 14.sp, Modifier.weight(1f), dikte = FontWeight.Bold, kleur = BruinZacht)
                    ZachteKnop("✕", { acties.wijzigGeld { d -> d.copy(vasteLasten = d.vasteLasten.filter { it.id != v.id }) } })
                }
            }
        }
        if (!nieuw) {
            ZachteKnop("➕ Vaste last", { nieuw = true })
        } else {
            Invoer(naam, { naam = it }, "Naam, bijv. Telefoon")
            Invoer(bedrag, { bedrag = it }, "Bedrag, bijv. 12,50", toetsen = KeyboardType.Decimal)
            Invoer(dag, { dag = it.filter(Char::isDigit).take(2) }, "Dag van de maand (1–28)", toetsen = KeyboardType.Number)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                data.potjes.forEach { p -> Keuze("${p.emoji} ${p.naam}", pot == p.id, { pot = p.id }) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ZachteKnop("Annuleer", { nieuw = false })
                ZachteKnop("✅ Bewaar", {
                    val c = Geld.leesBedrag(bedrag)
                    val d = dag.toIntOrNull()
                    if (naam.isNotBlank() && c != null && c > 0 && d != null && d in 1..28) {
                        val v = VasteLast(UUID.randomUUID().toString(), naam.trim(), c, d, pot)
                        acties.wijzigGeld { g -> g.copy(vasteLasten = g.vasteLasten + v) }
                        naam = ""; bedrag = ""; dag = ""; nieuw = false
                    }
                })
            }
        }
    }
}
