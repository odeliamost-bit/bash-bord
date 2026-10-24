package nl.odelia.dashboard.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.time.LocalDateTime
import nl.odelia.dashboard.MainActivity
import nl.odelia.dashboard.data.Instellingen
import nl.odelia.dashboard.data.KlokOpslag
import nl.odelia.dashboard.ui.thuis.alsBreuk
import nl.odelia.dashboard.ui.thuis.tijdTekst
import nl.odelia.dashboard.uren.KlokStaat

/**
 * Widget op het startscherm (ontwerp/Widget.dc.html):
 * links de grote in/uit-knop (tik = wisselen), rechts de ring met de uren van deze week (tik = app open).
 */
class KlokWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val staat = KlokOpslag(context).nu().staat(LocalDateTime.now(), Instellingen.van(context).weekdoel.value)
        val knop = klokKnop(context, staat.isIn, 76f)
        val ring = urenRing(context, staat.weekTotaal / staat.weekdoel, 72f)
        provideContent { KlokWidgetInhoud(staat, ImageProvider(knop), ImageProvider(ring)) }
    }

    companion object {
        suspend fun ververs(context: Context) = KlokWidget().updateAll(context)
    }
}

class KlokWidgetOntvanger : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KlokWidget()
}

/** Tik op de knop in de widget: in- of uitklokken. Daarna ververst de widget zichzelf. */
class WisselActie : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        KlokOpslag(context).wissel()
    }
}

private val Inkt = Color(0xFF3A3046)

/** De inhoud van de widget (los, zodat een test er een plaatje van kan maken). */
@Composable
internal fun KlokWidgetInhoud(staat: KlokStaat, knop: ImageProvider, ring: ImageProvider) {
    Row(GlanceModifier.fillMaxSize().padding(4.dp)) {
        Box(
            GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .cornerRadius(32.dp)
                .background(if (staat.isIn) Color(0xFFD6EBDD) else Color(0xFFFFD6E3))
                .clickable(actionRunCallback<WisselActie>())
                .semantics { contentDescription = if (staat.isIn) "Uitklokken" else "Inklokken" },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(knop, contentDescription = null)
                if (staat.isIn) {
                    Regel("al ${alsBreuk(staat.brutoVandaag)} uur gewerkt", 15, Color(0xFF2F5E43))
                    Regel("sinds ${staat.sinds?.let(::tijdTekst) ?: ""}", 13, Color(0xFF3D5E4A))
                } else if (staat.uitVandaag != null) {
                    Regel("${alsBreuk(staat.nettoVandaag)} uur gewerkt", 15, Color(0xFF7A2D4E))
                    Regel("uit om ${tijdTekst(staat.uitVandaag)}", 13, Color(0xFF7A2D4E))
                } else {
                    Regel("nog niet gestart", 15, Color(0xFF7A2D4E))
                }
            }
        }
        Spacer(GlanceModifier.width(12.dp))
        Box(
            GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .cornerRadius(32.dp)
                .background(Color.White)
                .clickable(actionStartActivity<MainActivity>()),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(ring, contentDescription = "${alsBreuk(staat.weekTotaal)} van ${alsBreuk(staat.weekdoel)} uur")
                Regel(alsBreuk(staat.weekTotaal), 28, Inkt)
                Regel("van ${alsBreuk(staat.weekdoel)} uur", 14, Color(0xFF6B6078))
            }
        }
    }
}

@Composable
private fun Regel(tekst: String, maat: Int, kleur: Color) {
    Text(
        tekst,
        style = TextStyle(color = ColorProvider(kleur), fontSize = maat.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
        maxLines = 1,
    )
}
