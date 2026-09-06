package lib.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.text.InputType
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

import rosh.terminal.R

class TerminalView @JvmOverloads constructor(
    c: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(c, attrs, defStyleAttr) {

    private val bg = Paint().apply {
        color = Color.BLACK
    }
    
    private val teks = Paint().apply {
        color = Color.WHITE
        textSize = 30f
        typeface = Typeface.MONOSPACE
        isAntiAlias = true
    }
    
    private val teksDir = Paint().apply {
        color = Color.BLUE
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.ITALIC)
        textSize = 30f
        isAntiAlias = true
    }

    private val maxLine = 100
    private var isiTerminal: String = "Terminal siap...\n"
    private var direktoriSaatIni: String = "~"
    private var inputUser: String = ""
    private var statusKey: Boolean = false
    private var isTextSelectable: Boolean = false
    private var onCommandListener: ((String) -> Unit)? = null

    fun setOnCommandListener(listener: (String) -> Unit) {
        onCommandListener = listener
    }

    init {
        if (attrs != null) {
            val typedArray = c.obtainStyledAttributes(attrs, R.styleable.TerminalView)
            isTextSelectable = typedArray.getBoolean(R.styleable.TerminalView_textSelectable, false)
            typedArray.recycle()
        }

        isFocusable = true
        isFocusableInTouchMode = true
        
        if (!statusKey) {
            setOnClickListener {
                OpenKey()
            }
        }
    }

    override fun onDraw(kertas: Canvas) {
        super.onDraw(kertas)
        kertas.drawColor(Color.BLACK)

        val marginKiri = 20f
        val tinggiBaris = teks.descent() - teks.ascent()
        
        val barisList = isiTerminal.split("\n").toMutableList()
        val promptAktif = "$direktoriSaatIni $ $inputUser"
        barisList.add(promptAktif)
        
        var posisiY = 50f
        val totalTinggiTeks = barisList.size * tinggiBaris
        
        if (totalTinggiTeks > height) {
            posisiY = height - totalTinggiTeks + 50f
        }

        for (i in barisList.indices) {
            val baris = barisList[i]
            
            if (i == barisList.lastIndex) {
                val bagianDirDanSimbol = "$direktoriSaatIni $ "
                kertas.drawText(bagianDirDanSimbol, marginKiri, posisiY, teksDir)
                val lebarDir = teksDir.measureText(bagianDirDanSimbol)
                kertas.drawText(inputUser, marginKiri + lebarDir, posisiY, teks)
            } else {
                kertas.drawText(baris, marginKiri, posisiY, teks)
            }
            
            posisiY += tinggiBaris
        }
    }

    fun append(terima: String?) {
        if (terima != null) {
            isiTerminal += terima
            val barisList = isiTerminal.split("\n")
            if (barisList.size > maxLine) {
                isiTerminal = barisList.takeLast(maxLine).joinToString("\n")
            }
            invalidate()
        }
    }
    
    fun textClear() {
        isiTerminal = ""
        inputUser = ""
        invalidate()
    }
    
    fun setText(terima: String?) {
        isiTerminal = terima ?: ""
        invalidate()
    }
    
    fun setDir(terima: String?, maxFolder: Int = 2) {
    val dir = terima ?: "~"

    val bagian = dir
        .trim('/')
        .split('/')
        .filter { it.isNotEmpty() }

    direktoriSaatIni =
        if (bagian.size > maxFolder) {
            ".../" + bagian.takeLast(maxFolder).joinToString("/")
        } else {
            dir
        }

    invalidate()
}
    
    fun runCmd(): String {
        return inputUser
    }
    
    private fun OpenKey() {
        requestFocus()
        val key = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        key.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        statusKey = true
    }

    override fun onCheckIsTextEditor(): Boolean { return true }

override fun onCreateInputConnection(
    outAttrs: EditorInfo
): InputConnection {

    outAttrs.inputType =
        InputType.TYPE_CLASS_TEXT or
        InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or
        InputType.TYPE_TEXT_FLAG_MULTI_LINE

    outAttrs.imeOptions =
        EditorInfo.IME_ACTION_DONE

    return object : BaseInputConnection(this, false) {

        override fun commitText(
            text: CharSequence?,
            newCursorPosition: Int
        ): Boolean {

            if (text != null) {
                inputUser += text.toString()
                invalidate()
            }

            return true
        }

        override fun deleteSurroundingText(
            beforeLength: Int,
            afterLength: Int
        ): Boolean {

            if (beforeLength > 0 && inputUser.isNotEmpty()) {
                inputUser = inputUser.dropLast(
                    beforeLength.coerceAtMost(inputUser.length)
                )
                invalidate()
            }

            return true
        }

        override fun sendKeyEvent(event: KeyEvent): Boolean {
            return this@TerminalView.dispatchKeyEvent(event)
        }

        override fun performEditorAction(actionCode: Int): Boolean {

            if (
                actionCode == EditorInfo.IME_ACTION_DONE ||
                actionCode == EditorInfo.IME_ACTION_GO ||
                actionCode == EditorInfo.IME_ACTION_NEXT
            ) {
                executeCommand()
                return true
            }

            return false
        }
    }
}
    
    override fun onKeyDown(
    keyCode: Int,
    event: KeyEvent?
): Boolean {

    if (keyCode == KeyEvent.KEYCODE_ENTER) {
        executeCommand()
        return true
    }

    if (keyCode == KeyEvent.KEYCODE_DEL) {

        if (inputUser.isNotEmpty()) {
            inputUser = inputUser.dropLast(1)
            invalidate()
        }

        return true
    }

    val unicode = event?.unicodeChar ?: 0

    if (unicode != 0) {
        inputUser += unicode.toChar()
        invalidate()
        return true
    }

    return super.onKeyDown(keyCode, event)
}
    
    private fun executeCommand() {

    val perintahFinal = inputUser

    isiTerminal +=
        "$direktoriSaatIni $ $perintahFinal\n"

    inputUser = ""

    invalidate()

    onCommandListener?.invoke(perintahFinal)
    }
}
