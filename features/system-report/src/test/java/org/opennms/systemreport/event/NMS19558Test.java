package org.opennms.systemreport.event;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.opennms.systemreport.event.CsvUtils.USER_LOGINS_CSV_HEADERS;

public class NMS19558Test {
    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setup() throws IOException {
        System.setProperty("opennms.home", folder.getRoot().getAbsolutePath());
        new File(folder.getRoot().getAbsolutePath()+"/etc/").mkdir();
        try (final CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(CsvUtils.USER_LOGINS_CSV_FILE_PATH), CSVFormat.DEFAULT.withHeader(USER_LOGINS_CSV_HEADERS.split(",")))) {
            for (int i = 0; i < 310; i++) {
                csvPrinter.printRecord("admin", "2025-01-01 20:00:00");
            }
            csvPrinter.printRecord("admin", CsvUtils.DATE_FORMAT.format(new Date()));
            csvPrinter.printRecord("admin", CsvUtils.DATE_FORMAT.format(new Date()));
            csvPrinter.printRecord("admin", CsvUtils.DATE_FORMAT.format(new Date()));
            csvPrinter.flush();
        }
    }

    @Test
    public void testEscape() throws IOException {
        CsvUtils.logUserDataToCsv("admin", Date.from(Instant.now()));
        assertFalse(systemOutRule.getLog().contains("java.text.ParseException"));
        assertEquals(5, Files.readAllLines(Path.of(CsvUtils.USER_LOGINS_CSV_FILE_PATH)).size());
    }
}
