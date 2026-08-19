package com.behavior.collector.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global logging utility wrapping java.util.logging.Logger.
 */
public final class LoggerUtil {

    private LoggerUtil() {
        // Utility class
    }

    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }

    public static void info(Class<?> clazz, String message) {
        getLogger(clazz).log(Level.INFO, message);
    }

    public static void warning(Class<?> clazz, String message) {
        getLogger(clazz).log(Level.WARNING, message);
    }

    public static void error(Class<?> clazz, String message, Throwable throwable) {
        getLogger(clazz).log(Level.SEVERE, message, throwable);
    }
}
