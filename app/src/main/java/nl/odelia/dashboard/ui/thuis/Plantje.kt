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
 * Zelf getekend plantje in een potje op een hangend plankje.
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

        // Blaadjes (achter de pot)
        val voet = Offset(potMidden, potBoven + 3f * u)
        val donker = Color(0xFF5E9E6E)
        val midden = Color(0xFF7DB889)
        val licht = Color(0xFF9FD0A0)
        blad(voet + Offset(-6f * u, 0f), 24f * u, 9f * u, -128f, midden) // hangt links over de rand
        blad(voet + Offset(6f * u, 0f), 20f * u, 8f * u, 132f, licht) // hangt rechts over de rand
        blad(voet, 40f * u, 11f * u, -58f, donker)
        blad(voet, 46f * u, 12f * u, -30f, midden)
        blad(voet, 50f * u, 12f * u, -6f, licht)
        blad(voet, 44f * u, 12f * u, 18f, donker)
        blad(voet, 38f * u, 11f * u, 44f, midden)
        blad(voet, 30f * u, 10f * u, 68f, licht)

        // Potje
        val pot = Path().apply {
            moveTo(potMidden - 15f * u, potBoven)
            lineTo(potMidden + 15f * u, potBoven)
            lineTo(potMidden + 11f * u, plankBoven)
            lineTo(potMidden - 11f * u, plankBoven)
            close()
        }
        drawPath(pot, Brush.verticalGradient(listOf(Color(0xFFEBA084), Color(0xFFC9704F)), potBoven, plankBoven))
        drawRoundRect(
            Brush.verticalGradient(listOf(Color(0xFFF4B89A), Color(0xFFDD8C6D)), potBoven - 3f * u, potBoven + 5f * u),
            topLeft = Offset(potMidden - 17f * u, potBoven - 3f * u),
            size = Size(34f * u, 8f * u),
            cornerRadius = CornerRadius(4f * u),
        )
        drawLine(Color.White.copy(alpha = 0.35f), Offset(potMidden - 9f * u, potBoven + 9f * u), Offset(potMidden - 7.5f * u, plankBoven - 5f * u), strokeWidth = 2.5f * u, cap = StrokeCap.Round)

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
        cubicTo(voet.x + breedte, voet.y - lengte * 0.3f, voet.x + breedte * 0.8f, voet.y - lengte * 0.8f, voet.x, voet.y - lengte)
        cubicTo(voet.x - breedte * 0.8f, voet.y - lengte * 0.8f, voet.x - breedte, voet.y - lengte * 0.3f, voet.x, voet.y)
        close()
    }
    rotate(hoek, pivot = voet) {
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
