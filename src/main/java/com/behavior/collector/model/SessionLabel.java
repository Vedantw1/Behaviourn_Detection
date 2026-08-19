package com.behavior.collector.model;

/**
 * Represents the label assigned to a behavioral recording session.
 */
public enum SessionLabel {
    GENUINE("Genuine"),
    FRAUD("Fraud");

    private final String displayName;

    SessionLabel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SessionLabel fromString(String label) {
        if (label == null) return GENUINE;
        for (SessionLabel sl : values()) {
            if (sl.name().equalsIgnoreCase(label) || sl.displayName.equalsIgnoreCase(label)) {
                return sl;
            }
        }
        return GENUINE;
    }
}
