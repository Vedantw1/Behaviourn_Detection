package com.behavior.collector.export;

import com.behavior.collector.model.FeatureVector;
import com.behavior.collector.util.LoggerUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Thread-safe SQLite Database Exporter implementation.
 * Automatically initializes database table schema if it does not exist and appends FeatureVector rows.
 */
public class DefaultDatabaseExporter implements DatabaseExporter {

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS behavioral_data (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT NOT NULL,
                label TEXT NOT NULL,
                avg_mouse_speed REAL NOT NULL,
                mouse_acceleration REAL NOT NULL,
                click_frequency REAL NOT NULL,
                scroll_speed REAL NOT NULL,
                avg_dwell_time REAL NOT NULL,
                avg_flight_time REAL NOT NULL,
                typing_speed REAL NOT NULL,
                backspace_count INTEGER NOT NULL,
                idle_time REAL NOT NULL,
                session_duration REAL NOT NULL,
                window_switch_count INTEGER NOT NULL,
                active_time_ratio REAL NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            """;

    private static final String INSERT_ROW_SQL = """
            INSERT INTO behavioral_data (
                user_id, label, avg_mouse_speed, mouse_acceleration, click_frequency,
                scroll_speed, avg_dwell_time, avg_flight_time, typing_speed,
                backspace_count, idle_time, session_duration, window_switch_count, active_time_ratio
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;

    @Override
    public synchronized void exportRow(FeatureVector vector, File dbFile) throws Exception {
        if (vector == null || dbFile == null) {
            throw new IllegalArgumentException("Vector and database file must not be null");
        }

        // Ensure parent directory exists
        File parentDir = dbFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        String jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
            // Ensure table schema exists
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(CREATE_TABLE_SQL);
            }

            // Insert feature row using prepared statement
            try (PreparedStatement pstmt = conn.prepareStatement(INSERT_ROW_SQL)) {
                pstmt.setString(1, vector.userId());
                pstmt.setString(2, vector.label().getDisplayName());
                pstmt.setDouble(3, vector.avgMouseSpeed());
                pstmt.setDouble(4, vector.mouseAcceleration());
                pstmt.setDouble(5, vector.clickFrequency());
                pstmt.setDouble(6, vector.scrollSpeed());
                pstmt.setDouble(7, vector.avgDwellTime());
                pstmt.setDouble(8, vector.avgFlightTime());
                pstmt.setDouble(9, vector.typingSpeed());
                pstmt.setLong(10, vector.backspaceCount());
                pstmt.setDouble(11, vector.idleTime());
                pstmt.setDouble(12, vector.sessionDuration());
                pstmt.setLong(13, vector.windowSwitchCount());
                pstmt.setDouble(14, vector.activeTimeRatio());

                pstmt.executeUpdate();
            }

            LoggerUtil.info(DefaultDatabaseExporter.class, "Appended feature row for user: " + vector.userId() + " to SQLite DB: " + dbFile.getName());
        } catch (Exception e) {
            LoggerUtil.error(DefaultDatabaseExporter.class, "Failed to export feature row to SQLite DB: " + dbFile.getAbsolutePath(), e);
            throw e;
        }
    }
}
