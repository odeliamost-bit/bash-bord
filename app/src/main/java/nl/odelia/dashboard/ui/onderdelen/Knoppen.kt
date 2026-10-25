package nl.odelia.dashboard.ui.onderdelen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.theme.Nunito

/** Bovenbalk met terug-knop en titel. Rechts mag nog iets (bijv. een 🎤-knop). */
@Composable
fun TerugBalk(titel: String, onTerug: () -> Unit, rechts: @Composable RowScope.() -> Unit = {}) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        RondeKnop(Icoontjes.terug, "Terug", onTerug, maat = 52.dp)
        Tekst(titel, 26.sp, Modifier.weight(1f), kleur = Color(0xFF5A3426), regel = 30.sp)
        rechts()
    }
}

/** Ronde crème knop met icoontje. */
@Composable
fun RondeKnop(icoon: List<Vorm>, uitleg: String, onClick: () -> Unit, maat: Dp = 52.dp, vulling: Brush = SolidColor(Kleur.Kaart)) {
    Box(
        Modifier
            .size(maat)
            .shadow(6.dp, CircleShape, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
            .clip(CircleShape)
            .background(vulling)
            .glans(CircleShape, licht = 0.8f, donker = 0f, boven = 2.dp)
            .clickable(onClick = onClick)
            .semantics { contentDescription = uitleg },
        contentAlignment = Alignment.Center,
    ) {
        Icoon(icoon, maat * 0.48f)
    }
}

/** Koraal 🎤-knop voor inspreken. */
@Composable
fun MicKnop(onClick: () -> Unit, maat: Dp = 56.dp) {
    RondeKnop(Icoontjes.microfoon, "Inspreken", onClick, maat, hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal))
}

/** Grote koraal knop met icoontje en 3D-voetje. */
@Composable
fun GroteKnop(tekst: String, onClick: () -> Unit, modifier: Modifier = Modifier, icoon: List<Vorm> = Icoontjes.plus) {
    val vorm = RoundedCornerShape(24.dp)
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .voet(Kleur.KoraalDiep, vorm, 6.dp)
            .shadow(12.dp, vorm, ambientColor = Kleur.Koraal, spotColor = Kleur.Koraal)
            .clip(vorm)
            .background(hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal))
            .glans(vorm, licht = 0.4f, donker = 0f)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
    ) {
        Icoon(icoon, 24.dp)
        Tekst(tekst, 19.sp, kleur = Color.White)
    }
}

/** Crème knop voor iets minder belangrijks. */
@Composable
fun ZachteKnop(tekst: String, onClick: () -> Unit, modifier: Modifier = Modifier, kleur: Color = Kleur.InktZacht) {
    val vorm = RoundedCornerShape(20.dp)
    Box(
        modifier
            .heightIn(min = 52.dp)
            .shadow(6.dp, vorm, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
            .clip(vorm)
            .background(Kleur.Kaart)
            .glans(vorm, licht = 0.9f, donker = 0.05f)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Tekst(tekst, 17.sp, kleur = kleur)
    }
}

/** Keuzeknopje (zoals "Werk", "morgen"). Gekozen = koraal randje. */
@Composable
fun Keuze(tekst: String, aan: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, achter: Color = Color.White) {
    val vorm = RoundedCornerShape(18.dp)
    Box(
        modifier
            .heightIn(min = 48.dp)
            .clip(vorm)
            .background(achter)
            .border(3.dp, if (aan) Kleur.Koraal else Color.Transparent, vorm)
            .toggleable(value = aan, role = Role.RadioButton, onValueChange = { onClick() })
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Tekst(tekst, 16.sp, dikte = FontWeight.ExtraBold, kleur = Color(0xFF3A3046))
    }
}

/** Aan/uit-schakelaartje zoals in het ontwerp. */
@Composable
fun Schakelaar(aan: Boolean) {
    Box(
        Modifier
            .size(56.dp, 32.dp)
            .background(if (aan) Color(0xFF3F9A64) else Color(0xFFC9BFD6), CircleShape)
            .padding(3.dp),
        contentAlignment = if (aan) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(Modifier.size(26.dp).background(Color.White, CircleShape))
    }
}

/** Tekstveld in klei-stijl. */
@Composable
fun Invoer(
    waarde: String,
    onWijzig: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    regels: Int = 1,
    rechts: @Composable RowScope.() -> Unit = {},
) {
    val vorm = RoundedCornerShape(if (regels > 1) 22.dp else 999.dp)
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .background(Color.White, vorm)
            .border(1.dp, Color(0xFFEEDFD3), vorm)
            .padding(start = 18.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = if (regels > 1) Alignment.Top else Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicTextField(
            value = waarde,
            onValueChange = onWijzig,
            modifier = Modifier.weight(1f).padding(vertical = 10.dp),
            textStyle = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Kleur.Inkt),
            minLines = regels,
            maxLines = if (regels > 1) 8 else 1,
            singleLine = regels == 1,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            cursorBrush = SolidColor(Kleur.Koraal),
            decorationBox = { veld ->
                Box {
                    if (waarde.isEmpty()) Tekst(hint, 18.sp, dikte = FontWeight.Bold, kleur = Color(0xFFB09A8C))
                    veld()
                }
            },
        )
        rechts()
    }
}

/** Kopje met icoontje: "👤 Wie?" */
@Composable
fun Kopje(tekst: String, icoon: List<Vorm>? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (icoon != null) Icoon(icoon, 24.dp)
        Tekst(tekst, 19.sp, kleur = Color(0xFF5A3426))
    }
}
