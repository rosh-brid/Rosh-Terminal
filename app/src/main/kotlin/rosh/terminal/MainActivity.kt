package rosh.terminal

import lib.widget.TerminalView
import lib.os.exec.*

import android.os.Bundle
import android.widget.*

import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.*

class MainActivity : AppCompatActivity() {

    private lateinit var pusat: DrawerLayout 
    private lateinit var terminal: TerminalView
    private lateinit var shell: Shell
    private lateinit var mode: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        PasangId()
        Tombol()
        Awal()
    }
    
    private fun PasangId() {
        pusat = findViewById(R.id.pusat)
        terminal = findViewById(R.id.terminal)
        mode = findViewById(R.id.mode)
        shell = ShellCustom() 
        
        terminal.setOnCommandListener { perintah ->
            Jalankan(perintah)
        }
    }
    
    override fun onBackPressed() {
        if(pusat.isDrawerOpen(GravityCompat.START)){
            pusat.closeDrawer(GravityCompat.START)
        }
    }
    
    private fun Awal() {
        terminal.setDir(filesDir.absolutePath)
        AturMode()
    }
    
    private fun Jalankan(perintah: String) {
        when (perintah.lowercase()) {
            "exit" -> finish()
            "clear" -> terminal.textClear()
            else -> MulaiTerminal(perintah)
        }
    }
    
    private fun MulaiTerminal(perintah: String) {
        shell.cmd(perintah)
        terminal.append("${shell.out}\n")
    }
    
    private fun Tombol() {
        mode.setOnCheckedChangeListener { _, _ ->
            AturMode()
        }
    }
    
    private fun AturMode() {
        if (mode.isChecked) {
            shell = ShellCustom()
            terminal.append("Custom Use\n")
        } else {
            shell = ShellSistem()
            terminal.append("System Use\n")
        }
    }
}
