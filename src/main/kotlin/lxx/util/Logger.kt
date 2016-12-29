package lxx.util

enum class LogLevel(val intLevel: Int) {
    DEBUG(0),
    INFO(1),
    WARN(2),
    ERROR(3),
    OFF(4);
}

object Logger {

    var level = LogLevel.valueOf(System.getProperty("loglevel") ?: "OFF")

    fun debug(message: () -> String, ex: Throwable? = null) {
        log(LogLevel.DEBUG, message, ex)
    }

    fun info(message: () -> String, ex: Throwable? = null) {
        log(LogLevel.INFO, message, ex)
    }

    fun warn(message: () -> String, ex: Throwable? = null) {
        log(LogLevel.WARN, message, ex)
    }

    fun log(level: LogLevel, message: () -> String, ex: Throwable? = null) {
        if (level.intLevel >= this.level.intLevel) {
            println("[$level] ${message()}")
            ex?.printStackTrace()
        }
    }

}