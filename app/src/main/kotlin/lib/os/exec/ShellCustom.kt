package lib.os.exec

class ShellCustom : Shell() {
    override fun cmd(terima: String) {
        textCmd = terima
        out = "diterima oleh sistem"
        sesi = 1
    }
}