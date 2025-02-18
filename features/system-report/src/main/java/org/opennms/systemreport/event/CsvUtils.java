package org.opennms.systemreport.event;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;


import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public class CsvUtils {
    private static final Logger LOG = LoggerFactory.getLogger(CsvUtils.class);
    private static final String OPENNMS_HOME = System.getProperty("opennms.home");
    public static final String USER_LOGINS_CSV_FILE_PATH = String.join("", OPENNMS_HOME, "/etc/user_logins.csv");
    private static final String  USER_NAME_CSV_HEADERS = "User Name";
    private static final String  LOGIN_TIME_CSV_HEADERS = "Login Time";
    private static final String  USER_LOGINS_CSV_HEADERS = String.join(",", USER_NAME_CSV_HEADERS, LOGIN_TIME_CSV_HEADERS);
    private static final Integer USER_LOGIN_THRESHOLD_DAYS= 60;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public static Resource readCsvHeaders(String filePath) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Reader reader = new FileReader(filePath, StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            String[] headers = csvParser.getHeaderMap().keySet().toArray(new String[0]);
            try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {

                csvPrinter.printRecord((Object[]) headers);
                csvPrinter.flush();
            }
        }

        return new ByteArrayResource(outputStream.toByteArray());
    }

    public static Resource readCsvData(String filePath, boolean skipHeaders) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Reader reader = new FileReader(filePath, StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT)) {
            try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
                boolean isFirstRow = true;
                for (CSVRecord record : csvParser) {
                    if (skipHeaders && isFirstRow) {
                        isFirstRow = false;
                        continue;
                    }
                    csvPrinter.printRecord(record);
                }
                csvPrinter.flush();
            }
        }
        return new ByteArrayResource(outputStream.toByteArray());

    }

    public static void writeDataToFile(Resource csvHeaders, Resource csvData, String filePath) throws IOException {

        File csvFile = new File(filePath);
        boolean isNewFile = !csvFile.exists() && csvFile.createNewFile();
        try (FileOutputStream fileOutputStream = new FileOutputStream(csvFile, true)) {
            writeDataToStream(csvHeaders, csvData, fileOutputStream, isNewFile);
        }
    }

    public static void writeDataToStream(Resource csvHeaders, Resource csvData, OutputStream outputStream,boolean writeHeaders) throws IOException {

        CSVFormat csvFormat = CSVFormat.DEFAULT;
        String[] headers = null;

        if (writeHeaders && csvHeaders != null && csvHeaders.contentLength() > 0) {
            try (InputStream headerStream = csvHeaders.getInputStream()) {
                headers = StreamUtils.copyToString(headerStream, StandardCharsets.UTF_8).trim().split(",");
                csvFormat = CSVFormat.DEFAULT.withHeader(headers);
            }
        }

        //Writer and CSVPrinter are outside the try that prevents to close stream.
        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);
        try (
                InputStream dataStream = csvData.getInputStream();
                Reader reader = new InputStreamReader(dataStream, StandardCharsets.UTF_8);
                CSVParser csvParser = new CSVParser(reader, csvFormat)
        ) {

            for (CSVRecord record : csvParser) {
                csvPrinter.printRecord(record);
            }
            csvPrinter.flush();
        }
    }

    public static void logUserDataToCsv(String username, Date loginTime) {

        try {

            String data = String.join(",",username,DATE_FORMAT.format(loginTime));
            Resource csvHeaders = new ByteArrayResource(USER_LOGINS_CSV_HEADERS.getBytes());
            Resource csvData = new ByteArrayResource(data.getBytes());
            writeDataToFile(csvHeaders, csvData, USER_LOGINS_CSV_FILE_PATH);
            removeOldRecordsFromCsv();

        } catch (IOException | ParseException e ) {
            LOG.error(e.getMessage(),e);
        }


    }

    public static void removeOldRecordsFromCsv() throws IOException, ParseException {

        File csvFile = new File(USER_LOGINS_CSV_FILE_PATH);
        if (!csvFile.exists() || csvFile.length() == 0) {
            throw new IOException("CSV file does not exist or is empty.");
        }

        try (
                InputStream inputStream = new FileInputStream(csvFile);
                InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
                FileOutputStream outputStream = new FileOutputStream(csvFile, false); // Overwrite mode
                OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(USER_LOGINS_CSV_HEADERS.split(",")));
        ) {

            for (CSVRecord record : csvParser) {
                String timestampStr = record.get(LOGIN_TIME_CSV_HEADERS);
                Date recordDate = DATE_FORMAT.parse(timestampStr);
                if (!isOlderThanThreshold(recordDate,USER_LOGIN_THRESHOLD_DAYS)) {
                    csvPrinter.printRecord(record.get(USER_NAME_CSV_HEADERS), record.get(LOGIN_TIME_CSV_HEADERS));
                }
                csvPrinter.flush();
            }
        }

    }

    private static boolean isOlderThanThreshold(Date recordTime, Integer thresholdDays) {
        Instant now = Instant.now();
        Instant recordInstant = recordTime.toInstant();
        Duration duration = Duration.between(recordInstant, now);
        return duration.toDays() > thresholdDays;
    }

}