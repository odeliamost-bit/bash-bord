package nl.odelia.dashboard.onthoud

/**
 * Waarmee het scherm "Onthoud dit" opent: tekst die gedeeld of geplakt is,
 * een persoon die al gekozen is, of meteen inspreken (🎤 van de widget).
 */
data class Concept(
    val tekst: String = "",
    val bron: Bron = Bron.Getypt,
    val persoonId: String? = null,
    val inspreken: Boolean = false,
)
