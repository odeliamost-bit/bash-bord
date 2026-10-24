package nl.odelia.dashboard.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import java.time.LocalDateTime
import nl.odelia.dashboard.onthoud.OnthoudActies
import nl.odelia.dashboard.ui.theme.DashboardThema
import nl.odelia.dashboard.ui.thuis.Tab
import nl.odelia.dashboard.ui.thuis.Voorbeeld
import nl.odelia.dashboard.uren.KlokActies

// Voorbeelden voor Android Studio (knop "Split" of "Design" rechtsboven in dit bestand).

private val vrijdag = LocalDateTime.of(2026, 10, 23, 14, 0)

@Preview(name = "Vandaag", widthDp = 411, heightDp = 1500)
@Composable
private fun VandaagVoorbeeld() {
    DashboardThema { DashboardApp(Voorbeeld.klok(vrijdag), KlokActies.niets, Voorbeeld.onthoud(vrijdag), OnthoudActies.niets, onVoorlezen = { true }) }
}

@Preview(name = "Werk", widthDp = 411, heightDp = 1500)
@Composable
private fun WerkVoorbeeld() {
    DashboardThema { DashboardApp(Voorbeeld.klok(vrijdag), KlokActies.niets, Voorbeeld.onthoud(vrijdag), OnthoudActies.niets, onVoorlezen = { true }, beginTab = Tab.Werk) }
}

@Preview(name = "Dag aanpassen", widthDp = 411, heightDp = 1200)
@Composable
private fun DagVoorbeeld() {
    DashboardThema {
        DashboardApp(Voorbeeld.klok(vrijdag), KlokActies.niets, Voorbeeld.onthoud(vrijdag), OnthoudActies.niets, onVoorlezen = { true }, beginScherm = "dag/${vrijdag.toLocalDate().minusDays(1)}")
    }
}

@Preview(name = "Onthoud dit", widthDp = 411, heightDp = 1400)
@Composable
private fun ToevoegenVoorbeeld() {
    DashboardThema {
        DashboardApp(Voorbeeld.klok(vrijdag), KlokActies.niets, Voorbeeld.onthoud(vrijdag), OnthoudActies.niets, onVoorlezen = { true }, beginScherm = "nieuw")
    }
}

@Preview(name = "Menu open", widthDp = 411, heightDp = 900)
@Composable
private fun MenuVoorbeeld() {
    DashboardThema { DashboardApp(Voorbeeld.klok(vrijdag), KlokActies.niets, Voorbeeld.onthoud(vrijdag), OnthoudActies.niets, onVoorlezen = { true }, beginMenuOpen = true) }
}
