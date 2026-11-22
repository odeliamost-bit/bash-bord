package nl.odelia.dashboard.logica

/**
 * Privacy-overzicht: per onderdeel waar de gegevens staan.
 * 📱 = alleen op dit apparaat. 🔒 = ook online, versleuteld, alleen jij kunt het lezen.
 */
object Privacy {
    data class Regel(val icoon: String, val naam: String, val online: Boolean, val uitleg: String = "")

    fun regels(): List<Regel> = listOf(
        Regel("💶", "Geld", "geld" in SyncRegels.ONLINE),
        Regel("🧠", "Onthoud + Mensen", "onthoud" in SyncRegels.ONLINE),
        Regel("🎓", "Leren", "leren" in SyncRegels.ONLINE),
        Regel("🎁", "Wensen", "wensen" in SyncRegels.ONLINE),
        Regel("🍲", "Recepten", "recepten" in SyncRegels.ONLINE),
        Regel("⚙️", "Instellingen", "instellingen" in SyncRegels.ONLINE, "zonder grote tekst en tegels"),
        Regel("💡", "Advies (⭐)", "advies" in SyncRegels.ONLINE),
        Regel("⛵", "Zeilweer (plek)", "weer" in SyncRegels.ONLINE),
        Regel("✉️", "Mail-hulp", false, "bewaart niets"),
        Regel("🏠", "Thuis", false, "tegels blijven per apparaat"),
        Regel("⏱️", "Uren + uurloon", "uren" in SyncRegels.ONLINE),
        Regel("🌸", "Cyclus", "cyclus" in SyncRegels.ONLINE),
        Regel("📈", "Beleggen", false),
        Regel("🏦", "ING-meldingen", false, "alleen de boeking gaat mee met Geld"),
        Regel("📸", "Foto's", false, "nog niet in de sync"),
    )
}
