package lxx.util

interface Debugger {

    fun debugProperty(name: String, value: Double?) {
        debugProperty(name, java.lang.String.format("%.2f", value))
    }

    fun debugProperty(name: String, value: String)

}
