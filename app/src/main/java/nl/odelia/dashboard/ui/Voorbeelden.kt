package nl.odelia.dashboard.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import nl.odelia.dashboard.ui.theme.DashboardThema
import nl.odelia.dashboard.ui.thuis.Tab

// Voorbeelden voor Android Studio (knop "Split" of "Design" rechtsboven in dit bestand).

private val geenActies = object : AppActies {}

@Preview(name = "Vandaag", widthDp = 411, heightDp = 1500)
@Composable
private fun VandaagVoorbeeld() {
    DashboardThema { DashboardApp(Voorbeeld.staat(), geenActies) }
}

@Preview(name = "Werk", widthDp = 411, heightDp = 1800)
@Composable
private fun WerkVoorbeeld() {
    DashboardThema { DashboardApp(Voorbeeld.staat(), geenActies, beginTab = Tab.Werk) }
}

@Preview(name = "Onthoud dit", widthDp = 411, heightDp = 1300)
@Composable
private fun ToevoegenVoorbeeld() {
    DashboardThema { DashboardApp(Voorbeeld.staat(), geenActies, beginScherm = Scherm.Toevoegen()) }
}

@Preview(name = "Menu open", widthDp = 411, heightDp = 900)
@Composable
private fun MenuVoorbeeld() {
    DashboardThema { DashboardApp(Voorbeeld.staat(), geenActies, beginMenuOpen = true) }
}
