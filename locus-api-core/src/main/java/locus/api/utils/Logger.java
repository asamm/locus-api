package locus.api.utils;

public class Logger {

    private static ILogger logger;

    public static void registerLogger(ILogger logger) {
        Logger.logger = logger;
    }

    public static void logI(String tag, String msg) {
        if (logger == null) {
            System.out.println(tag + " - " + msg);
        } else {
            logger.logI(tag, msg);
        }
    }

    public static void logD(String tag, String msg) {
        if (logger == null) {
            System.out.println(tag + " - " + msg);
        } else {
            logger.logD(tag, msg);
        }
    }

    public static void logW(String tag, String msg) {
        if (logger == null) {
            System.out.println(tag + " - " + msg);
        } else {
            logger.logW(tag, msg);
        }
    }

    public static void logE(String tag, String msg) {
        if (logger == null) {
            System.err.println(tag + " - " + msg);
        } else {
            logger.logE(tag, msg);
        }
    }

    public static void logE(String tag, String msg, Exception e) {
        if (logger == null) {
            System.err.println(tag + " - " + msg + ", e:" + e.getMessage());
            e.printStackTrace();
        } else {
            logger.logE(tag, msg, e);
        }
    }

    public interface ILogger {

        void logI(String tag, String msg);

        void logD(String tag, String msg);

        void logW(String tag, String msg);

        void logE(String tag, String msg);

        void logE(String tag, String msg, Exception e);
    }
}
