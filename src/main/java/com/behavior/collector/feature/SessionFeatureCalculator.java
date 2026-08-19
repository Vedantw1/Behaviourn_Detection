package com.behavior.collector.feature;

import com.behavior.collector.events.KeyboardEventData;
import com.behavior.collector.events.MouseEventData;
import com.behavior.collector.events.WindowEventData;
import com.behavior.collector.events.WindowFocusState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Calculator helper for session-level behavioral features including idle time,
 * window switch counts, and active time ratio.
 */
public class SessionFeatureCalculator {

    private static final double IDLE_THRESHOLD_SECONDS = 1.0;

    public static double calculateIdleTime(List<MouseEventData> mouseEvents,
                                           List<KeyboardEventData> keyboardEvents,
                                           long startTimeMillis,
                                           long endTimeMillis) {
        if (endTimeMillis <= startTimeMillis) return 0.0;

        List<Long> actionTimestamps = new ArrayList<>();
        actionTimestamps.add(startTimeMillis);

        for (MouseEventData m : mouseEvents) {
            actionTimestamps.add(m.timestamp());
        }
        for (KeyboardEventData k : keyboardEvents) {
            actionTimestamps.add(k.pressTimestamp());
            actionTimestamps.add(k.releaseTimestamp());
        }
        actionTimestamps.add(endTimeMillis);

        Collections.sort(actionTimestamps);

        double totalIdleSeconds = 0.0;
        for (int i = 1; i < actionTimestamps.size(); i++) {
            long prev = actionTimestamps.get(i - 1);
            long curr = actionTimestamps.get(i);
            double gapSeconds = (curr - prev) / 1000.0;
            if (gapSeconds >= IDLE_THRESHOLD_SECONDS) {
                totalIdleSeconds += gapSeconds;
            }
        }

        double totalSessionDuration = (endTimeMillis - startTimeMillis) / 1000.0;
        return Math.min(totalIdleSeconds, totalSessionDuration);
    }

    public static long calculateWindowSwitchCount(List<WindowEventData> windowEvents) {
        if (windowEvents.isEmpty()) return 0;
        return windowEvents.stream()
                .filter(w -> w.state() == WindowFocusState.LOST)
                .count();
    }

    public static double calculateActiveTimeRatio(double sessionDuration, double idleTime) {
        if (sessionDuration <= 0) return 0.0;
        double activeTime = Math.max(0.0, sessionDuration - idleTime);
        double ratio = activeTime / sessionDuration;
        return Math.min(1.0, Math.max(0.0, ratio));
    }
}
