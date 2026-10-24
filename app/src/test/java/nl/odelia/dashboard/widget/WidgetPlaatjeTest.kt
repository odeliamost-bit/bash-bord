package nl.odelia.dashboard.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.FrameLayout
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceId
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.compose
import androidx.glance.appwidget.provideContent
import androidx.test.core.app.ApplicationProvider
import java.io.File
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import nl.odelia.dashboard.ui.thuis.Voorbeeld
import nl.odelia.dashboard.uren.KlokStaat
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Tekent de widget als plaatje in app/build/plaatjes (zonder telefoon), om te kunnen controleren hoe hij eruitziet.
 * Het is geen echte telefoon: letters en schaduwen kunnen iets anders zijn.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "xxhdpi")
class WidgetPlaatjeTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private class Voorbeeldwidget(val staat: KlokStaat) : GlanceAppWidget() {
        override suspend fun provideGlance(context: Context, id: GlanceId) {
            val knop = klokKnop(context, staat.isIn, 76f)
            val ring = urenRing(context, staat.weekTotaal / staat.weekdoel, 72f)
            provideContent { KlokWidgetInhoud(staat, ImageProvider(knop), ImageProvider(ring)) }
        }
    }

    @OptIn(ExperimentalGlanceApi::class)
    private fun teken(naam: String, staat: KlokStaat) = runBlocking {
        val maat = DpSize(320.dp, 160.dp)
        val views = Voorbeeldwidget(staat).compose(context, size = maat)
        val dichtheid = context.resources.displayMetrics.density
        val b = (maat.width.value * dichtheid).toInt()
        val h = (maat.height.value * dichtheid).toInt()
        val houder = FrameLayout(context)
        val view = views.apply(context, houder)
        houder.addView(view)
        houder.measure(View.MeasureSpec.makeMeasureSpec(b, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY))
        houder.layout(0, 0, b, h)
        val bmp = Bitmap.createBitmap(b, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(0xFFF8CDB9.toInt()) // perzik, zoals de achtergrond van het startscherm in het ontwerp
        houder.draw(canvas)
        val map = File("build/plaatjes").apply { mkdirs() }
        File(map, "$naam.png").outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        assertTrue(File(map, "$naam.png").length() > 0)
    }

    @Test
    fun widgetAanHetWerk() = teken("widget-aan-het-werk", Voorbeeld.klok(LocalDateTime.of(2026, 10, 23, 13, 15)))

    @Test
    fun widgetNogNietGestart() = teken("widget-nog-niet-gestart", Voorbeeld.klok(LocalDateTime.of(2026, 10, 23, 7, 30)))

    @Test
    fun widgetKlaar() = teken("widget-klaar", Voorbeeld.klok(LocalDateTime.of(2026, 10, 23, 16, 30), vrijdagUit = "15:50"))
}
