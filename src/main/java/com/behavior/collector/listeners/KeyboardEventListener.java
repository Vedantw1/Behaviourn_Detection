package com.behavior.collector.listeners;

import com.behavior.collector.events.KeyboardEventData;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listens for keyboard press and release events, matching timing pairs while explicitly
 * discarding key character values to maintain privacy.
 */
public class KeyboardEventListener {
    private final EventCollector collector;
    private final Map<KeyCode, Long> pressTimestamps = new ConcurrentHashMap<>();

    public KeyboardEventListener(EventCollector collector) {
        this.collector = collector;
    }

    public EventHandler<KeyEvent> getKeyPressedHandler() {
        return event -> {
            if (!collector.isRecording()) return;
            KeyCode code = event.getCode();
            long now = System.currentTimeMillis();
            // Record initial press timestamp if not already held down
            pressTimestamps.putIfAbsent(code, now);
        };
    }

    public EventHandler<KeyEvent> getKeyReleasedHandler() {
        return event -> {
            if (!collector.isRecording()) return;
            KeyCode code = event.getCode();
            long releaseTime = System.currentTimeMillis();
            Long pressTime = pressTimestamps.remove(code);

            if (pressTime != null) {
                boolean isBackspace = (code == KeyCode.BACK_SPACE || code == KeyCode.DELETE);
                collector.recordKeyboardEvent(new KeyboardEventData(pressTime, releaseTime, isBackspace));
            }
        };
    }

    public void reset() {
        pressTimestamps.clear();
    }
}
