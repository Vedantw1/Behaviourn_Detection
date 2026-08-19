package com.behavior.collector.listeners;

import com.behavior.collector.events.WindowEventData;
import com.behavior.collector.events.WindowFocusState;
import javafx.beans.value.ChangeListener;

/**
 * Listens for application window focus gain and loss events.
 */
public class WindowEventListener {
    private final EventCollector collector;

    public WindowEventListener(EventCollector collector) {
        this.collector = collector;
    }

    public ChangeListener<Boolean> getFocusChangeListener() {
        return (observable, oldValue, newValue) -> {
            if (!collector.isRecording()) return;
            long now = System.currentTimeMillis();
            WindowFocusState state = Boolean.TRUE.equals(newValue)
                    ? WindowFocusState.GAINED
                    : WindowFocusState.LOST;
            collector.recordWindowEvent(new WindowEventData(now, state));
        };
    }
}
