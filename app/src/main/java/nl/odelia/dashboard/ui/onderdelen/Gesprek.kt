package nl.odelia.dashboard.ui.onderdelen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.theme.Nunito

private val Bruin = Color(0xFF5A3426)
private val Grijs = Color(0xFF7A4A38)

/** Kopje met titel, klein regeltje eronder, terug-knop links en iets rechts (Mail-hulp, Advies). */
@Composable
fun KopBalk(titel: String, onder: String, onTerug: () -> Unit, rechts: @Composable () -> Unit = {}) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        RondeKnop(Icoontjes.terug, "Terug", onTerug, maat = 52.dp)
        Column(Modifier.weight(1f)) {
            Tekst(titel, 28.sp, kleur = Bruin, regel = 31.sp)
            Tekst(onder, 14.sp, dikte = FontWeight.Bold, kleur = Grijs)
        }
        rechts()
    }
}

/** Rond gekleurd rondje met een emoji (rechts in de kopbalk). Met [onClick] is het een knop. */
@Composable
fun EmojiRondje(emoji: String, licht: Color, donker: Color, uitleg: String? = null, onClick: (() -> Unit)? = null, maat: Dp = 52.dp) {
    Box(
        Modifier
            .size(maat)
            .shadow(6.dp, CircleShape, ambientColor = donker, spotColor = donker)
            .clip(CircleShape)
            .background(hoekVerloop(licht, donker))
            .glans(CircleShape, licht = 0.5f, donker = 0.08f)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .then(if (uitleg != null) Modifier.semantics { contentDescription = uitleg } else Modifier),
        contentAlignment = Alignment.Center,
    ) { Tekst(emoji, 22.sp, kleur = Color.White) }
}

/** Praatwolkje. [vanMij] = koraal, rechts. Anders crème, links (de app). */
@Composable
fun Bubbel(tekst: String, vanMij: Boolean) {
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = if (vanMij) Alignment.CenterEnd else Alignment.CenterStart) {
        val vorm = if (vanMij) RoundedCornerShape(24.dp, 24.dp, 6.dp, 24.dp) else RoundedCornerShape(24.dp, 24.dp, 24.dp, 6.dp)
        Box(
            Modifier
                .widthIn(max = maxWidth * 0.85f)
                .shadow(if (vanMij) 0.dp else 6.dp, vorm, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
                .background(if (vanMij) hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal) else SolidColor(Kleur.Kaart), vorm)
                .padding(horizontal = 16.dp, vertical = if (vanMij) 12.dp else 14.dp),
        ) {
            Tekst(tekst, if (vanMij) 16.sp else 17.sp, dikte = FontWeight.ExtraBold, kleur = if (vanMij) Color.White else Kleur.Inkt, regel = 22.sp)
        }
    }
}

/** Knop met een volle kleur (groen, koraal) of wit. Minstens 52 dp hoog. */
@Composable
fun VolleKnop(
    tekst: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    vulling: Brush = SolidColor(Color.White),
    tekstKleur: Color = Kleur.Inkt,
    schaduw: Color = Kleur.KaartSchaduw,
) {
    val vorm = RoundedCornerShape(18.dp)
    Box(
        modifier
            .heightIn(min = 52.dp)
            .shadow(6.dp, vorm, ambientColor = schaduw, spotColor = schaduw)
            .clip(vorm)
            .background(vulling)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Tekst(tekst, 16.sp, kleur = tekstKleur, uitlijning = TextAlign.Center)
    }
}

/** Typbalk onderaan (Mail-hulp, Advies): zelf typen, 🎤 inspreken, ↑ versturen. */
@Composable
fun TypBalk(
    waarde: String,
    onWijzig: (String) -> Unit,
    hint: String,
    onStuur: () -> Unit,
    onInspreken: () -> Unit,
    stuurUitleg: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .shadow(14.dp, CircleShape, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
            .background(Kleur.Kaart, RoundedCornerShape(30.dp))
            .padding(start = 18.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicTextField(
            value = waarde,
            onValueChange = onWijzig,
            modifier = Modifier.weight(1f).padding(vertical = 8.dp),
            textStyle = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Kleur.Inkt),
            maxLines = 4,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onStuur() }),
            cursorBrush = SolidColor(Kleur.Koraal),
            decorationBox = { veld ->
                Box {
                    if (waarde.isEmpty()) Tekst(hint, 16.sp, dikte = FontWeight.Bold, kleur = Color(0xFF9A7A6A))
                    veld()
                }
            },
        )
        Box(
            Modifier
                .size(48.dp)
                .shadow(4.dp, CircleShape, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(role = Role.Button, onClick = onInspreken)
                .semantics { contentDescription = "Inspreken" },
            contentAlignment = Alignment.Center,
        ) { Tekst("🎤", 22.sp) }
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal))
                .clickable(role = Role.Button, onClick = onStuur)
                .semantics { contentDescription = stuurUitleg },
            contentAlignment = Alignment.Center,
        ) { Tekst("↑", 20.sp, kleur = Color.White) }
    }
}
