package nl.odelia.dashboard.ui.thuis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.R
import nl.odelia.dashboard.ui.onderdelen.Icoon
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.glans
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur

/**
 * "Goedemorgen, Odelia!" met haar meisje links (hoofd steekt boven de kaart uit)
 * en het plantje rechts op een hangend plankje.
 * Maten komen uit het ontwerp (kaart 354 breed, 168 hoog). Is de kaart smaller,
 * dan wordt het meisje iets kleiner en valt links een stukje haar weg.
 */
@Composable
fun WelkomKaart(groet: String, onderregel: String, onVoorlezen: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val breed = maxWidth
        val s = (breed / 354.dp).coerceIn(0.92f, 1f)
        val kaartHoogte = 168.dp
        val meisjeHoogte = max(196.dp * s, 188.dp)
        val meisjeBreedte = meisjeHoogte * (541f / 560f)
        val tekstStart = min(194.dp * s, breed - 148.dp)
        val venster = min(meisjeBreedte + 4.dp, tekstStart)
        val kaartVorm = RoundedCornerShape(28.dp)

        Box(Modifier.fillMaxWidth().height(meisjeHoogte)) {
            // Kaart
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(kaartHoogte)
                    .klei(hoekVerloop(Color(0xFFF9D9C6), Color(0xFFF3C3B1), Color(0xFFEDBAC4), graden = 125f), kaartVorm, Color(0xFFBE6E50)),
            ) {
                Belletjes(Modifier.matchParentSize().clip(kaartVorm))
            }

            // Plantje
            Plantje(
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 14.dp)
                    .padding(bottom = 22.dp)
                    .size(74.dp, 112.dp),
            )

            // Meisje: links en onder netjes afgeknipt, boven mag ze uitsteken
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .width(venster)
                    .height(meisjeHoogte)
                    .clip(RoundedCornerShape(bottomStart = 26.dp)),
            ) {
                Image(
                    painter = painterResource(R.drawable.meisje),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .wrapContentWidth(Alignment.End, unbounded = true)
                        .size(meisjeBreedte, meisjeHoogte),
                )
            }

            // Tekst
            Column(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = tekstStart, top = meisjeHoogte - kaartHoogte + 14.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Tekst("$groet,", 18.sp, kleur = Color(0xFF5A3426), regel = 20.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Tekst("Odelia!", 18.sp, kleur = Color(0xFF5A3426), regel = 20.sp)
                    Icoon(Icoontjes.zon, 19.dp)
                }
                Tekst(
                    onderregel,
                    14.sp,
                    Modifier.widthIn(max = 108.dp),
                    dikte = FontWeight.Bold,
                    kleur = Color(0xFF7A4A38),
                    regel = 17.sp,
                )
                VoorleesKnop(onVoorlezen, Modifier.padding(top = 6.dp))
            }
        }
    }
}

/** Zachte lichte rondjes op de achtergrond van de kaart. */
@Composable
private fun Belletjes(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawCircle(Color.White.copy(alpha = 0.22f), radius = h * 0.55f, center = Offset(w * 0.62f, h * 0.05f))
        drawCircle(Color(0xFFE6B6D6).copy(alpha = 0.35f), radius = h * 0.6f, center = Offset(w * 0.15f, h * 1.05f))
        drawCircle(Color.White.copy(alpha = 0.18f), radius = h * 0.18f, center = Offset(w * 0.86f, h * 0.2f))
    }
}

/** Koraal knopje "▶ Voorlezen". */
@Composable
fun VoorleesKnop(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .heightIn(min = 38.dp)
            .shadow(6.dp, CircleShape, ambientColor = Kleur.Koraal, spotColor = Kleur.Koraal)
            .clip(CircleShape)
            .background(hoekVerloop(Kleur.KoraalLicht, Kleur.Koraal))
            .glans(CircleShape, licht = 0.4f, donker = 0.1f, onder = 3.dp)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Voorlezen" }
            .padding(start = 10.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icoon(Icoontjes.afspelen, 14.dp)
        Tekst("Voorlezen", 14.sp, kleur = Color.White)
    }
}
