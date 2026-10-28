package nl.odelia.dashboard.data

import android.content.Context
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Back-up van alles in de app, als één .zip-bestand dat Odelia zelf bewaart
 * (bijv. in Downloads of Google Drive). Gaat niet vanzelf online.
 *
 * Erin: de database (uren, onthoud), alle JSON-bestandjes (geld, wensen, leren, …),
 * foto's, instellingen en cyclus. Niet erin: de sync-inlog (die blijft op het apparaat).
 */
object Backup {
    private const val MERK = "bash-bord-backup.txt"
    private const val DB = "dashboard.db"
    private const val MAX_BESTAND = 50L * 1024 * 1024
    private const val MAX_TOTAAL = 500L * 1024 * 1024

    /** De mappen in de zip, met waar ze op de telefoon staan. */
    private fun mappen(context: Context): Map<String, File> = mapOf(
        "files" to context.filesDir,
        "shared_prefs" to File(context.applicationInfo.dataDir, "shared_prefs"),
    )

    /** Losse bestanden uit `noBackupFilesDir` die wél mee mogen. */
    private val losNoBackup = listOf("cyclus.json")

    /** Schrijf de back-up naar [uit]. Geeft het aantal bestanden. */
    fun maak(context: Context, uit: OutputStream): Int {
        // Alles uit het logboek (WAL) in het hoofdbestand van de database zetten.
        try {
            AppDatabase.van(context).openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
        } catch (_: Exception) {
        }
        var aantal = 0
        ZipOutputStream(uit.buffered()).use { zip ->
            zip.putNextEntry(ZipEntry(MERK))
            zip.write("bash-bord back-up, versie 1\n".toByteArray())
            zip.closeEntry()

            mappen(context).forEach { (naam, map) ->
                map.walkTopDown().filter { it.isFile }.forEach { f ->
                    zip.putNextEntry(ZipEntry("$naam/" + f.relativeTo(map).invariantSeparatorsPath))
                    f.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                    aantal++
                }
            }
            listOf(DB, "$DB-wal", "$DB-shm").map { context.getDatabasePath(it) }.filter { it.isFile }.forEach { f ->
                zip.putNextEntry(ZipEntry("databases/${f.name}"))
                f.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
                aantal++
            }
            losNoBackup.map { File(context.noBackupFilesDir, it) }.filter { it.isFile }.forEach { f ->
                zip.putNextEntry(ZipEntry("nobackup/${f.name}"))
                f.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
                aantal++
            }
        }
        return aantal
    }

    /**
     * Zet een back-up terug. Alles wat er nu in de app staat, wordt vervangen.
     * Eerst wordt alles uitgepakt en gecontroleerd; pas als dat lukt, wordt het oude weggehaald.
     * Daarna moet de app opnieuw starten.
     *
     * Geeft `null` als het gelukt is, anders een korte uitleg.
     */
    fun zetTerug(context: Context, inn: InputStream): String? {
        val tijdelijk = File(context.cacheDir, "terugzetten").apply { deleteRecursively(); mkdirs() }
        try {
            var merk = false
            var totaal = 0L
            ZipInputStream(inn.buffered()).use { zip ->
                while (true) {
                    val e = zip.nextEntry ?: break
                    if (e.isDirectory) continue
                    if (e.name == MERK) {
                        merk = true
                        continue
                    }
                    if (!toegestaan(e.name)) continue
                    val doel = File(tijdelijk, e.name)
                    // Nooit buiten de map schrijven (zip met "../").
                    if (!doel.canonicalPath.startsWith(tijdelijk.canonicalPath + File.separator)) return "Dit bestand klopt niet."
                    doel.parentFile?.mkdirs()
                    var grootte = 0L
                    doel.outputStream().use { o ->
                        val buf = ByteArray(64 * 1024)
                        while (true) {
                            val n = zip.read(buf)
                            if (n < 0) break
                            grootte += n
                            totaal += n
                            if (grootte > MAX_BESTAND || totaal > MAX_TOTAAL) return "Deze back-up is te groot."
                            o.write(buf, 0, n)
                        }
                    }
                }
            }
            if (!merk) return "Dit is geen back-up van bash-bord."

            // Database dicht, dan het oude weg en het nieuwe erin.
            AppDatabase.sluit()
            mappen(context).forEach { (naam, map) ->
                val nieuw = File(tijdelijk, naam)
                if (nieuw.isDirectory) {
                    map.listFiles()?.forEach { it.deleteRecursively() }
                    nieuw.copyRecursively(map.apply { mkdirs() }, overwrite = true)
                }
            }
            val dbNieuw = File(tijdelijk, "databases")
            if (dbNieuw.isDirectory) {
                listOf(DB, "$DB-wal", "$DB-shm").forEach { context.getDatabasePath(it).delete() }
                dbNieuw.listFiles()?.forEach { it.copyTo(context.getDatabasePath(it.name), overwrite = true) }
            }
            File(tijdelijk, "nobackup").listFiles()?.forEach { it.copyTo(File(context.noBackupFilesDir, it.name), overwrite = true) }
            return null
        } catch (_: Exception) {
            return "Terugzetten lukte niet. Is dit het goede bestand?"
        } finally {
            tijdelijk.deleteRecursively()
        }
    }

    private fun toegestaan(naam: String): Boolean {
        if (naam.contains("..") || naam.startsWith("/")) return false
        return naam.startsWith("files/") ||
            naam.startsWith("shared_prefs/") ||
            naam in listOf(DB, "$DB-wal", "$DB-shm").map { "databases/$it" } ||
            naam in losNoBackup.map { "nobackup/$it" }
    }
}
