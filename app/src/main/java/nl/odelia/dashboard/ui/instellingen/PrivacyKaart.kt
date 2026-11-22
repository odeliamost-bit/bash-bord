package nl.odelia.dashboard.ui.instellingen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.logica.Privacy
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur

/** 🛡️ Privacy-overzicht: per onderdeel 📱 alleen dit apparaat, of 🔒 versleuteld online (alleen jij). */
@Composable
fun PrivacyKaart() {
    val vorm = RoundedCornerShape(26.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, vorm, Kleur.KaartSchaduw, hoogte = 8.dp)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Tekst("🛡️ Waar staan je gegevens?", 17.sp, kleur = Kleur.Inkt)
        Tekst("📱 = alleen op dit apparaat · 🔒 = versleuteld online, alleen jij", 14.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht)
        Privacy.regels().forEach { r ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Tekst(r.icoon, 20.sp)
                Column(Modifier.weight(1f)) {
                    Tekst(r.naam, 16.sp, kleur = Kleur.Inkt)
                    if (r.uitleg.isNotEmpty()) Tekst(r.uitleg, 13.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht)
                }
                Tekst(if (r.online) "🔒" else "📱", 22.sp)
            }
        }
    }
}
