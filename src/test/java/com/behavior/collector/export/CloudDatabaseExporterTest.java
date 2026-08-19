package com.behavior.collector.export;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CloudDatabaseExporterTest {

    @Test
    public void testCloudDatabaseExporterDisabledWhenPlaceholderProvided() {
        CloudDatabaseExporter exporter = new CloudDatabaseExporter("jdbc:postgresql://YOUR_CLOUD_DB_HOST:5432/db", "user", "pass");
        assertFalse(exporter.isEnabled(), "Cloud exporter should be disabled if template placeholder URL is used");
    }

    @Test
    public void testCloudDatabaseExporterEnabledWithValidUrl() {
        CloudDatabaseExporter exporter = new CloudDatabaseExporter("jdbc:postgresql://localhost:5432/testdb", "user", "pass");
        assertTrue(exporter.isEnabled(), "Cloud exporter should be enabled when valid URL is provided");
    }
}
