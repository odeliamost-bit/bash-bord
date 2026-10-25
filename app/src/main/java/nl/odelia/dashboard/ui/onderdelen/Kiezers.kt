package nl.odelia.dashboard.ui.onderdelen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import nl.odelia.dashboard.ui.theme.Kleur

/** Venstertje met een klok om een tijd te kiezen. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TijdKiezer(titel: String, begin: LocalTime, onKies: (LocalTime) -> Unit, onSluit: () -> Unit) {
    val keuze = rememberTimePickerState(begin.hour, begin.minute, is24Hour = true)
    Venster(titel, onSluit, onKlaar = { onKies(LocalTime.of(keuze.hour, keuze.minute)) }) {
        TimePicker(
            state = keuze,
            colors = TimePickerDefaults.colors(
                clockDialColor = Color(0xFFF5E8DA),
                selectorColor = Kleur.Koraal,
                timeSelectorSelectedContainerColor = Color(0xFFFAD8CF),
                timeSelectorSelectedContentColor = Color(0xFF5A2A1C),
                timeSelectorUnselectedContainerColor = Color(0xFFF5E8DA),
                timeSelectorUnselectedContentColor = Kleur.Inkt,
            ),
        )
    }
}

/** Venstertje met een kalender om een datum te kiezen. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatumKiezer(begin: LocalDate, onKies: (LocalDate) -> Unit, onSluit: () -> Unit) {
    // De kalender rekent in UTC-millis; middernacht UTC = die dag.
    val keuze = rememberDatePickerState(initialSelectedDateMillis = begin.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
    Venster(null, onSluit, onKlaar = {
        keuze.selectedDateMillis?.let { onKies(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) } ?: onSluit()
    }) {
        DatePicker(
            state = keuze,
            title = null,
            headline = null,
            showModeToggle = false,
            colors = DatePickerDefaults.colors(
                containerColor = Kleur.Kaart,
                selectedDayContainerColor = Kleur.Koraal,
                todayDateBorderColor = Kleur.Koraal,
                todayContentColor = Kleur.KoraalDiep,
            ),
        )
    }
}

/** Klei-kaart als venster, met "Terug" en een koraal knop. */
@Composable
fun Venster(
    titel: String?,
    onSluit: () -> Unit,
    onKlaar: () -> Unit,
    klaarTekst: String = "Kies",
    inhoud: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onSluit, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier
                .padding(16.dp)
                .klei(Kleur.Kaart, RoundedCornerShape(30.dp), Kleur.KaartSchaduw)
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (titel != null) Tekst(titel, 22.sp, kleur = Color(0xFF5A3426))
            inhoud()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ZachteKnop("Terug", onSluit, Modifier.weight(1f))
                GroteKnop(klaarTekst, onKlaar, Modifier.weight(1f), icoon = Icoontjes.vinkje)
            }
        }
    }
}
