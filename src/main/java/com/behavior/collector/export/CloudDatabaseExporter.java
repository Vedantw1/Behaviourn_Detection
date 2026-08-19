package com.behavior.collector.export;

import com.behavior.collector.model.FeatureVector;
import com.behavior.collector.util.LoggerUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cloud Database Exporter implementation for PostgreSQL / Supabase / Neon cloud databases.
 * Asynchronously persists FeatureVector rows to a central cloud database so team data collection is aggregated in real-time.
 */
public class CloudDatabaseExporter implements DatabaseExporter {

    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private boolean enabled = false;

    private static final String CREATE_TABLE_POSTGRES_SQL = """
            CREATE TABLE IF NOT EXISTS behavioral_data (
                id SERIAL PRIMARY KEY,
                user_id VARCHAR(100) NOT NULL,
                label VARCHAR(50) NOT NULL,
                avg_mouse_speed DOUBLE PRECISION NOT NULL,
                mouse_acceleration DOUBLE PRECISION NOT NULL,
                click_frequency DOUBLE PRECISION NOT NULL,
                scroll_speed DOUBLE PRECISION NOT NULL,
                avg_dwell_time DOUBLE PRECISION NOT NULL,
                avg_flight_time DOUBLE PRECISION NOT NULL,
                typing_speed DOUBLE PRECISION NOT NULL,
                backspace_count BIGINT NOT NULL,
                idle_time DOUBLE PRECISION NOT NULL,
                session_duration DOUBLE PRECISION NOT NULL,
                window_switch_count BIGINT NOT NULL,
                active_time_ratio DOUBLE PRECISION NOT NULL,
                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
            );
            """;

    private static final String INSERT_ROW_SQL = """
            INSERT INTO behavioral_data (
                user_id, label, avg_mouse_speed, mouse_acceleration, click_frequency,
                scroll_speed, avg_dwell_time, avg_flight_time, typing_speed,
                backspace_count, idle_time, session_duration, window_switch_count, active_time_ratio
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
            """;

    private final ExecutorService asyncExecutor = Executors.newSingleThreadExecutor();

    public CloudDatabaseExporter() {
        loadConfig();
    }

    public CloudDatabaseExporter(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.enabled = (dbUrl != null && !dbUrl.trim().isEmpty() && !dbUrl.contains("YOUR_CLOUD_DB_HOST"));
    }

    public void loadConfig() {
        File configFile = new File("db.properties");
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                Properties prop = new Properties();
                prop.load(input);

                String enabledProp = prop.getProperty("db.enabled", "false").trim();
                this.enabled = Boolean.parseBoolean(enabledProp);

                this.dbUrl = prop.getProperty("db.url", "").trim();
                this.dbUser = prop.getProperty("db.user", "").trim();
                this.dbPassword = prop.getProperty("db.password", "").trim();

                if (this.dbUrl.contains("YOUR_CLOUD_DB_HOST")) {
                    this.enabled = false;
                }
            } catch (Exception e) {
                LoggerUtil.warning(CloudDatabaseExporter.class, "Failed to load db.properties: " + e.getMessage());
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void exportRow(FeatureVector vector, File dummyFile) throws Exception {
        if (!enabled) {
            return;
        }

        if (vector == null) {
            throw new IllegalArgumentException("Vector must not be null");
        }

        // Asynchronously export to cloud DB to avoid blocking the JavaFX UI thread on network calls
        asyncExecutor.submit(() -> {
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                // Ensure table schema exists
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(CREATE_TABLE_POSTGRES_SQL);
                }

                // Insert feature row
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

                LoggerUtil.info(CloudDatabaseExporter.class, "Successfully exported feature vector for user '" + vector.userId() + "' to Cloud PostgreSQL Database.");
            } catch (Exception e) {
                LoggerUtil.error(CloudDatabaseExporter.class, "Failed to export feature vector to Cloud PostgreSQL Database", e);
            }
        });
    }
}
