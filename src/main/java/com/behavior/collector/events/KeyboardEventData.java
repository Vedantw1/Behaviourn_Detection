package com.behavior.collector.events;

/**
 * Immutable representation of a raw keyboard event.
 * Note: Typed characters are explicitly excluded for privacy compliance.
 *
 * @param pressTimestamp   Timestamp in milliseconds when key was pressed
 * @param releaseTimestamp Timestamp in milliseconds when key was released
 * @param isBackspace      True if the key was Backspace
 */
public record KeyboardEventData(
        long pressTimestamp,
        long releaseTimestamp,
        boolean isBackspace
) {
    /**
     * Calculates the dwell time (duration key was held down) in milliseconds.
     *
     * @return Dwell time in milliseconds
     */
    public long dwellTime() {
        return Math.max(0, releaseTimestamp - pressTimestamp);
    }
}
