package com.behavior.collector.events;

/**
 * Immutable representation of an application window focus change event.
 *
 * @param timestamp Epoch timestamp in milliseconds
 * @param state     Focus state (GAINED or LOST)
 */
public record WindowEventData(
        long timestamp,
        WindowFocusState state
) {}
