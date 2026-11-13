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
import androidx.glance.layout.RowScope
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
import nl.odelia.dashboard.meldingen.Meldingen

/** Snel vertalen vanaf het startscherm: typen, plakken of inspreken. Opent het Vertalen-scherm. */
class VertaalWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Row(GlanceModifier.fillMaxSize().padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Knop(context, "🌍", "Vertalen", "vertalen", Color(0xFF6E95C3))
                Spacer(GlanceModifier.width(8.dp))
                Knop(context, "📋", "Plakken", "vertalen-plak", Color(0xFFD9694F))
                Spacer(GlanceModifier.width(8.dp))
                Knop(context, "🎤", "Inspreken", "vertalen-spreek", Color(0xFF8FAE7E))
            }
        }
    }

    @Composable
    private fun RowScope.Knop(context: Context, groot: String, klein: String, scherm: String, kleur: Color) {
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
            Text(groot, style = TextStyle(color = ColorProvider(Color.White), fontSize = 28.sp, fontWeight = FontWeight.Bold))
            Text(klein, style = TextStyle(color = ColorProvider(Color.White), fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center))
        }
    }
}

class VertaalWidgetOntvanger : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = VertaalWidget()
}
