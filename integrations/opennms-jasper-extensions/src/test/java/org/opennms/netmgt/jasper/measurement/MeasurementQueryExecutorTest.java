package org.opennms.netmgt.jasper.measurement;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.io.ByteStreams;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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

/**
 * Verifies that the {@link MeasurementQueryExecutor} works correctly.
 */
public class MeasurementQueryExecutorTest {

    private static final Logger LOG = LoggerFactory.getLogger(MeasurementQueryExecutorTest.class);

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");

    private interface ReportFiller {
        void fill(Map<String, Object> params) throws Exception;
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9999);

    private List<Request> requestList = new ArrayList<Request>();

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
        ByteStreams.copy(MeasurementQueryExecutorTest.class.getResourceAsStream("/responses/" + filename), outputStream);
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
            Assert.assertTrue(ex.toString().contains("The provided language 'resourceQuery' is not supported"));
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

    @Test
    public void testReportNodeAvailabilityMonthly() throws JRException, IOException {
        createReport("NodeAvailabilityMonthly", new ReportFiller() {
            @Override
            public void fill(Map<String, Object> params) {
            }
        });
        verifyHttpCalls(0);
    }

    private void createReport(String reportName, ReportFiller filler) throws JRException, IOException {
        JasperReport jasperReport = JasperCompileManager.compileReport(getClass().getResourceAsStream("/reports/" + reportName + ".jrxml"));
        JasperPrint jasperPrint = fill(jasperReport, filler);
        createPdf(jasperPrint, reportName);
        createXhtml(jasperPrint, reportName);

        // this is ugly, but we verify that the reports exists and have a file size > 0
        verifyReport(reportName, "pdf");
        verifyReport(reportName, "html");
    }

    private void verifyReport(String reportName, String extension) throws IOException {
        final Path path = Paths.get(createFileName(reportName, extension));
        Assert.assertTrue(Files.exists(path));
        Assert.assertTrue(Files.size(path) > 0);

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
        File destFile = new File(createFileName(reportName, "html"));
        JasperExportManager.exportReportToHtmlFile(jasperPrint, createFileName(reportName, "html"));
        LOG.info("XHTML creation time: {} ms", (System.currentTimeMillis() - start));
    }

}
