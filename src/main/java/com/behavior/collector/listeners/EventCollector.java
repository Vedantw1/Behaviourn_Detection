package com.behavior.collector.listeners;

import com.behavior.collector.events.KeyboardEventData;
import com.behavior.collector.events.MouseEventData;
import com.behavior.collector.events.WindowEventData;
import com.behavior.collector.model.BehavioralSession;
import com.behavior.collector.model.SessionLabel;
import com.behavior.collector.util.LoggerUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

import com.github.kwhat.jnativehook.GlobalScreen;

import java.util.function.Consumer;

/**
 * Aggregator service responsible for controlling the recording lifecycle, managing raw event ingestion,
 * system-wide global key listening via JNativeHook, and maintaining the countdown timer.
 */
public class EventCollector {
    private BehavioralSession currentSession;
    private boolean recording = false;
    private int remainingSeconds = 30;
    private Timeline timeline;
    private GlobalKeyboardListener globalKeyboardListener;
    private GlobalMouseListener globalMouseListener;
    private boolean globalHookActive = false;

    private Consumer<Integer> timerTickListener;
    private Consumer<BehavioralSession> recordingCompletedListener;

    public EventCollector() {
        initGlobalHooks();
    }

    public synchronized void initGlobalHooks() {
        if (globalHookActive && GlobalScreen.isNativeHookRegistered()) return;
        try {
            // Disable verbose JNativeHook internal logger
            java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(java.util.logging.Level.OFF);
            logger.setUseParentHandlers(false);

            if (!GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.registerNativeHook();
            }

            if (globalKeyboardListener == null) {
                globalKeyboardListener = new GlobalKeyboardListener(this);
                GlobalScreen.addNativeKeyListener(globalKeyboardListener);
            }

            if (globalMouseListener == null) {
                globalMouseListener = new GlobalMouseListener(this);
                GlobalScreen.addNativeMouseListener(globalMouseListener);
                GlobalScreen.addNativeMouseMotionListener(globalMouseListener);
                GlobalScreen.addNativeMouseWheelListener(globalMouseListener);
            }

            globalHookActive = true;
            LoggerUtil.info(EventCollector.class, "Activated JNativeHook system-wide global keyboard and mouse listeners.");
        } catch (Throwable t) {
            globalHookActive = false;
            LoggerUtil.warning(EventCollector.class, "System-wide global hooks not available or macOS Accessibility permission denied: " + t.getMessage());
        }
    }

    public synchronized boolean isRecording() {
        return recording;
    }

    public boolean isGlobalHookActive() {
        return globalHookActive && GlobalScreen.isNativeHookRegistered();
    }

    public synchronized void startRecording(String userId, SessionLabel label, int sessionDurationSeconds) {
        if (recording) {
            LoggerUtil.warning(EventCollector.class, "Recording already in progress.");
            return;
        }

        this.currentSession = new BehavioralSession(userId, label);
        this.currentSession.startSession();
        this.recording = true;
        this.remainingSeconds = sessionDurationSeconds;

        if (!globalHookActive) {
            initGlobalHooks();
        }

        if (globalKeyboardListener != null) {
            globalKeyboardListener.reset();
        }

        if (timerTickListener != null) {
            timerTickListener.accept(remainingSeconds);
        }

        // Initialize JavaFX Timeline for 1-second countdown ticks
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            remainingSeconds--;
            if (timerTickListener != null) {
                timerTickListener.accept(remainingSeconds);
            }

            if (remainingSeconds <= 0) {
                stopRecording();
            }
        }));
        timeline.setCycleCount(sessionDurationSeconds);
        timeline.play();

        LoggerUtil.info(EventCollector.class, "Started behavioral recording session for user: " + userId);
    }

    public synchronized void stopRecording() {
        if (!recording) return;

        recording = false;
        if (timeline != null) {
            timeline.stop();
        }

        // Note: We deliberately do NOT unregister GlobalScreen on session stop because re-registering
        // on macOS terminates native threads and destroys event taps. The global listeners check
        // isRecording() and cleanly ignore events outside active recording sessions.

        if (currentSession != null) {
            currentSession.endSession();
            LoggerUtil.info(EventCollector.class, "Stopped behavioral recording session for user: " + currentSession.getUserId());
            BehavioralSession completedSession = currentSession;

            if (recordingCompletedListener != null) {
                Platform.runLater(() -> recordingCompletedListener.accept(completedSession));
            }
        }
    }

    public synchronized void shutdownGlobalHooks() {
        if (GlobalScreen.isNativeHookRegistered()) {
            try {
                if (globalKeyboardListener != null) {
                    GlobalScreen.removeNativeKeyListener(globalKeyboardListener);
                }
                if (globalMouseListener != null) {
                    GlobalScreen.removeNativeMouseListener(globalMouseListener);
                    GlobalScreen.removeNativeMouseMotionListener(globalMouseListener);
                    GlobalScreen.removeNativeMouseWheelListener(globalMouseListener);
                }
                GlobalScreen.unregisterNativeHook();
                globalHookActive = false;
                LoggerUtil.info(EventCollector.class, "Unregistered system-wide JNativeHook listeners.");
            } catch (Throwable t) {
                LoggerUtil.warning(EventCollector.class, "Error unregistering JNativeHook listener: " + t.getMessage());
            }
        }
    }

    public void recordMouseEvent(MouseEventData event) {
        if (recording && currentSession != null) {
            currentSession.addMouseEvent(event);
        }
    }

    public void recordKeyboardEvent(KeyboardEventData event) {
        if (recording && currentSession != null) {
            currentSession.addKeyboardEvent(event);
        }
    }

    public void recordWindowEvent(WindowEventData event) {
        if (recording && currentSession != null) {
            currentSession.addWindowEvent(event);
        }
    }

    public void setOnTimerTick(Consumer<Integer> listener) {
        this.timerTickListener = listener;
    }

    public void setOnRecordingCompleted(Consumer<BehavioralSession> listener) {
        this.recordingCompletedListener = listener;
    }

    public BehavioralSession getCurrentSession() {
        return currentSession;
    }
}
