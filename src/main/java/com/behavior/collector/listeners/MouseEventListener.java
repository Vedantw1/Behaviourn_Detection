package com.behavior.collector.listeners;

import com.behavior.collector.events.MouseEventData;
import com.behavior.collector.events.MouseEventType;
import com.behavior.collector.listeners.EventCollector;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

/**
 * Listens for JavaFX mouse and scroll events during an active recording session
 * and routes them to the EventCollector.
 */
public class MouseEventListener {
    private final EventCollector collector;

    public MouseEventListener(EventCollector collector) {
        this.collector = collector;
    }

    public EventHandler<MouseEvent> getMouseMovedHandler() {
        return event -> {
            if (!collector.isRecording())
                return;
            long now = System.currentTimeMillis();
            collector.recordMouseEvent(new MouseEventData(now, event.getX(), event.getY(), MouseEventType.MOVE));
        };
    }

    public EventHandler<MouseEvent> getMouseClickedHandler() {
        return event -> {
            if (!collector.isRecording())
                return;
            long now = System.currentTimeMillis();
            MouseEventType type = (event.getButton() == MouseButton.PRIMARY)
                    ? MouseEventType.LEFT_CLICK
                    : MouseEventType.RIGHT_CLICK;
            collector.recordMouseEvent(new MouseEventData(now, event.getX(), event.getY(), type));
        };
    }

    public EventHandler<ScrollEvent> getScrollHandler() {
        return event -> {
            if (!collector.isRecording())
                return;
            long now = System.currentTimeMillis();
            collector.recordMouseEvent(new MouseEventData(now, event.getX(), event.getY(), MouseEventType.SCROLL));
        };
    }
}
