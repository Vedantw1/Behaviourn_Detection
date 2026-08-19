package com.behavior.collector.events;

/**
 * Immutable representation of a single raw mouse event.
 *
 * @param timestamp Epoch timestamp in milliseconds when the event occurred
 * @param x         X coordinate relative to window/screen
 * @param y         Y coordinate relative to window/screen
 * @param eventType Type of mouse action (MOVE, LEFT_CLICK, RIGHT_CLICK, SCROLL)
 */
public record MouseEventData(
        long timestamp,
        double x,
        double y,
        MouseEventType eventType
) {}
