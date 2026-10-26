package nl.odelia.dashboard.ui.geld

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
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.SyncStaat
import nl.odelia.dashboard.ui.onderdelen.GroteKnop
import nl.odelia.dashboard.ui.onderdelen.Invoer
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur

private val uurMinuut = DateTimeFormatter.ofPattern("HH:mm")

/**
 * ☁️ Sync tussen apparaten: inloggen met een code per e-mail (geen wachtwoord),
 * en zien of het werkt. Niet ingesteld in deze versie? Dan zie je niks.
 */
@Composable
fun SyncKaart(sync: SyncStaat, acties: AppActies) {
    if (!sync.kan) return
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    val vorm = RoundedCornerShape(26.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, vorm, Kleur.KaartSchaduw, hoogte = 8.dp)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        when {
            sync.email != null -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Tekst(if (sync.live) "☁️" else "🌥️", 28.sp)
                Column(Modifier.weight(1f)) {
                    Tekst(if (sync.live) "Sync aan" else "Sync wacht op internet", 17.sp, kleur = Kleur.Inkt)
                    val tijd = sync.laatste?.let { " · laatst ${it.format(uurMinuut)}" }.orEmpty()
                    Tekst("${sync.email}$tijd", 14.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht)
                }
                ZachteKnop(if (sync.bezig) "…" else "🔄 Nu", acties::syncNu)
            }
            sync.codeVoor == null -> {
                Tekst("☁️ Zelfde geld op al je apparaten", 17.sp, kleur = Kleur.Inkt)
                Tekst("Log in met je e-mail. Je krijgt een code, geen wachtwoord.", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht)
                Invoer(email, { email = it }, "jouw@gmail.com", toetsen = KeyboardType.Email)
                GroteKnop(if (sync.bezig) "Even wachten…" else "📬 Stuur code", { if (!sync.bezig && "@" in email) acties.vraagSyncCode(email) })
            }
            else -> {
                Tekst("📬 Kijk in je mail", 17.sp, kleur = Kleur.Inkt)
                Tekst("Typ de code uit de mail van Supabase.", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht)
                Invoer(code, { code = it.filter(Char::isDigit).take(8) }, "123456", toetsen = KeyboardType.Number)
                GroteKnop(if (sync.bezig) "Even wachten…" else "✅ Inloggen", { if (!sync.bezig && code.length >= 6) acties.logInSync(code) })
            }
        }
        sync.bericht?.let { Tekst(it, 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht) }
        if (sync.email != null) ZachteKnop("Uitloggen", acties::logUitSync, Modifier.padding(top = 2.dp))
    }
}
