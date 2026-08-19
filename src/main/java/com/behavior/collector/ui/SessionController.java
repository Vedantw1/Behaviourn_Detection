package com.behavior.collector.ui;

import com.behavior.collector.export.CloudDatabaseExporter;
import com.behavior.collector.export.CsvExporter;
import com.behavior.collector.export.DatabaseExporter;
import com.behavior.collector.export.DefaultCsvExporter;
import com.behavior.collector.export.DefaultDatabaseExporter;
import com.behavior.collector.feature.DefaultFeatureEngine;
import com.behavior.collector.feature.FeatureEngine;
import com.behavior.collector.listeners.EventCollector;
import com.behavior.collector.model.BehavioralSession;
import com.behavior.collector.model.FeatureVector;
import com.behavior.collector.model.SessionLabel;
import com.behavior.collector.util.LoggerUtil;

import java.io.File;

/**
 * Controller mediating interaction between the JavaFX presentation layer,
 * EventCollector, FeatureEngine, CsvExporter, DatabaseExporter, and CloudDatabaseExporter.
 */
public class SessionController {
    private final EventCollector collector;
    private final FeatureEngine featureEngine;
    private final CsvExporter csvExporter;
    private final DatabaseExporter databaseExporter;
    private final CloudDatabaseExporter cloudExporter;
    private File outputCsvFile = new File("behavioral_data.csv");
    private File outputDbFile = new File("behavioral_data.db");

    public SessionController(EventCollector collector) {
        this.collector = collector;
        this.featureEngine = new DefaultFeatureEngine();
        this.csvExporter = new DefaultCsvExporter();
        this.databaseExporter = new DefaultDatabaseExporter();
        this.cloudExporter = new CloudDatabaseExporter();
    }

    public void setOutputCsvFile(File file) {
        if (file != null) {
            this.outputCsvFile = file;
        }
    }

    public File getOutputCsvFile() {
        return outputCsvFile;
    }

    public void setOutputDbFile(File file) {
        if (file != null) {
            this.outputDbFile = file;
        }
    }

    public File getOutputDbFile() {
        return outputDbFile;
    }

    public void startRecording(String userId, SessionLabel label, int durationSeconds) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID must not be empty.");
        }
        collector.startRecording(userId.trim(), label, durationSeconds);
    }

    public void stopRecording() {
        collector.stopRecording();
    }

    public FeatureVector processCompletedSession(BehavioralSession session) {
        try {
            FeatureVector vector = featureEngine.extractFeatures(session);
            csvExporter.exportRow(vector, outputCsvFile);
            databaseExporter.exportRow(vector, outputDbFile);

            if (cloudExporter.isEnabled()) {
                cloudExporter.exportRow(vector, null);
            }

            return vector;
        } catch (Exception e) {
            LoggerUtil.error(SessionController.class, "Error processing completed session", e);
            throw new RuntimeException("Failed to export features to storage: " + e.getMessage(), e);
        }
    }
}
