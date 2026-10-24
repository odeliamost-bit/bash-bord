package nl.odelia.dashboard.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.PathParser
import nl.odelia.dashboard.R
import kotlin.math.cos
import kotlin.math.sin

// Een widget kan geen Compose-tekeningen tonen. Daarom tekenen we de ronde knop en de ring als plaatje.

/** Kleuren van de klokknop (uit ontwerp/Widget.dc.html). */
class KnopKleur(val licht: Int, val donker: Int, val voet: Int)

val KnopGroen = KnopKleur(0xFF8CC7A0.toInt(), 0xFF4F9168.toInt(), 0xFF3F7A58.toInt())
val KnopRoze = KnopKleur(0xFFF29AB8.toInt(), 0xFFCC4F80.toInt(), 0xFFA93A66.toInt())

private const val UIT_ICOON = "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"
private const val IN_ICOON = "M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4M10 17l5-5-5-5M15 12H3"

private fun Context.px(dp: Float) = dp * resources.displayMetrics.density

/** Ronde 3D-knop met icoon en "UIT" of "IN", [maatDp] breed. */
fun klokKnop(context: Context, isIn: Boolean, maatDp: Float): Bitmap {
    val k = if (isIn) KnopGroen else KnopRoze
    val maat = context.px(maatDp)
    val voet = context.px(6f)
    val bmp = Bitmap.createBitmap(maat.toInt(), (maat + voet).toInt(), Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val r = maat / 2f
    val p = Paint(Paint.ANTI_ALIAS_FLAG)

    // Voetje onder de knop, dan de knop met verloop, dan de witte rand
    p.color = k.voet
    c.drawCircle(r, r + voet, r, p)
    val hoek = Math.toRadians(160.0)
    val dx = sin(hoek).toFloat() * r
    val dy = -cos(hoek).toFloat() * r
    p.shader = LinearGradient(r - dx, r - dy, r + dx, r + dy, k.licht, k.donker, Shader.TileMode.CLAMP)
    c.drawCircle(r, r, r, p)
    p.shader = null
    p.style = Paint.Style.STROKE
    p.strokeWidth = context.px(5f)
    p.color = 0xFFFFFFFF.toInt()
    c.drawCircle(r, r, r - p.strokeWidth / 2f, p)

    // Icoon (24×24 uit het ontwerp), wit
    val icoonMaat = maat * 0.32f
    c.save()
    c.translate(r - icoonMaat / 2f, maat * 0.2f)
    c.scale(icoonMaat / 24f, icoonMaat / 24f)
    p.strokeWidth = 2.6f
    p.strokeCap = Paint.Cap.ROUND
    p.strokeJoin = Paint.Join.ROUND
    c.drawPath(PathParser.createPathFromPathData(if (isIn) UIT_ICOON else IN_ICOON), p)
    c.restore()

    // "UIT" / "IN" in Nunito
    p.style = Paint.Style.FILL
    p.textAlign = Paint.Align.CENTER
    p.textSize = maat * 0.22f
    p.typeface = ResourcesCompat.getFont(context, R.font.nunito_black)
    c.drawText(if (isIn) "UIT" else "IN", r, maat * 0.76f, p)
    return bmp
}

/** Dunne ring: grijs-lila met daarover het deel van het weekdoel dat al gewerkt is. */
fun urenRing(context: Context, deel: Float, maatDp: Float): Bitmap {
    val maat = context.px(maatDp)
    val bmp = Bitmap.createBitmap(maat.toInt(), maat.toInt(), Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val dikte = maat * 0.14f
    val vak = RectF(dikte / 2 + maat * 0.06f, dikte / 2 + maat * 0.06f, maat - dikte / 2 - maat * 0.06f, maat - dikte / 2 - maat * 0.06f)
    val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dikte
        strokeCap = Paint.Cap.ROUND
    }
    p.color = 0xFFF1EBF8.toInt()
    c.drawArc(vak, 0f, 360f, false, p)
    if (deel > 0f) {
        p.color = 0xFFB9A3F0.toInt()
        c.drawArc(vak, -90f, 360f * deel.coerceAtMost(1f), false, p)
    }
    return bmp
}
