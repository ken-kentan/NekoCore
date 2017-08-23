package jp.kentan.minecraft.neko_core.utils;

import java.util.logging.Logger;


public class Log {
    private static Logger sLogger;

    public Log(Logger logger){
        sLogger = logger;
    }

    public static void print(final String str){
        sLogger.info(str);
    }

    public static void warn(final String str){
        sLogger.warning(str);
    }
}
