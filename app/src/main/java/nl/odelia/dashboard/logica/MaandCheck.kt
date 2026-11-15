package nl.odelia.dashboard.logica

import java.time.YearMonth
import org.json.JSONObject

/**
 * Uurloon, door Odelia zelf ingevuld. Staat in een eigen bestandje (`uurloon.json`) op dit apparaat.
 * Hoort NIET bij Geld of Instellingen, want die gaan mee met de sync. Uren en uurloon gaan nooit online.
 */
data class UurloonData(val cent: Long? = null)

object UurloonJson {
    fun schrijf(d: UurloonData): String = JSONObject().apply { d.cent?.let { put("cent", it) } }.toString()

    fun lees(tekst: String): UurloonData = try {
        val x = JSONObject(tekst)
        UurloonData(if (x.has("cent")) x.getLong("cent").takeIf { it > 0 } else null)
    } catch (_: Exception) {
        UurloonData()
    }
}

/** Maandcheck bij Geld (nov 2026): uren × uurloon naast het loon dat die maand in Geld is geboekt. */
object MaandCheck {
    /** Zoveel procent verschil noemen we nog "klopt ongeveer". */
    const val RUIMTE_PROCENT = 5

    enum class Oordeel { GeenUurloon, GeenUren, GeenLoon, Klopt, Minder, Meer }

    data class Uitkomst(
        val maand: YearMonth,
        val uren: Float,
        val dagen: Int,
        /** Uren × uurloon, of `null` als er geen uurloon is ingevuld. */
        val verwachtCent: Long?,
        /** Loon dat deze maand in Geld is geboekt (inkomen met categorie "loon"). */
        val loonCent: Long,
        /** Geboekt loon min verwacht. Min = minder gekregen dan uren × uurloon. */
        val verschilCent: Long?,
        val oordeel: Oordeel,
    )

    /** Al het inkomen "loon" van [maand]. Overboeken tussen potjes telt niet mee. */
    fun loon(boekingen: List<Boeking>, maand: YearMonth): Long = boekingen
        .filter { it.soort == BoekSoort.In && it.categorie == "loon" && YearMonth.from(it.datum) == maand }
        .sumOf { it.cent }

    fun verwacht(uren: Float, uurloonCent: Long): Long = Math.round(uren.toDouble() * uurloonCent)

    fun van(dagen: List<Werkdag>, boekingen: List<Boeking>, maand: YearMonth, uurloonCent: Long?): Uitkomst {
        val som = UrenStats.maand(dagen, maand)
        val loon = loon(boekingen, maand)
        val verwacht = uurloonCent?.takeIf { it > 0 }?.let { verwacht(som.uren, it) }
        val verschil = verwacht?.let { loon - it }
        val oordeel = when {
            verwacht == null -> Oordeel.GeenUurloon
            som.dagen == 0 -> Oordeel.GeenUren
            loon == 0L -> Oordeel.GeenLoon
            kotlin.math.abs(verschil!!) * 100 <= verwacht * RUIMTE_PROCENT -> Oordeel.Klopt
            verschil < 0 -> Oordeel.Minder
            else -> Oordeel.Meer
        }
        return Uitkomst(maand, som.uren, som.dagen, verwacht, loon, verschil, oordeel)
    }

    /** Eén korte zin bij de uitkomst. */
    fun zin(u: Uitkomst): String = when (u.oordeel) {
        Oordeel.GeenUurloon -> "Vul je uurloon in. Dan reken ik het voor je uit."
        Oordeel.GeenUren -> "Deze maand staan er geen uren."
        Oordeel.GeenLoon -> "Er is deze maand nog geen loon geboekt bij Geld."
        Oordeel.Klopt -> "✅ Dat klopt ongeveer."
        Oordeel.Minder -> "⚠️ Je kreeg ${Geld.bedrag(-u.verschilCent!!)} minder dan uren × uurloon."
        Oordeel.Meer -> "Je kreeg ${Geld.bedrag(u.verschilCent!!)} meer dan uren × uurloon."
    }
}
