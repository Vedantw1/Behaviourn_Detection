package com.behavior.collector.listeners;

import com.behavior.collector.events.KeyboardEventData;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * System-wide global keyboard listener using JNativeHook.
 * Captures keystrokes across all applications (even when window focus is lost).
 * Explicitly discards typed character content to remain privacy compliant.
 */
public class GlobalKeyboardListener implements NativeKeyListener {

    private final EventCollector collector;
    private final Map<Integer, Long> pressTimestamps = new ConcurrentHashMap<>();

    public GlobalKeyboardListener(EventCollector collector) {
        this.collector = collector;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (!collector.isRecording()) return;
        int keyCode = e.getKeyCode();
        long now = System.currentTimeMillis();
        pressTimestamps.putIfAbsent(keyCode, now);
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        if (!collector.isRecording()) return;
        int keyCode = e.getKeyCode();
        long releaseTime = System.currentTimeMillis();
        Long pressTime = pressTimestamps.remove(keyCode);

        if (pressTime != null) {
            boolean isBackspace = (keyCode == NativeKeyEvent.VC_BACKSPACE || keyCode == NativeKeyEvent.VC_DELETE);
            collector.recordKeyboardEvent(new KeyboardEventData(pressTime, releaseTime, isBackspace));
        }
    }

    public void reset() {
        pressTimestamps.clear();
    }
}
