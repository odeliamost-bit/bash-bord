package nl.odelia.dashboard

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDateTime
import kotlinx.coroutines.delay
import nl.odelia.dashboard.meldingen.Herinneringen
import nl.odelia.dashboard.meldingen.Meldingen
import nl.odelia.dashboard.ui.DashboardApp
import nl.odelia.dashboard.ui.theme.DashboardThema
import nl.odelia.dashboard.ui.theme.Kleur

class MainActivity : ComponentActivity() {
    private lateinit var voorlezer: Voorlezer
    private val model: DashboardModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Achtergrond loopt door achter de statusbalk; donkere icoontjes op de lichte kleur.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        voorlezer = Voorlezer(this)
        Meldingen.maakKanalen(this)
        Herinneringen.plan(this)
        vraagMeldingenToestemming()
        setContent {
            DashboardThema {
                val gegevens by model.klokGegevens.collectAsStateWithLifecycle()
                val weekdoel by model.weekdoel.collectAsStateWithLifecycle()
                val nu by klokTik()
                val klok = gegevens?.staat(nu, weekdoel)
                if (klok == null) {
                    // Database laadt nog (duurt heel kort).
                    Box(Modifier.fillMaxSize().background(Kleur.Achtergrond))
                } else {
                    DashboardApp(klok = klok, klokActies = model.klokActies, onVoorlezen = voorlezer::spreek)
                }
            }
        }
    }

    /** Android 13+: één keer vragen of de app meldingen mag tonen (voor de herinneringen). */
    private fun vraagMeldingenToestemming() {
        if (Build.VERSION.SDK_INT >= 33 && !Meldingen.magTonen(this)) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
                .launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onDestroy() {
        voorlezer.stop()
        super.onDestroy()
    }
}

/** De tijd van nu, elke 30 seconden bijgewerkt (voor "al 5½ uur gewerkt"). */
@Composable
private fun klokTik() = produceState(LocalDateTime.now()) {
    while (true) {
        delay(30_000)
        value = LocalDateTime.now()
    }
}
