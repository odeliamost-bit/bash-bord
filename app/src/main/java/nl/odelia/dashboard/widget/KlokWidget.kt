package nl.odelia.dashboard.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import nl.odelia.dashboard.Acties
import nl.odelia.dashboard.R
import nl.odelia.dashboard.data.Opslag
import nl.odelia.dashboard.logica.KlokStaat
import nl.odelia.dashboard.logica.alsBreuk
import nl.odelia.dashboard.logica.alsKlok

/**
 * Widget op het startscherm (ontwerp/Widget.dc.html): links de IN/UIT-knop, rechts de uren van de week.
 * Tik op de knop = in- of uitklokken. Android ververst widgets niet elke minuut,
 * daarom staat "sinds 7:45" erbij; de uren lopen soms een paar minuten achter.
 */
class KlokWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val klok = Opslag.van(context).klokNu()
        val ring = ringPlaatje(klok.weekUren / klok.weekdoel)
        provideContent { Inhoud(klok, ring) }
    }

    @Composable
    private fun Inhoud(klok: KlokStaat, ring: Bitmap) {
        val groen = klok.isIn
        Row(
            GlanceModifier.fillMaxSize().padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Knop
            Column(
                GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight()
                    .cornerRadius(28.dp)
                    .background(ColorProvider(if (groen) Color(0xFFD6EBDD) else Color(0xFFFFD6E3)))
                    .clickable(actionRunCallback<WisselKlok>()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    GlanceModifier
                        .size(76.dp)
                        .background(ImageProvider(if (groen) R.drawable.widget_knop_groen else R.drawable.widget_knop_roze)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (groen) "UIT" else "IN",
                        style = TextStyle(color = ColorProvider(Color.White), fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    )
                }
                Spacer(GlanceModifier.height(6.dp))
                val donker = ColorProvider(if (groen) Color(0xFF2F5E43) else Color(0xFF7A2D4E))
                val regel1 = when {
                    groen -> "al ${alsBreuk(klok.vandaag.uren)} uur gewerkt"
                    klok.vandaag.eind != null -> "${alsBreuk(klok.vandaag.uren)} uur gewerkt"
                    else -> "nog niet gestart"
                }
                Text(regel1, style = TextStyle(color = donker, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center))
                klok.sinds?.let {
                    Text("sinds ${alsKlok(it)}", style = TextStyle(color = donker, fontSize = 12.sp, textAlign = TextAlign.Center))
                }
            }

            Spacer(GlanceModifier.width(8.dp))

            // Week
            Column(
                GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight()
                    .cornerRadius(28.dp)
                    .background(ColorProvider(Color.White)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(ImageProvider(ring), contentDescription = null, modifier = GlanceModifier.size(84.dp))
                    Text(
                        alsBreuk(klok.weekUren),
                        style = TextStyle(color = ColorProvider(Color(0xFF3A3046)), fontSize = 22.sp, fontWeight = FontWeight.Bold),
                    )
                }
                Text(
                    "van ${alsBreuk(klok.weekdoel)} uur",
                    style = TextStyle(color = ColorProvider(Color(0xFF6B6078)), fontSize = 13.sp, fontWeight = FontWeight.Bold),
                )
            }
        }
    }

    /** Dunne lila ring (Glance kan zelf geen rondjes tekenen, dus een plaatje). */
    private fun ringPlaatje(deel: Float): Bitmap {
        val maat = 168
        val bitmap = Bitmap.createBitmap(maat, maat, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val dikte = 26f
        val vak = RectF(dikte / 2, dikte / 2, maat - dikte / 2, maat - dikte / 2)
        val pen = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = dikte }
        canvas.drawArc(vak, 0f, 360f, false, pen.apply { color = 0xFFF1EBF8.toInt() })
        pen.strokeCap = Paint.Cap.ROUND
        canvas.drawArc(vak, -90f, 360f * deel.coerceIn(0f, 1f), false, pen.apply { color = 0xFFB9A3F0.toInt() })
        return bitmap
    }
}

class WisselKlok : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        Acties.wisselKlok(context)
    }
}

class KlokWidgetOntvanger : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = KlokWidget()
}
