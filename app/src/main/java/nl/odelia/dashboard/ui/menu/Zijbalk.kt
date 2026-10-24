package nl.odelia.dashboard.ui.menu

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.odelia.dashboard.ui.Plek
import nl.odelia.dashboard.ui.onderdelen.Icoon
import nl.odelia.dashboard.ui.onderdelen.Icoontjes
import nl.odelia.dashboard.ui.onderdelen.Tekst
import nl.odelia.dashboard.ui.onderdelen.Vorm
import nl.odelia.dashboard.ui.onderdelen.glans
import nl.odelia.dashboard.ui.onderdelen.hoekVerloop
import nl.odelia.dashboard.ui.onderdelen.klei
import nl.odelia.dashboard.ui.theme.Kleur

/** Breedte van de dichte zijbalk plus de ruimte ernaast. De inhoud begint hierna. */
val ZijbalkRuimte = 88.dp

/**
 * Menu aan de linkerkant (ontwerp/Navigatie.dc.html).
 * Dicht: alleen icoontjes. Tik op een icoontje of de pijl = menu open.
 * Open: icoontjes met naam. Tik op een naam = daarheen.
 */
@Composable
fun Zijbalk(
    open: Boolean,
    gekozen: Plek,
    onWissel: () -> Unit,
    onKies: (Plek) -> Unit,
    modifier: Modifier = Modifier,
) {
    val breedte by animateDpAsState(if (open) 250.dp else 76.dp, label = "zijbalk")
    val vorm = RoundedCornerShape(30.dp)
    Column(
        modifier
            .fillMaxHeight()
            .width(breedte)
            .klei(Color(0xFFDDE9D7), vorm, schaduw = Color(0xFF5A8264), hoogte = 14.dp)
            .clip(vorm)
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val knopVorm = RoundedCornerShape(20.dp)
        Row(
            Modifier
                .padding(bottom = 6.dp)
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .shadow(6.dp, knopVorm, ambientColor = Kleur.Koraal, spotColor = Kleur.Koraal)
                .clip(knopVorm)
                .background(hoekVerloop(Kleur.KoraalLicht, Color(0xFFE07A62)))
                .glans(knopVorm, licht = 0.45f, donker = 0.1f)
                .clickable(onClick = onWissel)
                .semantics { contentDescription = if (open) "Menu sluiten" else "Menu openen" }
                .padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icoon(if (open) Icoontjes.pijlLinks else Icoontjes.pijlRechts, 28.dp)
            if (open) Tekst("Menu", 18.sp, kleur = Color.White, eenRegel = true)
        }

        Plek.entries.forEach { plek ->
            MenuRegel(plek, aan = plek == gekozen, open = open, onClick = { onKies(plek) })
        }
    }
}

@Composable
private fun MenuRegel(plek: Plek, aan: Boolean, open: Boolean, onClick: () -> Unit) {
    val vorm = RoundedCornerShape(20.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .clip(vorm)
            .background(if (aan) Kleur.Kaart else Color.Transparent)
            .selectable(selected = aan, onClick = onClick, role = Role.Tab)
            .semantics { if (!open) contentDescription = plek.label }
            .padding(start = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(46.dp), contentAlignment = Alignment.Center) {
            MenuIcoon(plek, 34.dp)
        }
        if (open) Tekst(plek.label, 18.sp, kleur = Kleur.Inkt, eenRegel = true)
    }
}

/** Gekleurd 3D-icoontje zonder blokje eromheen, zoals in het menu-ontwerp. */
@Composable
fun MenuIcoon(plek: Plek, maat: Dp, modifier: Modifier = Modifier) {
    val vormen = remember(plek) { menuVormen(plek) }
    Icoon(vormen, maat, modifier, schaduw = menuKleuren(plek).second.copy(alpha = 0.45f))
}

/** Lichte en donkere kleur per plek (de "G"-lijst uit het ontwerp). */
private fun menuKleuren(plek: Plek): Pair<Color, Color> = when (plek) {
    Plek.Thuis -> Color(0xFFC9B6F2) to Color(0xFF9C82DA)
    Plek.Werk -> Color(0xFFA8D6B4) to Color(0xFF6FAE84)
    Plek.Onthoud -> Color(0xFFF6B3BF) to Color(0xFFE58497)
    Plek.Mensen -> Color(0xFFAFCBEA) to Color(0xFF7FA5D2)
    Plek.Mail -> Color(0xFFD2BDF0) to Color(0xFFA88AD8)
    Plek.Advies -> Color(0xFFF8C29C) to Color(0xFFE9956A)
    Plek.Leren -> Color(0xFFF7DA8A) to Color(0xFFE5B54E)
    Plek.Cyclus -> Color(0xFFF4B0C8) to Color(0xFFDE7FA1)
    Plek.Geld -> Color(0xFFB9DDB0) to Color(0xFF82B874)
    Plek.Wensen -> Color(0xFFF7B7AE) to Color(0xFFE8877A)
    Plek.Wachtwoorden -> Color(0xFF9AD1D4) to Color(0xFF5FA8AD)
}

private fun menuPad(plek: Plek): String = when (plek) {
    Plek.Thuis -> "M3 11l9-7 9 7M5 10v10h14V10"
    Plek.Werk -> "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18zM12 7v5l3 2"
    Plek.Onthoud -> "M6 3h12v18l-6-4-6 4z"
    Plek.Mensen -> "M9 4.5a3.5 3.5 0 1 0 0 7 3.5 3.5 0 0 0 0-7zM2.5 20a6.5 6.5 0 0 1 13 0M17 6.5a2.5 2.5 0 1 1 0 5M18 14a5 5 0 0 1 3.5 5"
    Plek.Mail -> "M3 7a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2zM3.5 7l8.5 6 8.5-6"
    Plek.Advies -> "M14.7 6.3a4 4 0 0 0 5 5l-8.9 8.9a2.1 2.1 0 0 1-3-3zM14.7 6.3l3-3a4 4 0 0 1 2 5"
    Plek.Leren -> "M4 5a2 2 0 0 1 2-2h13v16H6a2 2 0 0 0-2 2zM4 21a2 2 0 0 1 2-2h13v2"
    Plek.Cyclus -> "M12 3s6 6.5 6 11a6 6 0 0 1-12 0c0-4.5 6-11 6-11z"
    Plek.Geld -> "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18zM15 9.5c-.5-1-1.6-1.5-3-1.5-1.7 0-3 .9-3 2s1.3 1.6 3 2 3 1 3 2-1.3 2-3 2c-1.4 0-2.5-.5-3-1.5M12 6v12"
    Plek.Wensen -> "M20.8 5.6a5.5 5.5 0 0 0-7.8 0L12 6.7l-1-1.1a5.5 5.5 0 0 0-7.8 7.8L12 22l8.8-8.6a5.5 5.5 0 0 0 0-7.8z"
    Plek.Wachtwoorden -> "M6 11h12v10H6zM8.5 11V7.5a3.5 3.5 0 0 1 7 0V11"
}

private fun menuVormen(plek: Plek): List<Vorm> {
    val (licht, donker) = menuKleuren(plek)
    // Bij Geld is het icoontje niet gevuld (anders zie je het muntteken niet).
    val vul = if (plek == Plek.Geld) null else licht
    return listOf(Vorm(menuPad(plek), vul = vul, lijn = donker, dikte = 2.3f))
}
