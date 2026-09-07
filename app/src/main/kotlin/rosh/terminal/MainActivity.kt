package rosh.terminal

import lib.widget.TerminalView

import android.os.*
import android.widget.*

import java.io.*

import kotlin.concurrent.thread

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat

class MainActivity : AppCompatActivity() {

    private lateinit var pusat: DrawerLayout
    private lateinit var terminal: TerminalView
    private lateinit var mode: Switch
    private val proses = ProcessBuilder("sh").redirectErrorStream(true).start()
    private val masuk = BufferedWriter( OutputStreamWriter(proses.outputStream) )
    private val keluar = BufferedReader( InputStreamReader(proses.inputStream) )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        PasangId()
        Awal()
        Tombol()
        MulaiBacaOutput()
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
                else -> Jalankan(perintah)
            }
        }
    }


    private fun AturMode() {
        if (mode.isChecked) {
            mode.text = "System Shell"
            terminal.append( "System Shell selected\n" )
        } else {
            mode.text = "Custom Shell"
            terminal.append( "Custom Shell selected\n" )
        }
    }

    private fun Jalankan(perintah: String) {
        if(mode.isChecked){ShellSistem(perintah)}
        else{ShellSistem(perintah)}
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
    
    private fun ShellSistem(cmd:String){
        thread {
            try {
                masuk.write(cmd)
                masuk.newLine()
                masuk.flush()

            } catch (e: Exception) {
                runOnUiThread {
                    terminal.append( "\nError: ${e.message}\n" )
                }
            }
        }
    }

    override fun onDestroy() {
        try { masuk.close() } catch (_: Exception) { }
        try { proses.destroy() } catch (_: Exception) { }
        super.onDestroy()
    }
}