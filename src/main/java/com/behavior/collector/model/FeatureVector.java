package com.behavior.collector.model;

/**
 * Immutable vector containing the 12 calculated behavioral features for a session,
 * along with the User ID and Session Label metadata.
 *
 * @param userId            User identification string
 * @param label             Session label (Genuine or Fraud)
 * @param avgMouseSpeed     Average mouse speed in px/sec
 * @param mouseAcceleration Rate of change of mouse speed in px/sec²
 * @param clickFrequency    Total mouse clicks / session duration (clicks/sec)
 * @param scrollSpeed       Total scroll events / session duration (events/sec)
 * @param avgDwellTime      Average key press-to-release hold duration in milliseconds
 * @param avgFlightTime     Average duration between key release and next key press in milliseconds
 * @param typingSpeed       Total keystrokes / session duration (keys/sec)
 * @param backspaceCount    Total number of Backspace key presses
 * @param idleTime          Cumulative inactive duration in seconds (>1s threshold)
 * @param sessionDuration   Total duration of the recording session in seconds
 * @param windowSwitchCount Number of times window focus was lost
 * @param activeTimeRatio   Ratio of active time vs total session duration [0.0 - 1.0]
 */
public record FeatureVector(
        String userId,
        SessionLabel label,
        double avgMouseSpeed,
        double mouseAcceleration,
        double clickFrequency,
        double scrollSpeed,
        double avgDwellTime,
        double avgFlightTime,
        double typingSpeed,
        long backspaceCount,
        double idleTime,
        double sessionDuration,
        long windowSwitchCount,
        double activeTimeRatio
) {
    /**
     * Converts feature vector values into a CSV row matching the dataset header specification.
     *
     * @return Formatted CSV string
     */
    public String toCsvRow() {
        return String.format("%s,%s,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%d,%.4f,%.4f,%d,%.4f",
                userId,
                label.getDisplayName(),
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
