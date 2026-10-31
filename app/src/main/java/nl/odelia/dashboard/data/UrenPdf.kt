package nl.odelia.dashboard.data

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import nl.odelia.dashboard.logica.PAUZE_MINUTEN
import nl.odelia.dashboard.logica.UrenPeriode
import nl.odelia.dashboard.logica.UrenRij
import nl.odelia.dashboard.logica.Urenmail
import nl.odelia.dashboard.logica.Werkdag

/**
 * Maakt de urentabel als PDF (bijlage voor Kim). Alles gebeurt op de telefoon, zonder internet.
 * Het bestand staat in de cache-map van de app en wordt bij de volgende mail weer opgeruimd.
 */
object UrenPdf {
    private const val BREED = 595 // A4 in punten
    private const val HOOG = 842
    private const val RAND = 48f
    private const val RIJ = 24f
    private val KOLOMMEN = floatArrayOf(140f, 85f, 85f, 85f, 104f) // samen 499 = BREED - 2 × RAND

    fun maak(map: File, periode: UrenPeriode, dagen: List<Werkdag>): File {
        val uit = File(map, "urenmail").apply { mkdirs() }
        uit.listFiles()?.forEach { it.delete() } // oude bijlagen weg
        val bestand = File(uit, Urenmail.bestandsnaam(periode))

        val tekst = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(0x3A, 0x2A, 0x24); textSize = 11.5f }
        val dik = Paint(tekst).apply { typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD) }
        val titel = Paint(dik).apply { textSize = 20f }
        val grijs = Paint(tekst).apply { color = Color.rgb(0x7A, 0x66, 0x5C); textSize = 10.5f }
        val lijn = Paint().apply { color = Color.rgb(0xD8, 0xC8, 0xBC); strokeWidth = 0.8f }
        val kop = Paint().apply { color = Color.rgb(0xFA, 0xD8, 0xCF) }
        val streep = Paint().apply { color = Color.rgb(0xFB, 0xF3, 0xEA) }

        val rijen = dagen.map(Urenmail::rij)
        val pdf = PdfDocument()
        try {
            var nr = 0
            var i = 0
            do {
                nr++
                val pagina = pdf.startPage(PdfDocument.PageInfo.Builder(BREED, HOOG, nr).create())
                val c = pagina.canvas
                var y = RAND + 20f
                if (nr == 1) {
                    c.drawText("Urenregistratie Odelia van der Most", RAND, y, titel)
                    y += 22f
                    c.drawText(periode.naam.replaceFirstChar { it.uppercase() }, RAND, y, tekst)
                    y += 26f
                }
                y = rij(c, y, UrenRij(Urenmail.KOPJES[0], Urenmail.KOPJES[1], Urenmail.KOPJES[2], Urenmail.KOPJES[3], Urenmail.KOPJES[4]), dik, lijn, kop)
                var even = false
                while (i < rijen.size && y + RIJ * 3 < HOOG - RAND) {
                    y = rij(c, y, rijen[i], tekst, lijn, if (even) streep else null)
                    even = !even
                    i++
                }
                if (i == rijen.size) {
                    y = rij(c, y, UrenRij("Totaal", "", "", "", Urenmail.decimaal(Urenmail.totaal(dagen))), dik, lijn, kop)
                    y += 22f
                    c.drawText("Uren in decimalen (7,50 = 7½ uur). Per gewerkte dag gaat er $PAUZE_MINUTEN min pauze af.", RAND, y, grijs)
                }
                pdf.finishPage(pagina)
            } while (i < rijen.size)
            bestand.outputStream().use { pdf.writeTo(it) }
        } finally {
            pdf.close()
        }
        return bestand
    }

    /** Eén rij met lijntjes eromheen. Bij vrij/ziek loopt het woord over de lege vakjes heen. */
    private fun rij(c: Canvas, y: Float, r: UrenRij, verf: Paint, lijn: Paint, vlak: Paint?): Float {
        val onder = y + RIJ
        if (vlak != null) c.drawRect(RAND, y, BREED - RAND, onder, vlak)
        c.drawLine(RAND, y, BREED - RAND, y, lijn)
        c.drawLine(RAND, onder, BREED - RAND, onder, lijn)
        val vakjes = listOf(r.datum, r.begin, r.eind, r.pauze, r.totaal)
        var x = RAND
        c.drawLine(x, y, x, onder, lijn)
        vakjes.forEachIndexed { k, v ->
            c.drawText(v, x + 8f, y + 16f, verf)
            x += KOLOMMEN[k]
            // Geen streep tussen "vrij" en de lege vakjes erna.
            val doorlopen = k in 1..3 && vakjes[k + 1].isEmpty()
            if (!doorlopen) c.drawLine(x, y, x, onder, lijn)
        }
        return onder
    }
}
