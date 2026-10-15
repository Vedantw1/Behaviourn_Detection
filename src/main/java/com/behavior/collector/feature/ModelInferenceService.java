package com.behavior.collector.feature;

import com.behavior.collector.model.BiometricPrediction;
import com.behavior.collector.model.FeatureVector;
import com.behavior.collector.util.LoggerUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to execute real-time machine learning inference against the trained behavioral model.
 */
public class ModelInferenceService {

    private final String pythonExecutable;
    private final String predictScriptPath;

    public ModelInferenceService() {
        File venvPy = new File("ml/venv/bin/python");
        if (venvPy.exists()) {
            this.pythonExecutable = venvPy.getAbsolutePath();
        } else {
            this.pythonExecutable = "python3";
        }

        File scriptFile = new File("ml/predict.py");
        this.predictScriptPath = scriptFile.getAbsolutePath();
    }

    /**
     * Executes real-time model inference for a completed session's FeatureVector.
     *
     * @param vector Calculated FeatureVector for the session
     * @return BiometricPrediction with Trust Score and Genuine/Fraud label, or null if inference failed
     */
    public BiometricPrediction predict(FeatureVector vector) {
        if (vector == null) return null;

        try {
            String jsonPayload = vector.toJson();
            ProcessBuilder pb = new ProcessBuilder(
                    pythonExecutable,
                    predictScriptPath,
                    "--json",
                    jsonPayload
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                LoggerUtil.warning(ModelInferenceService.class, "Predict script failed with code " + exitCode + ": " + output);
                return null;
            }

            String jsonOut = output.toString().trim();
            String label = extractString(jsonOut, "label", "Unknown");
            boolean isGenuine = extractBoolean(jsonOut, "is_genuine", true);
            double confidence = extractDouble(jsonOut, "confidence", 50.0);
            double trustScore = extractDouble(jsonOut, "trust_score", 50.0);
            double fraudScore = extractDouble(jsonOut, "fraud_score", 50.0);

            BiometricPrediction prediction = new BiometricPrediction(label, isGenuine, confidence, trustScore, fraudScore);
            LoggerUtil.info(ModelInferenceService.class, String.format(
                    "Real-time prediction: %s (Trust Score: %.1f%%, Confidence: %.1f%%)",
                    label, trustScore, confidence
            ));
            return prediction;

        } catch (Exception e) {
            LoggerUtil.error(ModelInferenceService.class, "Error executing real-time biometric inference", e);
            return null;
        }
    }

    private String extractString(String json, String key, String defaultVal) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : defaultVal;
    }

    private boolean extractBoolean(String json, String key, boolean defaultVal) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(true|false)");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? Boolean.parseBoolean(matcher.group(1)) : defaultVal;
    }

    private double extractDouble(String json, String key, double defaultVal) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*([0-9.]+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return defaultVal;
    }
}
