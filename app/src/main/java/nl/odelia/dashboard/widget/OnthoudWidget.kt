package nl.odelia.dashboard.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
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
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import nl.odelia.dashboard.MainActivity
import nl.odelia.dashboard.data.OnthoudOpslag

/** Widget "Onthoud": snel iets vastleggen, typen of inspreken (CLAUDE.md stap 3: quick-add widget). */
class OnthoudWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val staat = OnthoudOpslag(context).staat()
        val open = staat.open.size
        val vandaag = staat.vandaagTeDoen.size
        val nieuw = Intent(context, MainActivity::class.java).setAction(MainActivity.ACTIE_ONTHOUD)
        val inspreken = Intent(context, MainActivity::class.java).setAction(MainActivity.ACTIE_INSPREKEN)
        provideContent { OnthoudWidgetInhoud(open, vandaag, nieuw, inspreken) }
    }

    companion object {
        suspend fun ververs(context: Context) = OnthoudWidget().updateAll(context)
    }
}

class OnthoudWidgetOntvanger : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = OnthoudWidget()
}

/** De inhoud van de widget (los, zodat een test er een plaatje van kan maken). */
@Composable
internal fun OnthoudWidgetInhoud(open: Int, vandaag: Int, nieuw: Intent, inspreken: Intent) {
    Column(
        GlanceModifier.fillMaxSize().cornerRadius(28.dp).background(Color(0xFFFBF3EA)).padding(10.dp),
    ) {
        Text(
            "📌 $open nog doen · $vandaag vandaag",
            style = TextStyle(color = ColorProvider(Color(0xFF6E4A3A)), fontSize = 14.sp, fontWeight = FontWeight.Bold),
            maxLines = 1,
            modifier = GlanceModifier.padding(start = 6.dp, bottom = 6.dp),
        )
        Row(GlanceModifier.fillMaxWidth().defaultWeight()) {
            Box(
                GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight()
                    .cornerRadius(22.dp)
                    .background(Color(0xFFE07A62))
                    .clickable(actionStartActivity(nieuw))
                    .semantics { contentDescription = "Iets onthouden" },
                contentAlignment = Alignment.Center,
            ) {
                Text("+  Iets onthouden", style = TextStyle(color = ColorProvider(Color.White), fontSize = 17.sp, fontWeight = FontWeight.Bold), maxLines = 1)
            }
            Spacer(GlanceModifier.width(10.dp))
            Box(
                GlanceModifier
                    .width(64.dp)
                    .fillMaxHeight()
                    .cornerRadius(22.dp)
                    .background(Color(0xFFB9A3F0))
                    .clickable(actionStartActivity(inspreken))
                    .semantics { contentDescription = "Inspreken" },
                contentAlignment = Alignment.Center,
            ) {
                Text("🎤", style = TextStyle(fontSize = 24.sp))
            }
        }
        Spacer(GlanceModifier.height(2.dp))
    }
}
