package com.behavior.collector.export;

import com.behavior.collector.model.FeatureVector;
import com.behavior.collector.util.LoggerUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Thread-safe CSV Exporter implementation that writes the standard dataset header on creation
 * and appends single FeatureVector rows per completed session.
 */
public class DefaultCsvExporter implements CsvExporter {

    public static final String CSV_HEADER =
            "UserID,Label,AvgMouseSpeed,MouseAcceleration,ClickFrequency,ScrollSpeed," +
            "AvgDwellTime,AvgFlightTime,TypingSpeed,BackspaceCount,IdleTime,SessionDuration," +
            "WindowSwitchCount,ActiveTimeRatio";

    @Override
    public synchronized void exportRow(FeatureVector vector, File targetFile) throws Exception {
        if (vector == null || targetFile == null) {
            throw new IllegalArgumentException("Vector and target file must not be null");
        }

        boolean isNewFile = !targetFile.exists() || targetFile.length() == 0;

        // Ensure parent directories exist
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(targetFile, true)))) {
            if (isNewFile) {
                writer.println(CSV_HEADER);
                LoggerUtil.info(DefaultCsvExporter.class, "Created CSV file with header: " + targetFile.getAbsolutePath());
            }
            writer.println(vector.toCsvRow());
            writer.flush();
            LoggerUtil.info(DefaultCsvExporter.class, "Appended feature row for user: " + vector.userId() + " to " + targetFile.getName());
        } catch (Exception e) {
            LoggerUtil.error(DefaultCsvExporter.class, "Failed to export feature row to CSV: " + targetFile.getAbsolutePath(), e);
            throw e;
        }
    }
}
