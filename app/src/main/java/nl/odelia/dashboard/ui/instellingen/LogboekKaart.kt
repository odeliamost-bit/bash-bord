package nl.odelia.dashboard.ui.instellingen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter
import nl.odelia.dashboard.logica.ApiKosten
import nl.odelia.dashboard.logica.ApiKostenData
import nl.odelia.dashboard.logica.Geld
import nl.odelia.dashboard.logica.LogboekData
import nl.odelia.dashboard.ui.onderdelen.Invoer
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur

private val tijdVorm = DateTimeFormatter.ofPattern("EEE d MMM HH:mm", java.util.Locale.forLanguageTag("nl"))

@Composable
private fun Kaart(inhoud: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(26.dp), Kleur.KaartSchaduw, hoogte = 8.dp)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) { inhoud() }
}

/** 📜 Laatst gebeurd: sync, ING-uitgave in een potje, in- en uitgeklokt. */
@Composable
fun LogboekKaart(log: LogboekData) {
    var alles by remember { mutableStateOf(false) }
    Kaart {
        Tekst("📜 Laatst gebeurd", 17.sp, kleur = Kleur.Inkt)
        if (log.regels.isEmpty()) Tekst("Nog niks.", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht)
        (if (alles) log.regels else log.regels.take(5)).forEach { r ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Tekst(r.icoon, 18.sp)
                Column(Modifier.weight(1f)) {
                    Tekst(r.tekst, 15.sp, kleur = Kleur.Inkt)
                    Tekst(r.tijd.format(tijdVorm), 13.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht)
                }
            }
        }
        if (log.regels.size > 5) ZachteKnop(if (alles) "Minder" else "Alles (${log.regels.size})", { alles = !alles })
    }
}

/** 🤖 Claude-kosten deze maand + limiet. Zelf invullen uit de Anthropic Console (de app kan het niet zien). */
@Composable
fun ApiKostenKaart(k: ApiKostenData, wijzig: ((ApiKostenData) -> ApiKostenData) -> Unit) {
    var bedrag by remember { mutableStateOf("") }
    var limiet by remember { mutableStateOf("") }
    Kaart {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Tekst("🤖 Claude-kosten deze maand", 17.sp, kleur = Kleur.Inkt, modifier = Modifier.weight(1f))
            Tekst(Geld.bedrag(k.cent), 20.sp, kleur = Kleur.Inkt)
        }
        Tekst("Limiet ${Geld.bedrag(k.limietCent)} · ${ApiKosten.bericht(k)}", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht)
        Tekst("Kijk in de Anthropic Console en vul het bedrag hier in. De app kan het zelf niet zien.", 13.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht)
        Invoer(bedrag, { bedrag = it }, "Bedrag nu, bijv. 1,20", toetsen = KeyboardType.Decimal) {
            ZachteKnop("✅", { Geld.leesBedrag(bedrag)?.let { c -> wijzig { it.copy(cent = c) }; bedrag = "" } })
        }
        Invoer(limiet, { limiet = it }, "Limiet per maand, bijv. 5", toetsen = KeyboardType.Decimal) {
            ZachteKnop("✅", { Geld.leesBedrag(limiet)?.let { c -> wijzig { it.copy(limietCent = c) }; limiet = "" } })
        }
    }
}
