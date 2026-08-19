package com.behavior.collector.feature;

import com.behavior.collector.events.MouseEventData;
import com.behavior.collector.events.MouseEventType;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculator helper for mouse-related behavioral features.
 */
public class MouseFeatureCalculator {

    public static double calculateAverageSpeed(List<MouseEventData> mouseEvents, double sessionDuration) {
        if (mouseEvents.isEmpty() || sessionDuration <= 0) return 0.0;

        List<MouseEventData> moves = mouseEvents.stream()
                .filter(e -> e.eventType() == MouseEventType.MOVE)
                .toList();

        if (moves.size() < 2) return 0.0;

        double totalDistance = 0.0;
        for (int i = 1; i < moves.size(); i++) {
            MouseEventData p1 = moves.get(i - 1);
            MouseEventData p2 = moves.get(i);
            double dx = p2.x() - p1.x();
            double dy = p2.y() - p1.y();
            totalDistance += Math.hypot(dx, dy);
        }

        return totalDistance / sessionDuration;
    }

    public static double calculateMouseAcceleration(List<MouseEventData> mouseEvents, double sessionDuration) {
        if (mouseEvents.isEmpty() || sessionDuration <= 0) return 0.0;

        List<MouseEventData> moves = mouseEvents.stream()
                .filter(e -> e.eventType() == MouseEventType.MOVE)
                .toList();

        if (moves.size() < 3) return 0.0;

        List<Double> speeds = new ArrayList<>();
        List<Double> timeDeltas = new ArrayList<>();

        for (int i = 1; i < moves.size(); i++) {
            MouseEventData p1 = moves.get(i - 1);
            MouseEventData p2 = moves.get(i);
            double dt = (p2.timestamp() - p1.timestamp()) / 1000.0;
            if (dt > 0.001) { // minimum 1ms delta
                double dist = Math.hypot(p2.x() - p1.x(), p2.y() - p1.y());
                speeds.add(dist / dt);
                timeDeltas.add(dt);
            }
        }

        if (speeds.size() < 2) return 0.0;

        double totalAcc = 0.0;
        int count = 0;
        for (int i = 1; i < speeds.size(); i++) {
            double dv = speeds.get(i) - speeds.get(i - 1);
            double dt = timeDeltas.get(i);
            if (dt > 0.001) {
                totalAcc += Math.abs(dv / dt);
                count++;
            }
        }

        return count > 0 ? totalAcc / count : 0.0;
    }

    public static double calculateClickFrequency(List<MouseEventData> mouseEvents, double sessionDuration) {
        if (sessionDuration <= 0) return 0.0;
        long clicks = mouseEvents.stream()
                .filter(e -> e.eventType() == MouseEventType.LEFT_CLICK || e.eventType() == MouseEventType.RIGHT_CLICK)
                .count();
        return clicks / sessionDuration;
    }

    public static double calculateScrollSpeed(List<MouseEventData> mouseEvents, double sessionDuration) {
        if (sessionDuration <= 0) return 0.0;
        long scrolls = mouseEvents.stream()
                .filter(e -> e.eventType() == MouseEventType.SCROLL)
                .count();
        return scrolls / sessionDuration;
    }
}
