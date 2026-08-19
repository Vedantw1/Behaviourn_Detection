package com.behavior.collector.listeners;

import com.behavior.collector.events.KeyboardEventData;
import com.behavior.collector.model.SessionLabel;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalKeyboardListenerTest {

    @BeforeAll
    public static void initJavaFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
            latch.await();
        } catch (IllegalStateException e) {
            // Platform already initialized
        }
    }

    @Test
    public void testGlobalNativeKeyEventsRecorded() {
        EventCollector collector = new EventCollector();
        collector.startRecording("global_user", SessionLabel.GENUINE, 30);

        GlobalKeyboardListener listener = new GlobalKeyboardListener(collector);

        // 1. Simulate global native key press & release for key 'A' (VC_A)
        NativeKeyEvent pressA = new NativeKeyEvent(NativeKeyEvent.NATIVE_KEY_PRESSED, 0, 0, NativeKeyEvent.VC_A, NativeKeyEvent.CHAR_UNDEFINED);
        NativeKeyEvent releaseA = new NativeKeyEvent(NativeKeyEvent.NATIVE_KEY_RELEASED, 0, 0, NativeKeyEvent.VC_A, NativeKeyEvent.CHAR_UNDEFINED);

        listener.nativeKeyPressed(pressA);
        listener.nativeKeyReleased(releaseA);

        // 2. Simulate global native key press & release for Backspace (VC_BACKSPACE)
        NativeKeyEvent pressBack = new NativeKeyEvent(NativeKeyEvent.NATIVE_KEY_PRESSED, 0, 0, NativeKeyEvent.VC_BACKSPACE, NativeKeyEvent.CHAR_UNDEFINED);
        NativeKeyEvent releaseBack = new NativeKeyEvent(NativeKeyEvent.NATIVE_KEY_RELEASED, 0, 0, NativeKeyEvent.VC_BACKSPACE, NativeKeyEvent.CHAR_UNDEFINED);

        listener.nativeKeyPressed(pressBack);
        listener.nativeKeyReleased(releaseBack);

        collector.stopRecording();

        List<KeyboardEventData> events = collector.getCurrentSession().getKeyboardEvents();
        assertTrue(events.size() >= 2, "Global key events should be recorded");

        KeyboardEventData backspaceEvent = events.get(events.size() - 1);
        assertTrue(backspaceEvent.isBackspace(), "VC_BACKSPACE native key should be flagged as isBackspace");
    }
}
