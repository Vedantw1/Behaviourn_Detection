package com.behavior.collector.util;

/**
 * Time utility helper methods.
 */
public final class TimeUtils {

    private TimeUtils() {
        // Utility class
    }

    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static double millisToSeconds(long millis) {
        return millis / 1000.0;
    }

    public static String formatDurationSeconds(double seconds) {
        return String.format("%.2fs", seconds);
    }
}
