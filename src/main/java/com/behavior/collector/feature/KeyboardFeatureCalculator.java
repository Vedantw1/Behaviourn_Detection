package com.behavior.collector.feature;

import com.behavior.collector.events.KeyboardEventData;

import java.util.Comparator;
import java.util.List;

/**
 * Calculator helper for keyboard-related behavioral features.
 */
public class KeyboardFeatureCalculator {

    public static double calculateAverageDwellTime(List<KeyboardEventData> keyboardEvents) {
        if (keyboardEvents.isEmpty()) return 0.0;
        long totalDwell = 0;
        for (KeyboardEventData k : keyboardEvents) {
            totalDwell += k.dwellTime();
        }
        return (double) totalDwell / keyboardEvents.size();
    }

    public static double calculateAverageFlightTime(List<KeyboardEventData> keyboardEvents) {
        if (keyboardEvents.size() < 2) return 0.0;

        List<KeyboardEventData> sorted = keyboardEvents.stream()
                .sorted(Comparator.comparingLong(KeyboardEventData::pressTimestamp))
                .toList();

        double totalFlightTime = 0.0;
        int count = 0;

        for (int i = 1; i < sorted.size(); i++) {
            KeyboardEventData prev = sorted.get(i - 1);
            KeyboardEventData curr = sorted.get(i);
            long flight = curr.pressTimestamp() - prev.releaseTimestamp();
            // Ignore extreme gaps (e.g. > 10 seconds) between distinct typing bursts
            if (flight >= 0 && flight <= 10000) {
                totalFlightTime += flight;
                count++;
            }
        }

        return count > 0 ? totalFlightTime / count : 0.0;
    }

    public static double calculateTypingSpeed(List<KeyboardEventData> keyboardEvents, double sessionDuration) {
        if (sessionDuration <= 0) return 0.0;
        return keyboardEvents.size() / sessionDuration;
    }

    public static long calculateBackspaceCount(List<KeyboardEventData> keyboardEvents) {
        return keyboardEvents.stream()
                .filter(KeyboardEventData::isBackspace)
                .count();
    }
}
