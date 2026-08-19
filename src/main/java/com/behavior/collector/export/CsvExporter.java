package com.behavior.collector.export;

import com.behavior.collector.model.FeatureVector;

import java.io.File;

/**
 * Interface defining CSV persistence operations.
 */
public interface CsvExporter {

    /**
     * Appends a single FeatureVector row to the specified CSV file.
     * Creates the file and writes the CSV header if the file does not exist.
     *
     * @param vector FeatureVector to append
     * @param targetFile Target CSV file destination
     * @throws Exception If I/O write fails
     */
    void exportRow(FeatureVector vector, File targetFile) throws Exception;
}
