package org.auslides.play.module.morphia.utils;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import play.Logger;

import java.util.Map;

public class MorphiaLogger {

    public static boolean isDebugEnabled() {
        return Logger.isDebugEnabled();
    }

    public static boolean isWarnEnabled() {
        return Logger.isWarnEnabled();
    }

    public static boolean isErrorEnabled() {
        return Logger.isErrorEnabled();
    }

    public static void debug(Config morphiaConf) {
        if (isDebugEnabled()) {
            if (morphiaConf != null) {
                debug("Config by morphiaConf");
                for (Map.Entry<String, ConfigValue> entry : morphiaConf.entrySet()) {
                    if ( entry.getKey().contains("scan") || entry.getKey().contains("morphia.prefixes"))
                        debug("%s=%s", entry.getKey(), morphiaConf.getStringList(entry.getKey()));
                    else
                        debug("%s=%s", entry.getKey(), morphiaConf.getString(entry.getKey()));
                }
            }
        }
    }

    public static void debug(String msg, Object... args) {
        if (isDebugEnabled()) {
            Logger.debug(format(msg, args));
        }
    }

    public static void debug(Throwable t, String msg, Object... args) {
        if (isDebugEnabled()) {
            Logger.debug(format(msg, args), t);
        }
    }

    public static void warn(String msg, Object... args) {
        Logger.warn(format(msg, args));
    }

    public static void warn(Throwable t, String msg, Object... args) {
        Logger.warn(format(msg, args), t);
    }

    public static void error(String msg, Object... args) {
        Logger.error(format(msg, args));
    }

    public static void error(Throwable t, String msg, Object... args) {
        Logger.error(format(msg, args), t);
    }

    private static String format(String msg, Object... args) {
        return String.format("MorphiaModule> %s", String.format(msg, args));
    }

}
