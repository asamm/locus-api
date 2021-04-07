package locus.api.utils

/**
 * Generic static logger instance used in Locus API system.
 * To receive correct log messages, initialize over `registerLogger` as soon as possible.
 */
object Logger {

    private var logger: ILogger? = null

    fun registerLogger(logger: ILogger) {
        Logger.logger = logger
    }

    fun logI(tag: String, msg: String) {
        logger?.logI(tag, msg)
                ?: println("$tag - $msg")
    }

    fun logD(tag: String, msg: String) {
        logger?.logD(tag, msg)
                ?: println("$tag - $msg")
    }

    fun logW(tag: String, msg: String) {
        logger?.logW(tag, msg)
                ?: println("$tag - $msg")
    }

    fun logE(tag: String, msg: String) {
        logger?.logE(tag, msg)
                ?: System.err.println("$tag - $msg")
    }

    fun logE(tag: String, msg: String, e: Exception) {
        logger?.logE(tag, msg, e)
                ?: {
                    System.err.println(tag + " - " + msg + ", e:" + e.message)
                    e.printStackTrace()
                }()
    }

    interface ILogger {

        fun logI(tag: String, msg: String)

        fun logD(tag: String, msg: String)

        fun logW(tag: String, msg: String)

        fun logE(tag: String, msg: String)

        fun logE(tag: String, msg: String, e: Exception)
    }
}
