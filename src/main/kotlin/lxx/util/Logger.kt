package lxx.util

enum class LogLevel(val intLevel: Int) {
    DEBUG : LogLevel(0)
    INFO: LogLevel(1)
    WARN: LogLevel(2)
    ERROR: LogLevel(3)
    OFF: LogLevel(4)
}

object Logger {

    var level = LogLevel.DEBUG

    fun debug(message: () -> String, ex: Throwable? = null) {
        log(LogLevel.DEBUG, message, ex)
    }

    fun log(level: LogLevel, message: () -> String, ex: Throwable? = null) {
        if (level.intLevel >= this.level.intLevel) {
            println(message())
            ex?.printStackTrace()
        }
    }

}