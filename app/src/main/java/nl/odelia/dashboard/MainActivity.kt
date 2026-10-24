package nl.odelia.dashboard

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import nl.odelia.dashboard.ui.DashboardApp
import nl.odelia.dashboard.ui.theme.DashboardThema

class MainActivity : ComponentActivity() {
    private lateinit var voorlezer: Voorlezer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Achtergrond loopt door achter de statusbalk; donkere icoontjes op de lichte kleur.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        voorlezer = Voorlezer(this)
        setContent {
            DashboardThema {
                DashboardApp(onVoorlezen = voorlezer::spreek)
            }
        }
    }

    override fun onDestroy() {
        voorlezer.stop()
        super.onDestroy()
    }
}
