package nl.odelia.dashboard.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import java.time.LocalDateTime
import nl.odelia.dashboard.ui.theme.DashboardThema
import nl.odelia.dashboard.ui.thuis.Tab

// Voorbeelden voor Android Studio (knop "Split" of "Design" rechtsboven in dit bestand).

private val vrijdagOchtend = LocalDateTime.of(2026, 10, 23, 8, 15)

@Preview(name = "Vandaag", widthDp = 411, heightDp = 1500)
@Composable
private fun VandaagVoorbeeld() {
    DashboardThema { DashboardApp(onVoorlezen = { true }, nu = vrijdagOchtend) }
}

@Preview(name = "Werk", widthDp = 411, heightDp = 1500)
@Composable
private fun WerkVoorbeeld() {
    DashboardThema { DashboardApp(onVoorlezen = { true }, nu = vrijdagOchtend, beginTab = Tab.Werk) }
}

@Preview(name = "Menu open", widthDp = 411, heightDp = 900)
@Composable
private fun MenuVoorbeeld() {
    DashboardThema { DashboardApp(onVoorlezen = { true }, nu = vrijdagOchtend, beginMenuOpen = true) }
}
