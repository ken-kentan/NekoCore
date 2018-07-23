package jp.kentan.minecraft.neko_core.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
    private static Logger sLogger;

    public static void setLogger(Logger logger){
        sLogger = logger;
    }

    public static void info(final String msg){
        sLogger.log(Level.INFO, msg);
    }

    public static void warn(final String msg){
        sLogger.log(Level.WARNING, msg);
    }

    public static void error(final String msg){
        sLogger.log(Level.SEVERE, msg);
    }
}
