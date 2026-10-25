package nl.odelia.dashboard.logica

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/** Iets om te onthouden. Alleen [tekst] is verplicht; de rest is vrij. */
data class Onthoud(
    val id: String = UUID.randomUUID().toString(),
    val tekst: String,
    val wie: String? = null,
    val wat: String? = null,
    val waar: String? = null,
    val wanneer: LocalDate? = null,
    /** Organisatie of project: "Werk", "School", "PVB" … */
    val groep: String? = null,
    val seintje: Boolean = true,
    val klaar: Boolean = false,
    val gemaakt: LocalDateTime = LocalDateTime.now(),
    /** Bestandsnaam van een foto (alleen op de telefoon, in de map "fotos"). */
    val foto: String? = null,
) {
    /** Korte titel: wat er moet gebeuren. */
    val titel: String get() = tekst.ifBlank { listOfNotNull(wat, wie).joinToString(" ").ifBlank { if (foto != null) "📷 Foto" else "" } }
}
