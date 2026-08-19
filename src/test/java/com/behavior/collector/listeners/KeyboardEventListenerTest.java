package com.behavior.collector.listeners;

import com.behavior.collector.events.KeyboardEventData;
import com.behavior.collector.model.BehavioralSession;
import com.behavior.collector.model.SessionLabel;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class KeyboardEventListenerTest {

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
    public void testBackspaceAndDeleteKeyEventsRecordedAsBackspace() {
        EventCollector collector = new EventCollector();
        collector.startRecording("test_user", SessionLabel.GENUINE, 30);

        KeyboardEventListener listener = new KeyboardEventListener(collector);

        // 1. Simulate BACK_SPACE key press & release (Windows style)
        KeyEvent backspacePress = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.BACK_SPACE, false, false, false, false);
        KeyEvent backspaceRelease = new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.BACK_SPACE, false, false, false, false);
        listener.getKeyPressedHandler().handle(backspacePress);
        listener.getKeyReleasedHandler().handle(backspaceRelease);

        // 2. Simulate DELETE key press & release (macOS style)
        KeyEvent deletePress = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.DELETE, false, false, false, false);
        KeyEvent deleteRelease = new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.DELETE, false, false, false, false);
        listener.getKeyPressedHandler().handle(deletePress);
        listener.getKeyReleasedHandler().handle(deleteRelease);

        BehavioralSession session = collector.getCurrentSession();
        List<KeyboardEventData> events = session.getKeyboardEvents();

        assertEquals(2, events.size(), "Both backspace and delete events should be recorded");
        assertTrue(events.get(0).isBackspace(), "BACK_SPACE key should be flagged as isBackspace");
        assertTrue(events.get(1).isBackspace(), "DELETE key (macOS Backspace) should be flagged as isBackspace");
    }
}
