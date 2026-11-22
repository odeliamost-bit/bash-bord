package nl.odelia.dashboard.ui.instellingen

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch
import nl.odelia.dashboard.data.Bestanden
import nl.odelia.dashboard.data.Locatie
import nl.odelia.dashboard.logica.Poort
import nl.odelia.dashboard.meldingen.Planner
import nl.odelia.dashboard.ui.AppActies
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.ZachteKnop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur

/** 📍 Poort-check aan/uit (bouwnacht 2). Standaard uit. De plek van de poort blijft op dit apparaat. */
@Composable
fun PoortKaart(acties: AppActies) {
    val ctx = LocalContext.current
    val opslag = remember { Bestanden.poort(ctx) }
    val poort by opslag.data.collectAsState()
    val scope = rememberCoroutineScope()
    var bericht by remember { mutableStateOf<String?>(null) }
    var tik by remember { mutableIntStateOf(0) }
    val levens = LocalLifecycleOwner.current
    DisposableEffect(levens) {
        val kijk = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_RESUME) tik++ }
        levens.lifecycle.addObserver(kijk)
        onDispose { levens.lifecycle.removeObserver(kijk) }
    }
    val altijd = remember(tik) { Locatie.magAltijd(ctx) }

    fun plan() = scope.launch { Planner.plan(ctx.applicationContext) }
    fun hier() = scope.launch {
        bericht = "📍 Even zoeken…"
        val p = Locatie.eenKeer(ctx)
        if (p == null) {
            bericht = "Plek vinden lukte niet. Staat locatie aan?"
        } else {
            opslag.wijzig({ it.copy(lat = p.latitude, lon = p.longitude) })
            bericht = "✅ Poort bewaard (op ±${p.accuracy.toInt()} m)."
            plan()
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(26.dp), Kleur.KaartSchaduw, hoogte = 8.dp)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Tekst("📍 Poort-check bij uitklokken", 17.sp, kleur = Kleur.Inkt)
        Tekst(
            "Na 16:00, als je nog ingeklokt bent, kijkt de app om de 17 minuten één keer waar je bent. Buiten de poort? Dan een zoempje. " +
                "Geen constante locatie, tot 18:00. De plek van de poort blijft op deze telefoon.",
            14.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht,
        )
        ZachteKnop(if (poort.aan) "✅ Staat aan (tik = uit)" else "Staat uit (tik = aan)", {
            opslag.wijzig({ it.copy(aan = !it.aan) })
            plan()
        })
        if (poort.aan) {
            ZachteKnop(if (poort.lat == null) "📍 Ik sta nu bij de poort" else "📍 Poort opnieuw zetten (sta bij de poort)", {
                if (Locatie.mag(ctx)) hier() else acties.vraagLocatie { ok -> if (ok) hier() else bericht = "Zonder locatie werkt de poort-check niet." }
            })
            if (!altijd) {
                Tekst("Stap 2: kies bij Locatie \"Altijd toestaan\", anders werkt het niet als de app dicht is.", 14.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht)
                ZachteKnop("⚙️ Naar Android-instellingen", {
                    ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", ctx.packageName, null)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                })
            }
            if (Poort.klaar(poort) && altijd) Tekst("✅ Klaar. Vanaf 16:00 let de app op.", 15.sp, kleur = Kleur.Inkt)
        }
        bericht?.let { Tekst(it, 14.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht) }
    }
}
