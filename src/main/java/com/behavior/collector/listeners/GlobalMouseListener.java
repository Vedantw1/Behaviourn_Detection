package com.behavior.collector.listeners;

import com.behavior.collector.events.MouseEventData;
import com.behavior.collector.events.MouseEventType;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelListener;

/**
 * System-wide global mouse listener using JNativeHook.
 * Captures mouse movements, clicks, and scroll wheel actions across all applications on the OS.
 */
public class GlobalMouseListener implements NativeMouseInputListener, NativeMouseWheelListener {

    private final EventCollector collector;

    public GlobalMouseListener(EventCollector collector) {
        this.collector = collector;
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent e) {
        if (!collector.isRecording()) return;
        collector.recordMouseEvent(new MouseEventData(
                System.currentTimeMillis(),
                e.getX(),
                e.getY(),
                MouseEventType.MOVE
        ));
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent e) {
        if (!collector.isRecording()) return;
        collector.recordMouseEvent(new MouseEventData(
                System.currentTimeMillis(),
                e.getX(),
                e.getY(),
                MouseEventType.MOVE
        ));
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent e) {
        if (!collector.isRecording()) return;
        MouseEventType type = (e.getButton() == NativeMouseEvent.BUTTON1)
                ? MouseEventType.LEFT_CLICK
                : MouseEventType.RIGHT_CLICK;
        collector.recordMouseEvent(new MouseEventData(
                System.currentTimeMillis(),
                e.getX(),
                e.getY(),
                type
        ));
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        // Handled via nativeMouseClicked / moved
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent e) {
        // Handled via nativeMouseClicked / moved
    }

    @Override
    public void nativeMouseWheelMoved(NativeMouseWheelEvent e) {
        if (!collector.isRecording()) return;
        collector.recordMouseEvent(new MouseEventData(
                System.currentTimeMillis(),
                e.getX(),
                e.getY(),
                MouseEventType.SCROLL
        ));
    }
}
