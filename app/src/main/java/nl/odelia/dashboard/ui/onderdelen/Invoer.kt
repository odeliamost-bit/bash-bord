package nl.odelia.dashboard.ui.onderdelen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.theme.Nunito

/** Wit tekstveld met grijze voorbeeldtekst als het leeg is. [rechts]: knopjes in het veld (bv. 🎤). */
@Composable
fun Invoerveld(
    tekst: String,
    onTekst: (String) -> Unit,
    voorbeeld: String,
    modifier: Modifier = Modifier,
    eenRegel: Boolean = false,
    minHoogte: Dp = 56.dp,
    rechts: @Composable RowScope.() -> Unit = {},
) {
    val vorm = RoundedCornerShape(20.dp)
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = minHoogte)
            .shadow(3.dp, vorm, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
            .background(Color.White, vorm)
            .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.weight(1f).padding(vertical = 8.dp)) {
            if (tekst.isEmpty()) Tekst(voorbeeld, 17.sp, dikte = FontWeight.Bold, kleur = Color(0xFFA8968A))
            BasicTextField(
                value = tekst,
                onValueChange = onTekst,
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = voorbeeld },
                textStyle = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Kleur.Inkt),
                singleLine = eenRegel,
                cursorBrush = SolidColor(Kleur.Koraal),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = if (eenRegel) ImeAction.Done else ImeAction.Default,
                ),
            )
        }
        rechts()
    }
}

/** Rond knopje met een icoontje, bv. de 🎤 in een tekstveld. [hoofd] = koraal. */
@Composable
fun RondKnopje(icoon: List<Vorm>, uitleg: String, onClick: () -> Unit, maat: Dp = 44.dp, hoofd: Boolean = true) {
    Box(
        Modifier
            .tik(onClick = onClick)
            .size(maat)
            .clip(CircleShape)
            .background(if (hoofd) hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal) else SolidColor(Color(0xFFE6DBFF)))
            .semantics { contentDescription = uitleg },
        contentAlignment = Alignment.Center,
    ) {
        Icoon(icoon, maat * 0.5f)
    }
}

/** Keuze-knopje (pil). Gekozen = koraal randje. */
@Composable
fun Keuze(
    tekst: String,
    aan: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    achtergrond: Color = Color.White,
    voor: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier
            .kies(aan, onClick)
            .heightIn(min = 48.dp)
            .clip(CircleShape)
            .background(achtergrond)
            .border(3.dp, if (aan) Kleur.Koraal else Color(0xFFEEE6F8), CircleShape)
            .padding(start = if (voor != null) 6.dp else 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        voor?.invoke()
        Tekst(tekst, 16.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.Inkt, eenRegel = true)
    }
}

/** Balk met een paar keuzes naast elkaar; de gekozen keuze is koraal (bv. School · Werk · Thuis). */
@Composable
fun KeuzeBalk(opties: List<String>, gekozen: Int, onKies: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Kleur.Kaart.copy(alpha = 0.7f), CircleShape)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        opties.forEachIndexed { i, tekst ->
            val aan = i == gekozen
            Box(
                Modifier
                    .weight(1f)
                    .kies(aan) { onKies(i) }
                    .heightIn(min = 48.dp)
                    .then(
                        if (aan) {
                            Modifier.shadow(4.dp, CircleShape, ambientColor = Kleur.Koraal, spotColor = Kleur.Koraal)
                                .background(hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal), CircleShape)
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Tekst(tekst, 15.sp, kleur = if (aan) Color.White else Color(0xFF7A4A38), eenRegel = true)
            }
        }
    }
}

/** Aan/uit-schakelaar zoals in het ontwerp: groen = aan. */
@Composable
fun Schakelaar(aan: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier
            .width(56.dp)
            .heightIn(min = 32.dp)
            .background(if (aan) Color(0xFF3F9A64) else Color(0xFFC9BFD6), CircleShape)
            .padding(3.dp),
        contentAlignment = if (aan) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(Modifier.size(26.dp).background(Color.White, CircleShape))
    }
}

/** Terug-knop met een titel ernaast, en eventueel iets rechts (bv. de 🎤). */
@Composable
fun SchermKop(titel: String, onTerug: () -> Unit, rechts: @Composable () -> Unit = {}) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier
                .tik(onClick = onTerug)
                .size(48.dp)
                .shadow(6.dp, CircleShape, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
                .clip(CircleShape)
                .background(Kleur.Kaart)
                .semantics { contentDescription = "Terug" },
            contentAlignment = Alignment.Center,
        ) {
            Icoon(Icoontjes.terug, 24.dp)
        }
        Tekst(titel, 26.sp, Modifier.weight(1f))
        rechts()
    }
}

/** Kopje boven een stukje formulier: icoontje + tekst ("👤 Wie?"). */
@Composable
fun Kopje(icoon: List<Vorm>, tekst: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icoon(icoon, 24.dp)
        Tekst(tekst, 19.sp)
    }
}
