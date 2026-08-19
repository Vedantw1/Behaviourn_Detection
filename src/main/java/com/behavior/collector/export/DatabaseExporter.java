package com.behavior.collector.export;

import com.behavior.collector.model.FeatureVector;

import java.io.File;

/**
 * Interface defining database persistence operations for behavioral feature vectors.
 */
public interface DatabaseExporter {

    /**
     * Inserts a single FeatureVector row into the specified SQLite database file.
     * Automatically creates the database table if it does not already exist.
     *
     * @param vector FeatureVector to insert
     * @param dbFile Target SQLite database file
     * @throws Exception If database connection or query execution fails
     */
    void exportRow(FeatureVector vector, File dbFile) throws Exception;
}
