/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.jasper.measurement;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXB;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.google.common.io.ByteStreams;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.core.test.Level;
import org.opennms.core.test.LoggingEvent;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verifies that the {@link MeasurementQueryExecutor} works correctly.
 */
public class MeasurementQueryExecutorIT {

    private static final Logger LOG = LoggerFactory.getLogger(MeasurementQueryExecutorIT.class);

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");

    private interface ReportFiller {
        void fill(Map<String, Object> params) throws Exception;
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9999);

    private List<Request> requestList = new ArrayList<Request>();

    @After
    public void after() {
        try {
            LoggingEvent[] errorEvents = MockLogAppender.getEventsAtLevel(Level.ERROR);
            for (LoggingEvent eachEvent : errorEvents) {
                if ("net.sf.jasperreports.extensions.DefaultExtensionsRegistry".equals(eachEvent.getLoggerName())
                        && eachEvent.getMessage().contains("Error instantiating extensions registry")) {
                    Assert.fail("Jasper Report extensions not setup correctly. " +
                            "See http://jasperreports.sourceforge.net/api/net/sf/jasperreports/extensions/DefaultExtensionsRegistry.html for more details.");
                }
            }
        } finally {
            MockLogAppender.resetEvents();
        }
    }

    @Before
    public void before() throws IOException {
        requestList.clear();

        // we listen to all requests and verify that the POST data can be parsed as a Query Request
        wireMockRule.addMockServiceRequestListener(new RequestListener() {
            @Override
            public void requestReceived(Request request, Response response) {
                requestList.add(LoggedRequest.createFrom(request));
            }
        });

        // AllCharts Request 1
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/opennms/rest/measurements"))
                .withHeader("Accept", WireMock.equalTo("application/xml"))
                .withRequestBody(WireMock.matching(".*moCallAttempts.*"))
                .willReturn(WireMock.aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/xml")
                                .withBody(createBodyFrom("all-charts-1.xml"))
                ));

        // AllCharts Request 2
       WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/opennms/rest/measurements"))
                .withHeader("Accept", WireMock.equalTo("application/xml"))
                .withRequestBody(WireMock.matching(".*moSuccessRate.*"))
                .willReturn(WireMock.aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/xml")
                                .withBody(createBodyFrom("all-charts-2.xml"))
                ));

        // RrdGraph
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/opennms/rest/measurements"))
                .withHeader("Accept", WireMock.equalTo("application/xml"))
                .withRequestBody(WireMock.matching(".*http-8980.*"))
                .willReturn(WireMock.aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/xml")
                                .withBody(createBodyFrom("rrd-graph.xml"))
                ));

        // Forecast
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/opennms/rest/measurements"))
                .withHeader("Accept", WireMock.equalTo("application/xml"))
                .withRequestBody(WireMock.matching(".*FORECAST-RESOURCE-ID.*"))
                .willReturn(WireMock.aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/xml")
                                .withBody(createBodyFrom("forecast.xml"))
                ));

        // No data (404)
        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/opennms/rest/measurements"))
                .withHeader("Accept", WireMock.equalTo("application/xml"))
                .withRequestBody(WireMock.matching(".*http-9999.*"))
                .willReturn(WireMock.aResponse()
                        .withStatus(404)));

        // No data (empty)
        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/opennms/rest/measurements"))
                .withHeader("Accept", WireMock.equalTo("application/xml"))
                .withRequestBody(WireMock.matching(".*http-1234.*"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<query-response />") // minimal parsable
                ));

        // Everything else is automatically bound to a 404
    }

    private void verifyHttpCalls(int number) {
        // ensure a request was actually made and was only made <number> times
        WireMock.verify(number, WireMock.postRequestedFor(WireMock.urlMatching("/opennms/rest/measurements"))
                .withoutHeader("Authorization")
                .withHeader("Content-Type", WireMock.equalTo("application/xml")));

        // VERIFY that the Request Body is a valid QueryRequest
        Assert.assertEquals(number, requestList.size());
        for (Request eachRequest : requestList) {
            JAXB.unmarshal(new ByteArrayInputStream(eachRequest.getBody()), QueryRequest.class);
        }
    }

    private static String createBodyFrom(String filename) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteStreams.copy(MeasurementQueryExecutorIT.class.getResourceAsStream("/responses/" + filename), outputStream);
        return outputStream.toString();
    }

    @Before
    public void setUp() {
        new File("target/reports").mkdirs();
    }

    // The jrxml file contains a language="resourceQuery" statement, but is not supported anymore.
    // The test verifies that the appropriate exception is thrown.
    @Test
    public void testReportResourceTest() throws JRException, IOException {
        try {
            createReport("ResourceTest", new ReportFiller() {
                @Override
                public void fill(Map<String, Object> params) {
                    params.put("nodeid", 1);
                    params.put("resourceType", "nsVpnMonitor");
                }
            });
            Assert.fail("JRException expected, but not received");
        } catch (JRException ex) {
            Assert.assertTrue(ex.toString().contains("No query executer factory registered for the \"resourceQuery\" language."));
        }
        verifyHttpCalls(0);
    }

    @Test
    public void testReportAllCharts() throws IOException, JRException {
        createReport("AllChartsReport", new ReportFiller() {
            @Override
            public void fill(Map<String, Object> params) throws Exception {
                params.put("MEASUREMENT_URL", "http://localhost:9999/opennms/rest/measurements");
                params.put("startDate", String.valueOf(DATE_FORMAT.parse("Wed Aug 26 06:05:00 CEST 2015").getTime()));
                params.put("endDate",  String.valueOf(DATE_FORMAT.parse("Thu Aug 27 06:00:00 CEST 2015").getTime()));
            }
        });
        verifyHttpCalls(2);
    }

    @Test
    public void testReportRrdGraph() throws JRException, IOException {
        createReport("RrdGraph", new ReportFiller() {
            @Override
            public void fill(Map<String, Object> params) {
                params.put("MEASUREMENT_URL", "http://localhost:9999/opennms/rest/measurements");
                params.put("startDate", new Date("Wed Oct 13 17:25:00 EDT 2010").getTime());
                params.put("endDate", new Date("Wed Oct 13 21:16:30 EDT 2010").getTime());
            }
        });
        verifyHttpCalls(1);
    }

    @Test
    public void testReportNoDataEmptyResult() throws JRException, IOException {
        createReport("NoDataReport", new ReportFiller() {
            @Override
            public void fill(Map<String, Object> params) {
                params.put("MEASUREMENT_URL", "http://localhost:9999/opennms/rest/measurements");
                params.put("attribute1", "http-1234");
                params.put("attribute2", "ssh");
                params.put("startDate", new Date("Wed Oct 13 17:25:00 EDT 2010").getTime());
                params.put("endDate", new Date("Wed Oct 13 21:16:30 EDT 2010").getTime());
            }
        });
        verifyHttpCalls(1);
    }

    @Test
    public void testReportNoData404() throws JRException, IOException {
        createReport("NoDataReport", new ReportFiller() {
            @Override
            public void fill(Map<String, Object> params) {
                params.put("MEASUREMENT_URL", "http://localhost:9999/opennms/rest/measurements");
                params.put("attribute1", "http-9999");
                params.put("attribute2", "ssh");
                params.put("startDate", new Date("Wed Oct 13 17:25:00 EDT 2010").getTime());
                params.put("endDate", new Date("Wed Oct 13 21:16:30 EDT 2010").getTime());
            }
        });
        verifyHttpCalls(1);
    }

    // This test returns an empty data set because it uses a SQL query and we do not setup the database
    // However we keep this test for now.
    @Test
    public void testReportNodeAvailabilityMonthly() throws JRException, IOException {
        createReport("NodeAvailabilityMonthly", new ReportFiller() {
            @Override
            public void fill(Map<String, Object> params) {
            }
        });
        verifyHttpCalls(0);
    }

    @Test
    public void testReportHwForecast() throws IOException, JRException {
        createReport("Forecast", new ReportFiller() {
            @Override
            public void fill(Map<String, Object> params) throws Exception {
                params.put(JRParameter.IS_IGNORE_PAGINATION, true);
                params.put("MEASUREMENT_URL", "http://localhost:9999/opennms/rest/measurements");
                params.put("dsName", "ifInOctets");
                params.put("startDate", "1414602000000");
                params.put("endDate", "1417046400000");
            }
        });

        // Verify the results of the generated report
        Table<Integer, String, Double> forecasts = TreeBasedTable.create();

        FileReader reader = new FileReader(createFileName("Forecast", "csv"));
        CSVParser parser = new CSVParser(reader, CSVFormat.RFC4180.withHeader());
        int k = 0;
        for (CSVRecord record : parser) {
            try {
                Double fit = Double.parseDouble(record.get("HWFit"));
                Double lwr = Double.parseDouble(record.get("HWLwr"));
                Double upr = Double.parseDouble(record.get("HWUpr"));

                if(Double.isNaN(fit)) {
                    continue;
                }

                forecasts.put(k, "fit", fit);
                forecasts.put(k, "lwr", lwr);
                forecasts.put(k, "upr", upr);

                k++;
            } catch (NumberFormatException e) {
                // pass
            }
        }

        Assert.assertEquals(340, forecasts.rowKeySet().size());
        // First fitted value
        Assert.assertEquals(432.526086422424, forecasts.get(0, "fit"), 0.00001);
        // Last fitted value for which there is a known data point
        Assert.assertEquals(24079.4692522087, forecasts.get(327, "fit"), 0.00001);
        // First forecasted value
        Assert.assertEquals(22245.5417010936, forecasts.get(328, "fit"), 0.00001);
    }

    private void createReport(String reportName, ReportFiller filler) throws JRException, IOException {
        JasperReport jasperReport = JasperCompileManager.compileReport(getClass().getResourceAsStream("/reports/" + reportName + ".jrxml"));
        JasperPrint jasperPrint = fill(jasperReport, filler);
        createPdf(jasperPrint, reportName);
        createXhtml(jasperPrint, reportName);
        createCsv(jasperPrint, reportName);

        // this is ugly, but we verify that the reports exists and have a file size > 0
        verifyReport(reportName, "pdf");
        verifyReport(reportName, "html");
        verifyReport(reportName, "csv");
    }

    private void verifyReport(String reportName, String extension) throws IOException {
        final Path path = Paths.get(createFileName(reportName, extension));
        Assert.assertTrue(Files.exists(path));
        if (!"csv".equals(extension)) {
            Assert.assertTrue(Files.size(path) > 0);
        }
    }

    private static String createFileName(String reportName, String extension) {
        return String.format("target/reports/%s.%s", reportName, extension);
    }

    private JasperPrint fill(final JasperReport jasperReport, final ReportFiller filler) throws JRException {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            filler.fill(params);
            return JasperFillManager.fillReport(jasperReport, params);
        } catch (Exception ex) {
            if (ex instanceof JRException) {
                throw (JRException) ex;
            }
            throw new JRException(ex);
        }
    }

    private void createPdf(JasperPrint jasperPrint, String reportName) throws JRException {
        long start = System.currentTimeMillis();
        JasperExportManager.exportReportToPdfFile(jasperPrint, createFileName(reportName, "pdf"));
        LOG.info("PDF creation time: {} ms", (System.currentTimeMillis() - start));
    }

    private void createXhtml(JasperPrint jasperPrint, String reportName) throws JRException {
        long start = System.currentTimeMillis();
        JasperExportManager.exportReportToHtmlFile(jasperPrint, createFileName(reportName, "html"));
        LOG.info("XHTML creation time: {} ms", (System.currentTimeMillis() - start));
    }

    private void createCsv(JasperPrint jasperPrint, String reportName) throws JRException {
        long start = System.currentTimeMillis();

        SimpleExporterInput input = new SimpleExporterInput(jasperPrint);
        SimpleWriterExporterOutput output = new SimpleWriterExporterOutput(createFileName(reportName, "csv"));

        JRCsvExporter exporter = new JRCsvExporter();
        exporter.setExporterInput(input);
        exporter.setExporterOutput(output);
        exporter.exportReport();

        LOG.info("CSV creation time: {} ms", (System.currentTimeMillis() - start));
    }
}
