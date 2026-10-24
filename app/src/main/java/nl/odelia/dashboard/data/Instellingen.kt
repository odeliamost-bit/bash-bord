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

    private val _kimAdres = MutableStateFlow(prefs.getString(KIM_ADRES, "") ?: "")

    /** E-mailadres van Kim voor de urenmail (mag leeg: dan kies je haar zelf in Gmail). Alleen op de telefoon. */
    val kimAdres: StateFlow<String> = _kimAdres

    fun zetKimAdres(adres: String) {
        prefs.edit().putString(KIM_ADRES, adres.trim()).apply()
        _kimAdres.value = adres.trim()
    }

    /** Op welke dag het ochtendbericht het laatst kwam ("2026-10-23"), zodat het niet twee keer komt. */
    var laatsteOchtendbericht: String
        get() = prefs.getString(LAATSTE_OCHTEND, "") ?: ""
        set(datum) = prefs.edit().putString(LAATSTE_OCHTEND, datum).apply()

    fun zetWeekdoel(uren: Float) {
        val goed = uren.coerceIn(1f, 60f)
        prefs.edit().putFloat(WEEKDOEL, goed).apply()
        _weekdoel.value = goed
    }

    companion object {
        private const val WEEKDOEL = "weekdoel"
        private const val KIM_ADRES = "kim_adres"
        private const val LAATSTE_OCHTEND = "laatste_ochtendbericht"

        @Volatile private var enige: Instellingen? = null

        fun van(context: Context): Instellingen =
            enige ?: synchronized(this) { enige ?: Instellingen(context).also { enige = it } }
    }
}
