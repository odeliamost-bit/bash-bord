package nl.odelia.dashboard.ui.leren

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.logica.LoopSchema
import nl.odelia.dashboard.logica.Onderdeel
import nl.odelia.dashboard.ui.onderdelen.Keuze
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur

private val Stroom = Color(0xFF5E9A70)
private val Dood = Color(0xFFC9BDB2)
private val Fout = Color(0xFFD9694F)

/** Schema lezen: een oefen-lus van een afsluiter, getekend als Veld → Junction box → MCC. Namen zijn verzonnen. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoopDeel(staat: LoopSchema.Staat, onStaat: (LoopSchema.Staat) -> Unit, onKies: (Onderdeel) -> Unit) {
    val s = LoopSchema.reken(staat)
    val draadLos = staat.storing == LoopSchema.Storing.Draadbreuk

    Kaart {
        Tekst("Afsluiter onder een tank", 20.sp)
        Tekst("Lees van links naar rechts. Groen = er loopt stroom. Tik op een blokje voor uitleg.", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktGrijs)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("VELD", "JUNCTION BOX", "MCC").forEach {
                Tekst(it, 13.sp, Modifier.weight(1f), kleur = Kleur.InktZacht, uitlijning = TextAlign.Center)
            }
        }

        Lus("Melding OPEN  →") {
            Blok("ZSO-101", if (s.schakelaarOpen) "contact dicht" else "contact open", s.schakelaarOpen) { onKies(LoopSchema.zso) }
            Draad(s.schakelaarOpen)
            Blok("JB-10", if (draadLos) "klem 1-2 ✂️ los" else "klem 1-2", s.schakelaarOpen && !draadLos, fout = draadLos) { onKies(LoopSchema.jb) }
            Draad(s.ingangOpen)
            Blok("K1 → I3.0", if (s.ingangOpen) "1" else "0", s.ingangOpen) { onKies(if (s.ingangOpen) LoopSchema.ingang else LoopSchema.versterker) }
        }
        Lus("Melding DICHT  →") {
            Blok("ZSC-101", if (s.schakelaarDicht) "contact dicht" else "contact open", s.schakelaarDicht) { onKies(LoopSchema.zsc) }
            Draad(s.schakelaarDicht)
            Blok("JB-10", "klem 3-4", s.schakelaarDicht) { onKies(LoopSchema.jb) }
            Draad(s.ingangDicht)
            Blok("K1 → I3.1", if (s.ingangDicht) "1" else "0", s.ingangDicht) { onKies(LoopSchema.versterker) }
        }
        Lus("←  Aansturing") {
            Blok("YV-101 + XV-101", if (s.klepOpen) "klep OPEN" else "klep DICHT", s.ventiel, fout = s.ventiel && !s.klepOpen) {
                onKies(if (s.ventiel) LoopSchema.ventiel else LoopSchema.afsluiter)
            }
            Draad(s.ventiel)
            Blok("JB-11", "klem 1-2", s.ventiel) { onKies(LoopSchema.jb) }
            Draad(s.ventiel)
            Blok("Q5.0", if (s.ventiel) "24 V" else "0 V", s.ventiel) { onKies(LoopSchema.uitgang) }
        }

        Tekst(s.melding, 17.sp, kleur = if (s.storing) Fout else Stroom)
    }

    Kaart {
        Tekst("Zelf proberen", 20.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Keuze("Dicht", !staat.sturen, onClick = { onStaat(staat.copy(sturen = false)) }, Modifier.weight(1f))
            Keuze("Open sturen", staat.sturen, onClick = { onStaat(staat.copy(sturen = true)) }, Modifier.weight(1f))
        }
        Tekst("Storing erin zetten:", 15.sp, dikte = FontWeight.Bold, kleur = Kleur.InktZacht)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LoopSchema.Storing.entries.forEach { st -> Keuze(st.label, staat.storing == st, onClick = { onStaat(staat.copy(storing = st)) }) }
        }
    }

    val gekozen = remember { mutableStateMapOf<Int, Int>() }
    Kaart {
        Tekst("Opdrachtjes", 20.sp)
        LoopSchema.vragen.forEachIndexed { i, v ->
            Tekst("${i + 1}. ${v.vraag}", 16.sp, dikte = FontWeight.ExtraBold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                v.antwoorden.forEachIndexed { a, tekst -> Keuze(tekst, gekozen[i] == a, onClick = { gekozen[i] = a }) }
            }
            gekozen[i]?.let { a ->
                Tekst(if (a == v.goed) "✅ Goed! ${v.uitleg}" else "❌ Nog niet. Probeer het in het schema hierboven.", 15.sp,
                    dikte = FontWeight.Bold, kleur = if (a == v.goed) Stroom else Fout)
            }
        }
    }
}

@Composable
private fun Kaart(inhoud: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(26.dp), Kleur.KaartSchaduw)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) { inhoud() }
}

@Composable
private fun Lus(naam: String, inhoud: @Composable RowScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Tekst(naam, 14.sp, kleur = Kleur.InktZacht)
        Row(verticalAlignment = Alignment.CenterVertically, content = inhoud)
    }
}

@Composable
private fun RowScope.Blok(naam: String, onder: String, aan: Boolean, fout: Boolean = false, onTik: () -> Unit) {
    val rand = if (fout) Fout else if (aan) Stroom else Dood
    Column(
        Modifier
            .weight(1f)
            .heightIn(min = 60.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(rand.copy(alpha = 0.18f))
            .clickable(onClickLabel = "Uitleg", onClick = onTik)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Tekst(naam, 13.sp, uitlijning = TextAlign.Center, regel = 15.sp)
        Tekst(onder, 12.sp, dikte = FontWeight.Bold, kleur = rand, uitlijning = TextAlign.Center, regel = 14.sp)
    }
}

@Composable
private fun Draad(aan: Boolean) {
    Box(Modifier.width(14.dp).height(4.dp).background(if (aan) Stroom else Dood))
}
