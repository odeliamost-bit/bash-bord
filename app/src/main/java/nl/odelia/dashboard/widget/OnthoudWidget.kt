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
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import nl.odelia.dashboard.MainActivity
import nl.odelia.dashboard.data.Opslag
import nl.odelia.dashboard.logica.Overzicht
import nl.odelia.dashboard.meldingen.Meldingen

/** Snel iets onthouden vanaf het startscherm: typen/plakken (＋) of inspreken (🎤). */
class OnthoudWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val vandaag = Overzicht.van(Opslag.van(context).onthoudNu(), java.time.LocalDate.now()).vandaag.size
        provideContent { Inhoud(context, vandaag) }
    }

    @Composable
    private fun Inhoud(context: Context, vandaag: Int) {
        Row(GlanceModifier.fillMaxSize().padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Knop(context, "＋", "Onthoud", "toevoegen", Color(0xFFD9694F))
            Spacer(GlanceModifier.width(8.dp))
            Knop(context, "🎤", "Inspreken", "inspreken", Color(0xFF6E95C3))
            Spacer(GlanceModifier.width(8.dp))
            Knop(context, if (vandaag == 0) "✓" else "$vandaag", "vandaag", "onthoud", Color(0xFFE3A93E))
        }
    }

    @Composable
    private fun androidx.glance.layout.RowScope.Knop(context: Context, groot: String, klein: String, scherm: String, kleur: Color) {
        val intent = Intent(context, MainActivity::class.java)
            .setAction("nl.odelia.dashboard.WIDGET_$scherm")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(Meldingen.EXTRA_SCHERM, scherm)
        Column(
            GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .cornerRadius(24.dp)
                .background(ColorProvider(kleur))
                .clickable(actionStartActivity(intent)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(groot, style = TextStyle(color = ColorProvider(Color.White), fontSize = 30.sp, fontWeight = FontWeight.Bold))
            Text(klein, style = TextStyle(color = ColorProvider(Color.White), fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center))
        }
    }
}

class OnthoudWidgetOntvanger : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = OnthoudWidget()
}
