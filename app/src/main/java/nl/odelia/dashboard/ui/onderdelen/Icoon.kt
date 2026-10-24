package nl.odelia.dashboard.ui.onderdelen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Eén vorm uit een SVG-icoontje uit het ontwerp (viewBox 0 0 24 24). */
class Vorm(d: String, val vul: Color? = null, val lijn: Color? = null, val dikte: Float = 2f) {
    val pad: Path = PathParser().parsePathString(d).toPath()
}

/** SVG `<circle>` als pad. */
fun cirkel(cx: Float, cy: Float, r: Float) =
    "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0z"

/** SVG `<rect rx=..>` als pad. */
fun rechthoek(x: Float, y: Float, b: Float, h: Float, r: Float) =
    "M${x + r} ${y}h${b - 2 * r}a$r $r 0 0 1 $r ${r}v${h - 2 * r}a$r $r 0 0 1 ${-r} ${r}" +
        "h${2 * r - b}a$r $r 0 0 1 ${-r} ${-r}v${2 * r - h}a$r $r 0 0 1 $r ${-r}z"

/** Tekent een icoontje. Met [schaduw] krijgt het een zacht 3D-schaduwtje eronder. */
@Composable
fun Icoon(vormen: List<Vorm>, maat: Dp, modifier: Modifier = Modifier, schaduw: Color? = null) {
    Canvas(modifier.size(maat)) {
        scale(size.minDimension / 24f, pivot = Offset.Zero) {
            if (schaduw != null) {
                for (stap in 1..3) {
                    translate(top = stap * 0.7f) { teken(vormen, schaduw.copy(alpha = schaduw.alpha * 0.25f)) }
                }
            }
            teken(vormen, null)
        }
    }
}

private fun DrawScope.teken(vormen: List<Vorm>, alles: Color?) {
    for (v in vormen) {
        v.vul?.let { drawPath(v.pad, alles ?: it) }
        v.lijn?.let {
            drawPath(v.pad, alles ?: it, style = Stroke(v.dikte, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

/** Gekleurd 3D-blokje met een icoontje erin (zoals op de tegels). */
@Composable
fun IcoonBlok(
    licht: Color,
    donker: Color,
    vormen: List<Vorm>,
    modifier: Modifier = Modifier,
    maat: Dp = 50.dp,
    hoek: Dp = 16.dp,
    icoonMaat: Dp = 28.dp,
) {
    val vorm = RoundedCornerShape(hoek)
    Box(
        modifier
            .size(maat)
            .shadow(6.dp, vorm, ambientColor = donker, spotColor = donker)
            .background(hoekVerloop(licht, donker), vorm)
            .glans(vorm, licht = 0.4f, donker = 0.12f),
        contentAlignment = Alignment.Center,
    ) {
        Icoon(vormen, icoonMaat)
    }
}
