package lib.os.exec

open class Shell(
    open var textCmd: String = "",
    open var out: String = "",
    open var sesi: Int = 0
) {
    open fun cmd(terima: String) {
        textCmd = terima
        out = "diterima"
        sesi = 1
    }
}