package org.opennms.features.datachoices.internal.usagestatistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CsvLogger {

    private static final Logger LOG = LoggerFactory.getLogger(CsvLogger.class);

    private static final String OPENNMS_HOME = System.getProperty("opennms.home");
    public static final String OPEN_NMS_CSV_FILE_PATH = OPENNMS_HOME +"/etc/login_logs.csv";
    private static final String[] HEADER = {"Username", "Timestamp"};
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logToCsv(String username) {
        try {
            File csvFile = new File(OPEN_NMS_CSV_FILE_PATH);

            // Create the file and write the header if it doesn't exist
            if (!csvFile.exists()) {
                csvFile.getParentFile().mkdirs(); // Ensure the /etc folder exists
                try (FileWriter writer = new FileWriter(csvFile, true)) {
                    writer.append(String.join(",", HEADER)).append("\n");
                }
            }

            // Cleanup stale data before adding new data
            cleanupStaleData();

            // Append the new log entry to the CSV file
            try (FileWriter writer = new FileWriter(csvFile, true)) {
                String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
                String logEntry = String.join(",", username, timestamp);
                writer.append(logEntry).append("\n");
            }
        } catch (IOException e) {
            LOG.error("Failed to write log entry to CSV file", e);
        }
    }

    private static void cleanupStaleData() {
        File csvFile = new File(OPEN_NMS_CSV_FILE_PATH);

        if (!csvFile.exists()) {
            return; // No file to clean up
        }

        List<String> updatedEntries = new ArrayList<>();
        LocalDate sixtyDaysAgo = LocalDate.now().minusDays(60);

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean isFirstLine = true;

            // Read the CSV file line by line
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    updatedEntries.add(line); // Keep the header
                    isFirstLine = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 2) {
                    continue; // Skip malformed lines
                }

                // Parse the timestamp and check if it's within 60 days
                LocalDateTime entryTimestamp = LocalDateTime.parse(parts[1].trim(), TIMESTAMP_FORMATTER);
                if (!entryTimestamp.toLocalDate().isBefore(sixtyDaysAgo)) {
                    updatedEntries.add(line); // Keep valid entries
                }
            }

        } catch (IOException e) {
            LOG.error("Failed to read CSV file for cleanup", e);
        }

        // Write back the updated entries
        try (FileWriter writer = new FileWriter(csvFile, false)) { // Overwrite the file
            for (String entry : updatedEntries) {
                writer.append(entry).append("\n");
            }
        } catch (IOException e) {
            LOG.error("Failed to write cleaned-up data to CSV file", e);
        }
    }
}
