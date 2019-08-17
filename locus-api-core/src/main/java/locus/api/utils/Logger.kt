package locus.api.utils

object Logger {

    private var logger: ILogger? = null

    fun registerLogger(logger: ILogger) {
        Logger.logger = logger
    }

    fun logI(tag: String, msg: String) {
        if (logger == null) {
            println("$tag - $msg")
        } else {
            logger!!.logI(tag, msg)
        }
    }

    fun logD(tag: String, msg: String) {
        if (logger == null) {
            println("$tag - $msg")
        } else {
            logger!!.logD(tag, msg)
        }
    }

    fun logW(tag: String, msg: String) {
        if (logger == null) {
            println("$tag - $msg")
        } else {
            logger!!.logW(tag, msg)
        }
    }

    fun logE(tag: String, msg: String) {
        if (logger == null) {
            System.err.println("$tag - $msg")
        } else {
            logger!!.logE(tag, msg)
        }
    }

    fun logE(tag: String, msg: String, e: Exception) {
        if (logger == null) {
            System.err.println(tag + " - " + msg + ", e:" + e.message)
            e.printStackTrace()
        } else {
            logger!!.logE(tag, msg, e)
        }
    }

    interface ILogger {

        fun logI(tag: String, msg: String)

        fun logD(tag: String, msg: String)

        fun logW(tag: String, msg: String)

        fun logE(tag: String, msg: String)

        fun logE(tag: String, msg: String, e: Exception)
    }
}
