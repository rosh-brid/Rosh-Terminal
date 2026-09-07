package rosh.terminal

import lib.widget.TerminalView

import android.os.*
import android.widget.*

import java.io.*
import java.lang.Process

import kotlin.concurrent.thread

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat

class MainActivity : AppCompatActivity() {

    private lateinit var pusat: DrawerLayout
    private lateinit var terminal: TerminalView
    private lateinit var mode: Switch
    private lateinit var proses: Process
    private lateinit var masuk: BufferedWriter
    private lateinit var keluar: BufferedReader
    private val prootDir by lazy { File(applicationInfo.nativeLibraryDir, "libproot.so") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        PasangId()
        Awal()
        Tombol()
    }


    private fun PasangId() {
        pusat = findViewById(R.id.pusat)
        terminal = findViewById(R.id.terminal)
        mode = findViewById(R.id.mode)
    }


    private fun Awal() {
        AturMode()
        terminal.setDir("/")
    }


    @Deprecated("OnBackPressedDispatcher")
    override fun onBackPressed() {
        if (pusat.isDrawerOpen(GravityCompat.START)) {
            pusat.closeDrawer(GravityCompat.START)
        } else { Keluar() }
        @Suppress("DEPRECATION")
        super.onBackPressed()
    }

    private fun Keluar() {
        AlertDialog.Builder(this)
            .setTitle("Exit")
            .setMessage("You will exit app?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Sure") { _, _ ->
                try { masuk.close() } catch (_: Exception) { }
                try { proses.destroy() } catch (_: Exception) { }
                finish()
            }
            .show()
    }


    private fun Tombol() {
        mode.setOnCheckedChangeListener { _, _ ->
            pusat.closeDrawer(GravityCompat.START)
            AturMode()
        }

        terminal.setOnCommandListener { perintah ->
            when (perintah.lowercase()) {
                "clear" -> terminal.textClear()
                "exit" -> Keluar()
                "get-proot-dir" -> terminal.append(prootDir.absolutePath)
                else -> Jalankan(perintah)
            }
        }
    }
    
    private fun BuatProses(builder: ProcessBuilder) {
        proses = builder.redirectErrorStream(true).start()
        masuk = BufferedWriter( OutputStreamWriter(proses.outputStream) )
        keluar = BufferedReader( InputStreamReader(proses.inputStream) )
    }


    private fun AturMode() {
        if (mode.isChecked) {
            BuatProses(ProcessBuilder("sh"))
            mode.text = "System Shell"
            terminal.append("System Shell selected\n")
            MulaiBacaOutput()
                masuk.write("cd ${filesDir}")
                masuk.newLine()
                masuk.flush()
        } else {
            AturProot()
            mode.text = "Custom Shell"
            terminal.append("Custom Shell selected\n")
            MulaiBacaOutput()
        }
    }

    private fun Jalankan(perintah: String) {
        thread {
            try {
                masuk.write(perintah)
                masuk.newLine()
                masuk.flush()

            } catch (e: Exception) {
                runOnUiThread {
                    terminal.append( "\nError: ${e.message}\n" )
                }
            }
        }
    }


    private fun MulaiBacaOutput() {
    thread(name = "Shell-Output") {
        val buffer = CharArray(4096)
        try {
            while (!Thread.currentThread().isInterrupted) {
                val jumlah = keluar.read(buffer)
                    if (jumlah == -1) { break }
                    if (jumlah == 0) { continue }
                val teks = String(buffer, 0, jumlah)

                runOnUiThread {
                    if (isFinishing || isDestroyed) { return@runOnUiThread }
                    terminal.append(teks)
                }
            }

        } catch (e: IOException) {
            if (!isFinishing && !isDestroyed) {
                runOnUiThread {
                    terminal.append( "\n[Shell I/O Error] ${e.message}\n" )
                }
            }

        } catch (e: Exception) {
            if (!isFinishing && !isDestroyed) {
                runOnUiThread {
                    terminal.append( "\n[Shell Error] ${e.message}\n" )
                }
            }

        } finally {
            try { keluar.close() } catch (_: Exception) { }
            }
        }
    }

    override fun onDestroy() {
        try { masuk.close() } catch (_: Exception) { }
        try { proses.destroy() } catch (_: Exception) { }
        super.onDestroy()
    }
    
    private fun AturProot(): Boolean {
    val root = File(filesDir, "../root")

    if (!root.exists()) {
        root.mkdirs()
        // kosongkan
        // tar mkdir extract dll
        return false
    } else {
        val PROOT_TMP_DIR = File(root, "tmp")
        val args = mutableListOf<String>().apply {
        add(prootDir.absolutePath)
        add("--kill-on-exit")
        add("-w")
        add("/")
        add("-0")
        add("--link2symlink")
        add("-r")
        add(root.absolutePath)

        add("/bin/sh")
    }
        val builder = ProcessBuilder( *args.toTypedArray() )    
    
        val env = builder.environment()
            env["PROOT_LOADER"] = File( applicationInfo.nativeLibraryDir, "libloader.so" ).absolutePath
            env["PROOT_LOADER_32"] = File( applicationInfo.nativeLibraryDir, "libloader32.so" ).absolutePath
            env["PROOT_TMP_DIR"] = PROOT_TMP_DIR.absolutePath
            env["TMPDIR"] = PROOT_TMP_DIR.absolutePath
            env["LD_LIBRARY_PATH"] = applicationInfo.nativeLibraryDir
            env["PATH"] = "/bin:/sbin:/usr/bin:/usr/sbin:/usr/games:/usr/local/bin:/usr/local/sbin"

        BuatProses(builder)

        return true
        }
    }
}