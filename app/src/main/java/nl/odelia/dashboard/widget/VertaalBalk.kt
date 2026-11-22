package nl.odelia.dashboard.widget

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import nl.odelia.dashboard.online.Vertaler

/**
 * Zwart balkje onderaan het scherm, over andere apps heen ("tekenen over andere apps").
 * Tik erop = weg. Na 40 seconden gaat het vanzelf weg.
 * Zonder die toestemming: een gewoon Android-berichtje (Toast) als reserve.
 */
object VertaalBalk {
    private const val WEG_NA_MS = 40_000L
    private val hoofd = Handler(Looper.getMainLooper())
    private val bezig = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val weg = Runnable { verberg() }
    private var balk: TextView? = null
    private var vensters: WindowManager? = null

    fun mag(context: Context) = Settings.canDrawOverlays(context)

    /** Vertaal [gesproken] en laat het resultaat in het balkje zien. */
    fun vertaal(context: Context, gesproken: String) {
        val app = context.applicationContext
        toon(app, "⏳ Even vertalen…\n“$gesproken”")
        bezig.launch {
            val tekst = try {
                "🌍 ${Vertaler.vertaal(gesproken)}\n\n“$gesproken”"
            } catch (_: Exception) {
                "😕 Vertalen lukte niet. Is er internet? Probeer het nog eens."
            }
            toon(app, tekst)
        }
    }

    fun toon(context: Context, tekst: String) {
        val app = context.applicationContext
        if (!mag(app)) {
            Toast.makeText(app, tekst, Toast.LENGTH_LONG).show()
            return
        }
        val v = balk ?: maak(app) ?: run {
            Toast.makeText(app, tekst, Toast.LENGTH_LONG).show()
            return
        }
        v.text = tekst
        hoofd.removeCallbacks(weg)
        hoofd.postDelayed(weg, WEG_NA_MS)
    }

    fun verberg() {
        hoofd.removeCallbacks(weg)
        balk?.let { runCatching { vensters?.removeView(it) } }
        balk = null
    }

    private fun maak(app: Context): TextView? {
        val wm = app.getSystemService(WindowManager::class.java) ?: return null
        val dp = app.resources.displayMetrics.density
        val v = TextView(app).apply {
            setBackgroundColor(Color.BLACK)
            setTextColor(Color.WHITE)
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            val rand = (18 * dp).toInt()
            setPadding(rand, rand, rand, rand)
            maxLines = 12
            setOnClickListener { verberg() }
        }
        val plek = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply { gravity = Gravity.BOTTOM }
        return runCatching { wm.addView(v, plek) }.map {
            vensters = wm
            balk = v
            v
        }.getOrNull()
    }
}
