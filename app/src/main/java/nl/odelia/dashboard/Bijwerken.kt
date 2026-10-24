package nl.odelia.dashboard

import android.content.Context
import java.time.LocalDateTime
import nl.odelia.dashboard.data.Instellingen
import nl.odelia.dashboard.data.KlokOpslag
import nl.odelia.dashboard.meldingen.Herinneringen
import nl.odelia.dashboard.widget.KlokWidget

/** Na elke wijziging in de gegevens: alles wat buiten de app zichtbaar is bijwerken. */
object Bijwerken {
    suspend fun alles(context: Context) {
        KlokWidget.ververs(context)
        val staat = KlokOpslag(context).nu().staat(LocalDateTime.now(), Instellingen.van(context).weekdoel.value)
        Herinneringen.ruimOp(context, staat)
    }
}
