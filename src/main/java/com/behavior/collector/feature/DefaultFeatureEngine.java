package com.behavior.collector.feature;

import com.behavior.collector.events.KeyboardEventData;
import com.behavior.collector.events.MouseEventData;
import com.behavior.collector.events.WindowEventData;
import com.behavior.collector.model.BehavioralSession;
import com.behavior.collector.model.FeatureVector;

import java.util.List;

/**
 * Production implementation of FeatureEngine combining helper calculators
 * to produce a FeatureVector containing exactly the 12 specified behavioral metrics.
 */
public class DefaultFeatureEngine implements FeatureEngine {

    @Override
    public FeatureVector extractFeatures(BehavioralSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null");
        }

        List<MouseEventData> mouseEvents = session.getMouseEvents();
        List<KeyboardEventData> keyboardEvents = session.getKeyboardEvents();
        List<WindowEventData> windowEvents = session.getWindowEvents();

        double sessionDuration = session.getDurationSeconds();
        if (sessionDuration <= 0) {
            sessionDuration = 30.0; // default fallback if timing delta is tiny
        }

        // 1. Average Mouse Speed
        double avgMouseSpeed = MouseFeatureCalculator.calculateAverageSpeed(mouseEvents, sessionDuration);

        // 2. Mouse Acceleration
        double mouseAcceleration = MouseFeatureCalculator.calculateMouseAcceleration(mouseEvents, sessionDuration);

        // 3. Click Frequency
        double clickFrequency = MouseFeatureCalculator.calculateClickFrequency(mouseEvents, sessionDuration);

        // 4. Scroll Speed
        double scrollSpeed = MouseFeatureCalculator.calculateScrollSpeed(mouseEvents, sessionDuration);

        // 5. Average Dwell Time
        double avgDwellTime = KeyboardFeatureCalculator.calculateAverageDwellTime(keyboardEvents);

        // 6. Average Flight Time
        double avgFlightTime = KeyboardFeatureCalculator.calculateAverageFlightTime(keyboardEvents);

        // 7. Typing Speed
        double typingSpeed = KeyboardFeatureCalculator.calculateTypingSpeed(keyboardEvents, sessionDuration);

        // 8. Backspace Count
        long backspaceCount = KeyboardFeatureCalculator.calculateBackspaceCount(keyboardEvents);

        // 9. Idle Time
        double idleTime = SessionFeatureCalculator.calculateIdleTime(
                mouseEvents, keyboardEvents, session.getStartTimeMillis(), session.getEndTimeMillis());

        // 10. Session Duration
        // (Exact duration in seconds)

        // 11. Window Switch Count
        long windowSwitchCount = SessionFeatureCalculator.calculateWindowSwitchCount(windowEvents);

        // 12. Active Time Ratio
        double activeTimeRatio = SessionFeatureCalculator.calculateActiveTimeRatio(sessionDuration, idleTime);

        return new FeatureVector(
                session.getUserId(),
                session.getLabel(),
                avgMouseSpeed,
                mouseAcceleration,
                clickFrequency,
                scrollSpeed,
                avgDwellTime,
                avgFlightTime,
                typingSpeed,
                backspaceCount,
                idleTime,
                sessionDuration,
                windowSwitchCount,
                activeTimeRatio
        );
    }
}
