package nl.odelia.dashboard.logica

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Vaste instellingen. Later misschien aan te passen in de app. */
object Instellingen {
    /** Uren per week (ontwerp: "van 35 uur"). */
    const val WEEKDOEL = 35f
    val HERINNER_INKLOKKEN: LocalTime = LocalTime.of(7, 50)
    val HERINNER_UITKLOKKEN: LocalTime = LocalTime.of(15, 50)
    /** Ochtendmelding: zoveel minuten na inklokken. */
    const val OCHTEND_NA_MINUTEN = 20L
    /** Urenmail: zoveel minuten na uitklokken op vrijdag. */
    const val URENMAIL_NA_MINUTEN = 30L
    /** Seintje voor een deadline: de dag ervoor om deze tijd. */
    val SEINTJE_OM: LocalTime = LocalTime.of(16, 15)

    fun isWerkdag(datum: LocalDate) = datum.dayOfWeek != DayOfWeek.SATURDAY && datum.dayOfWeek != DayOfWeek.SUNDAY
}

/** Alles wat het hoofdscherm en de widget over klokken laten zien. */
data class KlokStaat(
    val nu: LocalDateTime,
    val isIn: Boolean,
    /** Begin van de periode die nu loopt. */
    val sinds: LocalTime?,
    val vandaag: Werkdag,
    /** Maandag t/m vrijdag. */
    val week: List<Werkdag>,
    val weekUren: Float,
    val weekdoel: Float,
    /** Begin van een periode van een eerdere dag die nog open staat. */
    val vergeten: LocalDateTime?,
) {
    companion object {
        fun maak(
            perioden: List<Periode>,
            soorten: Map<LocalDate, DagSoort>,
            nu: LocalDateTime,
            weekdoel: Float = Instellingen.WEEKDOEL,
        ): KlokStaat {
            val vandaag = nu.toLocalDate()
            val loopt = perioden.filter { it.eind == null && it.begin.toLocalDate() == vandaag }.maxByOrNull { it.begin }
            return KlokStaat(
                nu = nu,
                isIn = loopt != null,
                sinds = loopt?.begin?.toLocalTime(),
                vandaag = Uren.werkdag(perioden, soorten, vandaag, nu),
                week = Uren.werkweek(perioden, soorten, vandaag, nu),
                weekUren = Uren.weekTotaal(perioden, soorten, vandaag, nu),
                weekdoel = weekdoel,
                vergeten = Uren.vergeten(perioden, nu)?.begin,
            )
        }

        fun leeg(nu: LocalDateTime) = maak(emptyList(), emptyMap(), nu)
    }
}
