package nl.odelia.dashboard

import android.content.Context
import androidx.glance.appwidget.updateAll
import nl.odelia.dashboard.data.Opslag
import nl.odelia.dashboard.meldingen.Planner
import nl.odelia.dashboard.online.UrenNaarSheet
import nl.odelia.dashboard.widget.KlokWidget
import nl.odelia.dashboard.widget.OnthoudWidget

/** Dingen die vanuit de app, de widget én de meldingen kunnen gebeuren. */
object Acties {
    /** In of uit klokken. Geeft `true` als je nu ingeklokt bent. */
    suspend fun wisselKlok(context: Context): Boolean {
        val isIn = Opslag.van(context).wisselKlok()
        naWijziging(context)
        return isIn
    }

    /** Na elke wijziging: widgets bijwerken, de wekkers opnieuw zetten en afgeronde dagen naar de spreadsheet. */
    suspend fun naWijziging(context: Context) {
        KlokWidget().updateAll(context)
        OnthoudWidget().updateAll(context)
        Planner.plan(context)
        UrenNaarSheet.probeer(context)
    }
}
