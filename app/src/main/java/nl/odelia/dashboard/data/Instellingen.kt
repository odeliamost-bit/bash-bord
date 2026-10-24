package nl.odelia.dashboard.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import nl.odelia.dashboard.uren.Uren

/** Kleine instellingen (geen database nodig). */
class Instellingen private constructor(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("instellingen", Context.MODE_PRIVATE)
    private val _weekdoel = MutableStateFlow(prefs.getFloat(WEEKDOEL, Uren.WEEKDOEL))

    /** Uren per week. Voorlopig 35 (Odelia wist het niet zeker, 24 okt). */
    val weekdoel: StateFlow<Float> = _weekdoel

    fun zetWeekdoel(uren: Float) {
        val goed = uren.coerceIn(1f, 60f)
        prefs.edit().putFloat(WEEKDOEL, goed).apply()
        _weekdoel.value = goed
    }

    companion object {
        private const val WEEKDOEL = "weekdoel"

        @Volatile private var enige: Instellingen? = null

        fun van(context: Context): Instellingen =
            enige ?: synchronized(this) { enige ?: Instellingen(context).also { enige = it } }
    }
}
