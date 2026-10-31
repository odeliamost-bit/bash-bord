package nl.odelia.dashboard.logica

import java.time.LocalTime
import org.json.JSONArray
import org.json.JSONObject

/** De tegels onderaan het hoofdscherm. `breed` = een hele rij. */
enum class TegelSoort(val naam: String, val breed: Boolean = false) {
    Advies("Advies", breed = true),
    Mail("Mail-hulp"),
    Mensen("Mensen"),
    Week("Week"),
    Wensen("Wensen"),
    Boeken("Boeken"),
    Simulator("Simulator"),
    Leren("Leren", breed = true),
}

/**
 * Wat Odelia zelf kan instellen. Staat in instellingen.json op het apparaat.
 * Gaat niet online (wel mee in de back-up).
 */
data class InstellingenData(
    val groteTekst: Boolean = false,
    val inklokken: LocalTime = Instellingen.HERINNER_INKLOKKEN,
    val uitklokken: LocalTime = Instellingen.HERINNER_UITKLOKKEN,
    val seintjeOm: LocalTime = Instellingen.SEINTJE_OM,
    val weekdoel: Float = Instellingen.WEEKDOEL,
    val klokHerinnering: Boolean = true,
    val ochtendMelding: Boolean = true,
    val urenmailSeintje: Boolean = true,
    val deadlineSeintje: Boolean = true,
    /** Afgeronde werkdagen naar de Google-spreadsheet (alleen als dat is ingesteld, zie README). */
    val urenNaarSheet: Boolean = true,
    /** Zichtbare tegels, in deze volgorde. Wat er niet in staat, is verborgen. */
    val tegels: List<TegelSoort> = TegelSoort.entries,
    /** Namen van menu-plekken die verborgen zijn (bijv. "Zeilweer"). */
    val verborgenMenu: Set<String> = emptySet(),
) {
    fun wisselTegel(t: TegelSoort) = copy(tegels = if (t in tegels) tegels - t else tegels + t)

    /** Tegel één plek omhoog (-1) of omlaag (+1). */
    fun schuifTegel(t: TegelSoort, richting: Int): InstellingenData {
        val i = tegels.indexOf(t)
        val j = i + richting
        if (i < 0 || j !in tegels.indices) return this
        return copy(tegels = tegels.toMutableList().apply { this[i] = this[j]; this[j] = t })
    }

    fun wisselMenu(naam: String) = copy(verborgenMenu = if (naam in verborgenMenu) verborgenMenu - naam else verborgenMenu + naam)

    companion object {
        const val MIN_WEEKDOEL = 1f
        const val MAX_WEEKDOEL = 60f
        /** Stapjes van 5 minuten bij de tijden. */
        const val TIJD_STAP = 5L

        fun tijdStap(t: LocalTime, richting: Int): LocalTime = t.plusMinutes(TIJD_STAP * richting)

        fun weekdoelStap(w: Float, richting: Int): Float = (w + richting).coerceIn(MIN_WEEKDOEL, MAX_WEEKDOEL)
    }
}

/** Instellingen naar tekst (JSON) en terug. Kapot of onbekend? Dan de gewone waarde. */
object InstellingenJson {
    private const val VERSIE = 1

    fun schrijf(d: InstellingenData): String = JSONObject()
        .put("versie", VERSIE)
        .put("groteTekst", d.groteTekst)
        .put("inklokken", d.inklokken.toString())
        .put("uitklokken", d.uitklokken.toString())
        .put("seintjeOm", d.seintjeOm.toString())
        .put("weekdoel", d.weekdoel.toDouble())
        .put("klokHerinnering", d.klokHerinnering)
        .put("ochtendMelding", d.ochtendMelding)
        .put("urenmailSeintje", d.urenmailSeintje)
        .put("deadlineSeintje", d.deadlineSeintje)
        .put("urenNaarSheet", d.urenNaarSheet)
        .put("tegels", JSONArray(d.tegels.map { it.name }))
        .put("verborgenMenu", JSONArray(d.verborgenMenu.sorted()))
        .toString()

    /**
     * Voor de sync: alles behalve grote tekst en tegels.
     * Die twee blijven per apparaat (gekozen door Odelia: tablet andere tegels dan telefoon).
     */
    fun schrijfGedeeld(d: InstellingenData): String =
        JSONObject(schrijf(d)).apply {
            remove("groteTekst")
            remove("tegels")
        }.toString()

    /** Instellingen van een ander apparaat overnemen, maar grote tekst en tegels van dit apparaat houden. */
    fun neemGedeeld(hier: InstellingenData, tekst: String): InstellingenData {
        JSONObject(tekst) // kapot? dan een fout, en verandert er niks
        return lees(tekst).copy(groteTekst = hier.groteTekst, tegels = hier.tegels)
    }

    fun lees(tekst: String): InstellingenData {
        val x = try {
            JSONObject(tekst)
        } catch (_: Exception) {
            return InstellingenData()
        }
        val s = InstellingenData()
        fun tijd(naam: String, standaard: LocalTime) = try {
            LocalTime.parse(x.getString(naam))
        } catch (_: Exception) {
            standaard
        }
        val tegels = x.optJSONArray("tegels")?.let { a ->
            (0 until a.length()).mapNotNull { i -> TegelSoort.entries.firstOrNull { it.name == a.optString(i) } }.distinct()
        } ?: s.tegels
        val menu = x.optJSONArray("verborgenMenu")?.let { a -> (0 until a.length()).map { a.optString(it) }.filter { it.isNotEmpty() }.toSet() }
            ?: s.verborgenMenu
        return InstellingenData(
            groteTekst = x.optBoolean("groteTekst", s.groteTekst),
            inklokken = tijd("inklokken", s.inklokken),
            uitklokken = tijd("uitklokken", s.uitklokken),
            seintjeOm = tijd("seintjeOm", s.seintjeOm),
            weekdoel = x.optDouble("weekdoel", s.weekdoel.toDouble()).toFloat()
                .takeIf { !it.isNaN() }?.coerceIn(InstellingenData.MIN_WEEKDOEL, InstellingenData.MAX_WEEKDOEL) ?: s.weekdoel,
            klokHerinnering = x.optBoolean("klokHerinnering", s.klokHerinnering),
            ochtendMelding = x.optBoolean("ochtendMelding", s.ochtendMelding),
            urenmailSeintje = x.optBoolean("urenmailSeintje", s.urenmailSeintje),
            deadlineSeintje = x.optBoolean("deadlineSeintje", s.deadlineSeintje),
            urenNaarSheet = x.optBoolean("urenNaarSheet", s.urenNaarSheet),
            tegels = tegels,
            verborgenMenu = menu,
        )
    }
}
