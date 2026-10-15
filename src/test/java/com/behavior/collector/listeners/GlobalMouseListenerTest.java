package com.behavior.collector.listeners;

import com.behavior.collector.events.MouseEventData;
import com.behavior.collector.events.MouseEventType;
import com.behavior.collector.model.SessionLabel;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseWheelEvent;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalMouseListenerTest {

    @BeforeAll
    public static void initJavaFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
            latch.await();
        } catch (IllegalStateException e) {
            // Already initialized
        }
    }

    @Test
    public void testGlobalNativeMouseEventsRecorded() {
        EventCollector collector = new EventCollector();
        collector.startRecording("global_mouse_user", SessionLabel.GENUINE, 30);

        GlobalMouseListener listener = new GlobalMouseListener(collector);

        // 1. Move event
        NativeMouseEvent moveEvent = new NativeMouseEvent(
                NativeMouseEvent.NATIVE_MOUSE_MOVED,
                0, 200, 350, 0, NativeMouseEvent.NOBUTTON
        );
        listener.nativeMouseMoved(moveEvent);

        // 2. Click event
        NativeMouseEvent clickEvent = new NativeMouseEvent(
                NativeMouseEvent.NATIVE_MOUSE_CLICKED,
                0, 250, 400, 1, NativeMouseEvent.BUTTON1
        );
        listener.nativeMouseClicked(clickEvent);

        // 3. Scroll event
        NativeMouseWheelEvent wheelEvent = new NativeMouseWheelEvent(
                NativeMouseWheelEvent.NATIVE_MOUSE_WHEEL,
                0, 250, 400, 0,
                NativeMouseWheelEvent.WHEEL_UNIT_SCROLL,
                3, 1, NativeMouseWheelEvent.WHEEL_VERTICAL_DIRECTION
        );
        listener.nativeMouseWheelMoved(wheelEvent);

        collector.stopRecording();

        List<MouseEventData> events = collector.getCurrentSession().getMouseEvents();
        assertEquals(3, events.size(), "Should have recorded move, click, and scroll events");
        assertEquals(MouseEventType.MOVE, events.get(0).eventType());
        assertEquals(MouseEventType.LEFT_CLICK, events.get(1).eventType());
        assertEquals(MouseEventType.SCROLL, events.get(2).eventType());
    }
}
