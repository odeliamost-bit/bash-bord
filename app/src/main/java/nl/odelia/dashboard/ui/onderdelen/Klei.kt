package nl.odelia.dashboard.ui.onderdelen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/** Verloop onder een hoek, zoals CSS `linear-gradient(160deg, ...)`. */
fun hoekVerloop(van: Color, naar: Color, graden: Float = 160f): Brush =
    HoekVerloop(listOf(van, naar), graden)

fun hoekVerloop(van: Color, midden: Color, naar: Color, graden: Float = 160f): Brush =
    HoekVerloop(listOf(van, midden, naar), graden)

/** Rond verloop vanuit een punt, zoals CSS `radial-gradient(circle at 35% 30%, ...)`. */
fun rondVerloop(vararg stops: Pair<Float, Color>, x: Float = 0.35f, y: Float = 0.3f): Brush =
    RondVerloop(stops.toList(), x, y)

private class HoekVerloop(val kleuren: List<Color>, val graden: Float) : ShaderBrush() {
    override fun createShader(size: Size): Shader {
        val rad = Math.toRadians(graden.toDouble())
        val dx = sin(rad).toFloat()
        val dy = -cos(rad).toFloat()
        val half = (abs(size.width * dx) + abs(size.height * dy)) / 2f
        val midden = size.center
        return LinearGradientShader(
            from = Offset(midden.x - dx * half, midden.y - dy * half),
            to = Offset(midden.x + dx * half, midden.y + dy * half),
            colors = kleuren,
        )
    }

    override fun equals(other: Any?) =
        other is HoekVerloop && other.kleuren == kleuren && other.graden == graden

    override fun hashCode() = kleuren.hashCode() * 31 + graden.hashCode()
}

private class RondVerloop(val stops: List<Pair<Float, Color>>, val x: Float, val y: Float) : ShaderBrush() {
    override fun createShader(size: Size): Shader {
        val c = Offset(size.width * x, size.height * y)
        val straal = maxOf(
            hypot(c.x, c.y),
            hypot(size.width - c.x, c.y),
            hypot(c.x, size.height - c.y),
            hypot(size.width - c.x, size.height - c.y),
        )
        return RadialGradientShader(
            center = c,
            radius = straal,
            colors = stops.map { it.second },
            colorStops = stops.map { it.first },
        )
    }

    override fun equals(other: Any?) =
        other is RondVerloop && other.stops == stops && other.x == x && other.y == y

    override fun hashCode() = (stops.hashCode() * 31 + x.hashCode()) * 31 + y.hashCode()
}

/** Kaart met "klei"-gevoel: zachte schaduw, glans bovenaan, wit randje. */
fun Modifier.klei(kleur: Color, vorm: Shape, schaduw: Color, hoogte: Dp = 12.dp): Modifier =
    klei(SolidColor(kleur), vorm, schaduw, hoogte)

fun Modifier.klei(vulling: Brush, vorm: Shape, schaduw: Color, hoogte: Dp = 12.dp): Modifier =
    shadow(hoogte, vorm, clip = false, ambientColor = schaduw, spotColor = schaduw)
        .background(vulling, vorm)
        .glans(vorm)
        .border(1.dp, Color.White.copy(alpha = 0.7f), vorm)

/**
 * Witte glans langs de bovenrand en een zacht donker randje onderaan,
 * zoals CSS `inset 0 3px 0 wit, inset 0 -4px 0 zwart`.
 */
fun Modifier.glans(
    vorm: Shape,
    licht: Float = 0.7f,
    donker: Float = 0.08f,
    boven: Dp = 3.dp,
    onder: Dp = 4.dp,
): Modifier = drawWithCache {
    val basis = Path().apply { addOutline(vorm.createOutline(size, layoutDirection, this@drawWithCache)) }
    val randBoven = basis.zonder(basis.verschoven(boven.toPx()))
    val randOnder = basis.zonder(basis.verschoven(-onder.toPx()))
    onDrawBehind {
        drawPath(randBoven, Color.White.copy(alpha = licht))
        drawPath(randOnder, Color.Black.copy(alpha = donker))
    }
}

/** Stevige "voet" onder een knop, zoals CSS `0 6px 0 donkerder`. Zet hem vóór de achtergrond. */
fun Modifier.voet(kleur: Color, vorm: Shape, diepte: Dp): Modifier = drawBehind {
    val omtrek = vorm.createOutline(size, layoutDirection, this)
    translate(top = diepte.toPx()) { drawOutline(omtrek, kleur) }
}

private fun Path.verschoven(dy: Float) = Path().also { it.addPath(this, Offset(0f, dy)) }

private fun Path.zonder(ander: Path) = Path().also { it.op(this, ander, PathOperation.Difference) }
