package nl.odelia.dashboard.ui.geld

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.logica.Categorie
import nl.odelia.dashboard.logica.Geld
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.glans
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur
import nl.odelia.dashboard.ui.theme.Nunito

// Kleuren uit ontwerp/Geld.dc.html
internal val Bruin = Color(0xFF5A3426)
internal val BruinZacht = Color(0xFF8A5A48)
internal val Grijs = Color(0xFF8A6A5A)
internal val Groen = Color(0xFF6FAE84)
internal val GroenLicht = Color(0xFFA8D6B4)
internal val GroenDonker = Color(0xFF3F7A58)
internal val LilaLicht = Color(0xFFD2BDF0)
internal val Lila = Color(0xFFA88AD8)
internal val Rand = Color(0xFFE3C3B0)

/** Achtergrondjes van de potjes-kaarten, op volgorde. */
internal val potKleuren = listOf(
    0xFFF3E6FB, 0xFFDCE7F2, 0xFFE3EDDF, 0xFFFBE0E8, 0xFFFDE6DA, 0xFFFBD9E3, 0xFFFBEBC4, 0xFFFFF1B8,
).map(::Color)

/** Kleurtjes van de balkjes bij verdelen. */
internal val verdeelKleuren = listOf(0xFF8E9CF0, 0xFFF6CF5A, 0xFFF49A8E, 0xFFF7A8C4, 0xFFB9A3F0, 0xFF9ACFAE, 0xFF8EC5F5).map(::Color)

internal val alleCategorieen = (Geld.uitgaven + Geld.inkomen + Geld.sparen).associateBy { it.id }

internal fun categorie(id: String): Categorie =
    alleCategorieen[id] ?: Categorie(id, if (id == "verdeel") "Verdeeld" else id, "🔁", 0xFFD9CFC6)

/** "€ 4.251,72" zonder het €-teken (dat staat er in het ontwerp los voor). */
internal fun zonderEuro(cent: Long) = Geld.bedrag(cent).replace("€ ", "")

/** Bedrag als tekst in een invoerveld: 500 → "5,00". */
internal fun invulTekst(cent: Long) = zonderEuro(cent).replace(".", "")

/** Crème klei-kaart (r30) met ruimte ertussen. */
@Composable
internal fun Paneel(modifier: Modifier = Modifier, ruimte: Dp = 12.dp, midden: Boolean = false, inhoud: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
            .padding(18.dp),
        horizontalAlignment = if (midden) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(ruimte),
        content = inhoud,
    )
}

/** Witte regel in een paneel (bv. een categorie of een vast potje). */
@Composable
internal fun WitteRegel(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, uitleg: String? = null, inhoud: @Composable RowScope.() -> Unit) {
    val vorm = RoundedCornerShape(18.dp)
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .shadow(3.dp, vorm, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
            .clip(vorm)
            .background(Color.White)
            .let { if (onClick != null) it.clickable(onClickLabel = uitleg, onClick = onClick) else it }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) { inhoud() }
}

/** Grote knop over de hele breedte met rond icoon-vlakje, titel en onderregel (Verdelen, Ik heb iets gekocht). */
@Composable
internal fun BalkKnop(
    emoji: String?,
    titel: String,
    onder: String,
    van: Color,
    naar: Color,
    open: Boolean,
    onClick: () -> Unit,
    icoon: (@Composable () -> Unit)? = null,
) {
    val vorm = RoundedCornerShape(30.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 78.dp)
            .shadow(12.dp, vorm, ambientColor = naar, spotColor = naar)
            .clip(vorm)
            .background(hoekVerloop(van, naar))
            .glans(vorm, licht = 0.4f, donker = 0.08f, boven = 4.dp, onder = 5.dp)
            .selectable(selected = open, role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(52.dp).background(Color.White.copy(alpha = 0.3f), CircleShape), contentAlignment = Alignment.Center) {
            if (icoon != null) icoon() else Tekst(emoji.orEmpty(), 26.sp)
        }
        Column(Modifier.weight(1f)) {
            Tekst(titel, 21.sp, kleur = Color.White, regel = 24.sp)
            Tekst(onder, 15.sp, dikte = FontWeight.Bold, kleur = Color.White.copy(alpha = 0.95f))
        }
        Tekst(if (open) "▲" else "▼", 16.sp, kleur = Color.White.copy(alpha = 0.8f))
    }
}

/** Groene knop "✓ Toevoegen" / "Nu verdelen". */
@Composable
internal fun GroeneKnop(tekst: String, onClick: () -> Unit, modifier: Modifier = Modifier, van: Color = GroenLicht, naar: Color = Groen) {
    val vorm = CircleShape
    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .shadow(8.dp, vorm, ambientColor = naar, spotColor = naar)
            .clip(vorm)
            .background(hoekVerloop(van, naar))
            .glans(vorm, licht = 0.4f, donker = 0f)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Tekst(tekst, 19.sp, kleur = Color.White, uitlijning = TextAlign.Center)
    }
}

/** Een rij knopjes waarvan er één aan staat (Dag/Week/Maand, Uitgave/Inkomen/Sparen). */
@Composable
internal fun <T> Segment(
    keuzes: List<T>,
    gekozen: T,
    label: (T) -> String,
    onKies: (T) -> Unit,
    modifier: Modifier = Modifier,
    aan: Brush = hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal),
    uit: Color = Color.Transparent,
    achter: Color = Color.White,
    maat: TextUnit = 15.sp,
    hoogte: Dp = 48.dp,
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(achter, CircleShape)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        keuzes.forEach { k ->
            val op = k == gekozen
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = hoogte)
                    .clip(CircleShape)
                    .background(if (op) aan else SolidColor(uit))
                    .selectable(selected = op, role = Role.Tab, onClick = { onKies(k) })
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Tekst(label(k), maat, kleur = if (op) Color.White else Color(0xFF7A4A38), uitlijning = TextAlign.Center, eenRegel = true)
            }
        }
    }
}

/** Wit pilletje met koraal randje als hij gekozen is ("Vrije geld", "€5"). */
@Composable
internal fun Chip(tekst: String, aan: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, rand: Color = Kleur.Koraal, vorm: RoundedCornerShape = CircleShape) {
    Box(
        modifier
            .heightIn(min = 48.dp)
            .shadow(4.dp, vorm, ambientColor = Kleur.KaartSchaduw, spotColor = Kleur.KaartSchaduw)
            .clip(vorm)
            .background(Color.White)
            .border(3.dp, if (aan) rand else Color.Transparent, vorm)
            .selectable(selected = aan, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Tekst(tekst, 15.sp, dikte = FontWeight.ExtraBold, kleur = Kleur.Inkt, uitlijning = TextAlign.Center)
    }
}

/** Groot bedrag-veld: "€ 5,00" met een streep eronder. */
@Composable
internal fun BedragVeld(
    tekst: String,
    onWijzig: (String) -> Unit,
    uitleg: String,
    maat: TextUnit = 42.sp,
    streep: Color = Rand,
    breed: Dp = 180.dp,
    rechts: @Composable () -> Unit = {},
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
        Tekst("€", maat * 0.9f, kleur = Bruin)
        BasicTextField(
            value = tekst,
            onValueChange = { nieuw -> onWijzig(nieuw.filter { it.isDigit() || it == ',' || it == '.' }.take(12)) },
            singleLine = true,
            textStyle = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = maat, color = Bruin, textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            cursorBrush = SolidColor(Kleur.Koraal),
            modifier = Modifier
                .width(breed)
                .semantics { contentDescription = uitleg }
                .drawBehind {
                    val y = size.height - 1.5.dp.toPx()
                    drawLine(streep, Offset(0f, y), Offset(size.width, y), strokeWidth = 3.dp.toPx())
                },
            decorationBox = { veld ->
                Box(contentAlignment = Alignment.Center) {
                    if (tekst.isEmpty()) Tekst("0,00", maat, kleur = Color(0xFFD8C4B6))
                    veld()
                }
            },
        )
        rechts()
    }
}

/** Kopje in het Geld-scherm: "🫙 Uit welk potje?" */
@Composable
internal fun GeldKopje(tekst: String) = Tekst(tekst, 17.sp, kleur = Bruin)

/** Rond icoon-bolletje met emoji op een kleurtje. */
@Composable
internal fun EmojiBol(emoji: String, kleur: Color, maat: Dp = 40.dp) {
    Box(Modifier.size(maat).background(kleur, CircleShape), contentAlignment = Alignment.Center) {
        Tekst(emoji, (maat.value * 0.55f).sp)
    }
}
