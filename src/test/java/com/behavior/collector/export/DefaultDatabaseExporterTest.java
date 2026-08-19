package com.behavior.collector.export;

import com.behavior.collector.model.FeatureVector;
import com.behavior.collector.model.SessionLabel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultDatabaseExporterTest {

    @Test
    public void testExportRowCreatesTableAndInsertsData(@TempDir Path tempDir) throws Exception {
        File dbFile = tempDir.resolve("test_behavioral_data.db").toFile();
        DatabaseExporter exporter = new DefaultDatabaseExporter();

        FeatureVector dummyVector = new FeatureVector(
                "user_test",
                SessionLabel.GENUINE,
                250.5,
                110.2,
                1.5,
                0.5,
                120.0,
                180.0,
                4.5,
                2,
                1.2,
                30.0,
                1,
                0.96
        );

        // Execute export
        exporter.exportRow(dummyVector, dbFile);

        assertTrue(dbFile.exists(), "Database file should be created");
        assertTrue(dbFile.length() > 0, "Database file size should be greater than 0");

        // Query database to verify contents
        String jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM behavioral_data WHERE user_id = 'user_test'")) {

            assertTrue(rs.next(), "Row for user_test should exist in SQLite table");
            assertEquals("user_test", rs.getString("user_id"));
            assertEquals("Genuine", rs.getString("label"));
            assertEquals(250.5, rs.getDouble("avg_mouse_speed"), 0.001);
            assertEquals(110.2, rs.getDouble("mouse_acceleration"), 0.001);
            assertEquals(1.5, rs.getDouble("click_frequency"), 0.001);
            assertEquals(0.5, rs.getDouble("scroll_speed"), 0.001);
            assertEquals(120.0, rs.getDouble("avg_dwell_time"), 0.001);
            assertEquals(180.0, rs.getDouble("avg_flight_time"), 0.001);
            assertEquals(4.5, rs.getDouble("typing_speed"), 0.001);
            assertEquals(2, rs.getLong("backspace_count"));
            assertEquals(1.2, rs.getDouble("idle_time"), 0.001);
            assertEquals(30.0, rs.getDouble("session_duration"), 0.001);
            assertEquals(1, rs.getLong("window_switch_count"));
            assertEquals(0.96, rs.getDouble("active_time_ratio"), 0.001);
            assertNotNull(rs.getTimestamp("created_at"), "Created timestamp should be set");
        }
    }
}
