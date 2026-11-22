package nl.odelia.dashboard.ui.thuis

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.util.UUID
import nl.odelia.dashboard.logica.Geld
import nl.odelia.dashboard.logica.GeldData
import nl.odelia.dashboard.logica.UrenStats
import nl.odelia.dashboard.logica.Vooruit
import nl.odelia.dashboard.ui.geld.VasteLastenWaarschuwing
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur

private val Bruin = Color(0xFF5A3426)

/**
 * 🗓️ Maandafsluiting (bouwnacht 2): eind/begin van de maand. Wat is er over per potje, wat kwam erbij,
 * en gaat de rest (uit Vrije geld) naar sparen of beleggen? Daarna verdwijnt de kaart voor die maand.
 * Plus: waarschuwing als een potje de vaste lasten niet kan betalen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MaandAfsluitingKaart(geld: GeldData, vandaag: LocalDate, wijzig: ((GeldData) -> GeldData) -> Unit) {
    val waarschuwing = Vooruit.waarschuwingen(geld, vandaag).isNotEmpty()
    val m = Vooruit.teAfsluiten(geld, vandaag)
    if (m == null && !waarschuwing) return
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Color(0xFFEDE3FA), RoundedCornerShape(26.dp), Kleur.KaartSchaduw, hoogte = 8.dp)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (waarschuwing) VasteLastenWaarschuwing(geld, vandaag)
        if (m != null) {
            val a = Vooruit.afsluiting(geld, m)
            Tekst("🗓️ ${UrenStats.naam(m)} afsluiten", 18.sp, kleur = Bruin)
            Tekst("💜 Erbij: ${Geld.bedrag(a.inkomen)} · 🛒 Uit: ${Geld.bedrag(a.uitgaven)}", 15.sp, dikte = FontWeight.Bold, kleur = Bruin)
            a.potjes.filter { it.second != 0L }.forEach { (p, s) ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Tekst("${p.emoji} ${p.naam}", 15.sp, Modifier.weight(1f), dikte = FontWeight.Bold, kleur = Bruin)
                    Tekst(Geld.bedrag(s), 15.sp, kleur = if (s < 0) Color(0xFFB4442E) else Bruin)
                }
            }
            val bron = a.restPotje
            if (bron != null && a.rest > 0) {
                Tekst("Over in ${bron.emoji} ${bron.naam}: ${Geld.bedrag(a.rest)}. Waar moet dat heen?", 16.sp, kleur = Bruin)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ZachteKnop("🐷 Sparen", { wijzig { Vooruit.sluitAf(it, a, "spaar", UUID.randomUUID().toString(), vandaag) } })
                    ZachteKnop("📈 Beleggen", { wijzig { Vooruit.sluitAf(it, a, "inv", UUID.randomUUID().toString(), vandaag) } })
                    ZachteKnop("Laten staan", { wijzig { Vooruit.sluitAf(it, a, null, "", vandaag) } })
                }
            } else {
                ZachteKnop("✅ Klaar, afsluiten", { wijzig { Vooruit.sluitAf(it, a, null, "", vandaag) } })
            }
        }
    }
}
