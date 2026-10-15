package com.behavior.collector.model;

/**
 * Result of machine learning model inference on a FeatureVector.
 *
 * @param label        Predicted class label ("Genuine" or "Fraud")
 * @param isGenuine    True if classified as genuine human user
 * @param confidence   Confidence percentage of the prediction [0.0 - 100.0]
 * @param trustScore   Biometric trust score [0.0 - 100.0]
 * @param fraudScore   Fraud / anomaly probability score [0.0 - 100.0]
 */
public record BiometricPrediction(
        String label,
        boolean isGenuine,
        double confidence,
        double trustScore,
        double fraudScore
) {}
