package nl.odelia.dashboard.logica

import java.time.Instant

/**
 * Regels voor de sync tussen apparaten (telefoon, tablet, laptop).
 * Los van internet, zodat ze te testen zijn. Het online-deel staat in `online/Sync.kt`.
 *
 * Per onderdeel (bijv. "geld") staat online één blok tekst. Bij een botsing wint de nieuwste wijziging.
 */
object SyncRegels {
    /**
     * Alleen deze onderdelen mogen online (gekozen door Odelia, okt 2026).
     * Instellingen: zonder grote tekst en tegels (die blijven per apparaat).
     * Uren, cyclus, mail-hulp, advies, weer en thuis gaan NOOIT online.
     * Supabase controleert dezelfde lijst (zie `supabase/kompas.sql`).
     */
    val ONLINE = setOf("geld", "onthoud", "leren", "wensen", "recepten", "mensen", "fotos", "instellingen")

    /** Wat dit apparaat weet over één onderdeel. */
    data class Stand(
        /** Tijd (van de server) van de online versie die hier staat. `null` = nog nooit gesynct. */
        val versie: Instant? = null,
        /** Hier iets veranderd dat nog niet online staat? */
        val vies: Boolean = false,
        /** Wanneer hier voor het laatst iets veranderde. */
        val gewijzigd: Instant? = null,
    )

    enum class Stap { Niks, Verstuur, Neem }

    /** Wat te doen. [online] = tijd van de online versie, of `null` als er online nog niks staat. */
    fun kies(stand: Stand, online: Instant?): Stap = when {
        online == null -> Stap.Verstuur
        // Eerste keer op dit apparaat: wat online staat wint. (Dus: eerst op de telefoon inloggen.)
        stand.versie == null -> Stap.Neem
        online > stand.versie ->
            if (stand.vies && stand.gewijzigd != null && stand.gewijzigd > online) Stap.Verstuur else Stap.Neem
        stand.vies -> Stap.Verstuur
        else -> Stap.Niks
    }
}
