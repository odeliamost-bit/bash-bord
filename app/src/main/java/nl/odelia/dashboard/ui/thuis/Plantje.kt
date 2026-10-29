package nl.odelia.dashboard.ui.thuis

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate

/**
 * Zelf getekend plantje in een rond zandkleurig potje op een hangend plankje.
 * Vervangt het plantje van internet uit het ontwerp.
 *
 * Tekenvlak is 74 breed. De rechter 14 steekt buiten de kaart uit;
 * daar houdt het plankje op (net als in het ontwerp).
 */
@Composable
fun Plantje(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val u = size.width / 74f
        val kaartRand = 60f * u
        val plankBoven = size.height - 12f * u
        val potMidden = 37f * u
        val potBoven = plankBoven - 25f * u

        // Touwtje met spijker
        val spijker = Offset(15f * u, size.height * 0.36f)
        drawLine(Color(0xFFB98A63), Offset(14f * u, plankBoven + 3f * u), spijker, strokeWidth = 2.2f * u, cap = StrokeCap.Round)
        drawCircle(Color(0xFFB98A63), radius = 5.2f * u, center = spijker)
        drawCircle(Color(0xFFD9A27A), radius = 3.7f * u, center = spijker)

        // Blaadjes (achter de pot): brede, ronde olijfgroene blaadjes in een waaier (zoals Odelia koos)
        val voet = Offset(potMidden, potBoven + 2f * u)
        val donker = Color(0xFF6E8F45)
        val midden = Color(0xFF86A657)
        val licht = Color(0xFFA0BC6E)
        blad(voet + Offset(-3f * u, 0f), 30f * u, 15f * u, -62f, midden)
        blad(voet + Offset(3f * u, 0f), 30f * u, 15f * u, 62f, midden)
        blad(voet, 40f * u, 16f * u, -32f, donker)
        blad(voet, 40f * u, 16f * u, 32f, donker)
        blad(voet, 48f * u, 17f * u, -6f, licht)

        // Potje: rond en buikig, zandkleur
        val potOnder = plankBoven
        val pot = Path().apply {
            moveTo(potMidden - 14f * u, potBoven)
            cubicTo(potMidden - 22f * u, potBoven + 8f * u, potMidden - 18f * u, potOnder - 2f * u, potMidden - 10f * u, potOnder)
            lineTo(potMidden + 10f * u, potOnder)
            cubicTo(potMidden + 18f * u, potOnder - 2f * u, potMidden + 22f * u, potBoven + 8f * u, potMidden + 14f * u, potBoven)
            close()
        }
        drawPath(pot, Brush.verticalGradient(listOf(Color(0xFFF4DFC2), Color(0xFFDDB88E)), potBoven, potOnder))
        drawRoundRect(
            Brush.verticalGradient(listOf(Color(0xFFF8E9D2), Color(0xFFE7C9A0)), potBoven - 3f * u, potBoven + 4f * u),
            topLeft = Offset(potMidden - 15f * u, potBoven - 3f * u),
            size = Size(30f * u, 7f * u),
            cornerRadius = CornerRadius(3.5f * u),
        )
        drawLine(Color.White.copy(alpha = 0.4f), Offset(potMidden - 11f * u, potBoven + 8f * u), Offset(potMidden - 10f * u, potOnder - 6f * u), strokeWidth = 2.5f * u, cap = StrokeCap.Round)

        // Plankje: links rond, rechts recht tegen de kaartrand
        val plank = RoundRect(
            rect = Rect(8f * u, plankBoven, kaartRand, size.height),
            topLeft = CornerRadius(6f * u),
            bottomLeft = CornerRadius(6f * u),
        )
        translate(-3f * u, 5f * u) {
            drawPath(Path().apply { addRoundRect(plank) }, Color(0xFF965032).copy(alpha = 0.18f))
        }
        drawPath(
            Path().apply { addRoundRect(plank) },
            Brush.verticalGradient(listOf(Color(0xFFF2C9A0), Color(0xFFDDA67C)), plankBoven, size.height),
        )
        drawLine(Color.White.copy(alpha = 0.5f), Offset(12f * u, plankBoven + 1.5f * u), Offset(kaartRand, plankBoven + 1.5f * u), strokeWidth = 2.5f * u)
    }
}

/** Eén blaadje. [hoek] in graden: 0 = recht omhoog, positief = naar rechts. */
private fun DrawScope.blad(voet: Offset, lengte: Float, breedte: Float, hoek: Float, kleur: Color) {
    val vorm = Path().apply {
        moveTo(voet.x, voet.y)
        // Steeltje onderaan, daarboven een breed rond blad
        lineTo(voet.x, voet.y - lengte * 0.3f)
        cubicTo(voet.x + breedte, voet.y - lengte * 0.35f, voet.x + breedte * 0.9f, voet.y - lengte * 0.9f, voet.x, voet.y - lengte)
        cubicTo(voet.x - breedte * 0.9f, voet.y - lengte * 0.9f, voet.x - breedte, voet.y - lengte * 0.35f, voet.x, voet.y - lengte * 0.3f)
        close()
    }
    rotate(hoek, pivot = voet) {
        drawLine(kleur, voet, Offset(voet.x, voet.y - lengte * 0.32f), strokeWidth = breedte * 0.16f, cap = StrokeCap.Round)
        drawPath(vorm, kleur)
        drawLine(
            Color.White.copy(alpha = 0.35f),
            Offset(voet.x, voet.y - lengte * 0.25f),
            Offset(voet.x, voet.y - lengte * 0.8f),
            strokeWidth = breedte * 0.14f,
            cap = StrokeCap.Round,
        )
    }
}
