package com.behavior.collector.model;

import com.behavior.collector.events.KeyboardEventData;
import com.behavior.collector.events.MouseEventData;
import com.behavior.collector.events.WindowEventData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Container holding all raw events captured during a 30-second recording session.
 */
public class BehavioralSession {
    private final String userId;
    private final SessionLabel label;
    private long startTimeMillis;
    private long endTimeMillis;

    private final List<MouseEventData> mouseEvents = Collections.synchronizedList(new ArrayList<>());
    private final List<KeyboardEventData> keyboardEvents = Collections.synchronizedList(new ArrayList<>());
    private final List<WindowEventData> windowEvents = Collections.synchronizedList(new ArrayList<>());

    public BehavioralSession(String userId, SessionLabel label) {
        this.userId = userId != null ? userId.trim() : "anonymous";
        this.label = label != null ? label : SessionLabel.GENUINE;
    }

    public void startSession() {
        this.startTimeMillis = System.currentTimeMillis();
    }

    public void endSession() {
        this.endTimeMillis = System.currentTimeMillis();
    }

    public void addMouseEvent(MouseEventData event) {
        mouseEvents.add(event);
    }

    public void addKeyboardEvent(KeyboardEventData event) {
        keyboardEvents.add(event);
    }

    public void addWindowEvent(WindowEventData event) {
        windowEvents.add(event);
    }

    public String getUserId() {
        return userId;
    }

    public SessionLabel getLabel() {
        return label;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public long getEndTimeMillis() {
        return endTimeMillis;
    }

    public double getDurationSeconds() {
        if (endTimeMillis <= startTimeMillis) return 0.0;
        return (endTimeMillis - startTimeMillis) / 1000.0;
    }

    public List<MouseEventData> getMouseEvents() {
        return new ArrayList<>(mouseEvents);
    }

    public List<KeyboardEventData> getKeyboardEvents() {
        return new ArrayList<>(keyboardEvents);
    }

    public List<WindowEventData> getWindowEvents() {
        return new ArrayList<>(windowEvents);
    }
}
