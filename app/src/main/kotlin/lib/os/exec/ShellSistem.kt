package lib.os.exec

class ShellSistem : Shell() {
    override fun cmd(terima: String) {
        textCmd = terima
        out = "diterima oleh custom"
        sesi = 1
    }
}