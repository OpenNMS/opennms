/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.jasper.measurement;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.opennms.core.test.Level;
import org.opennms.core.test.LoggingEvent;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.measurements.api.QueryRequestValidator;
import org.opennms.netmgt.measurements.api.exceptions.ValidationException;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractMeasurementQueryExecutorTest {

    protected static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");

    protected interface ReportFiller {
        void fill(Map<String, Object> params) throws Exception;
    }

    // By default each request is automatically bound to 404
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9999);

    private final List<Request> requestList = new ArrayList<>();

    private final Logger LOG = LoggerFactory.getLogger(getClass());

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
            MockLogAppender.resetState();
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

        new File("target/reports").mkdirs();
    }

    protected String createBodyFrom(String filename) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteStreams.copy(MeasurementQueryExecutorRemoteIT.class.getResourceAsStream("/responses/" + filename), outputStream);
        return outputStream.toString();
    }

    protected void verifyHttpCalls(int number) {
        // ensure a request was actually made and was only made <number> times
        WireMock.verify(number, WireMock.postRequestedFor(WireMock.urlMatching("/opennms/rest/measurements"))
                .withoutHeader("Authorization")
                .withHeader("Content-Type", WireMock.equalTo("application/xml")));

        // VERIFY that the Request Body is a valid QueryRequest
        Assert.assertEquals(number, requestList.size());
        for (Request eachRequest : requestList) {
            QueryRequest queryRequest = JaxbUtils.unmarshal(QueryRequest.class, eachRequest.getBodyAsString());
            try {
                new QueryRequestValidator().validate(queryRequest);
            } catch (ValidationException e) {
                Throwables.propagate(e);
            }
        }
    }

    protected void createReport(String reportName, ReportFiller filler) throws JRException, IOException {
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

    protected void verifyReport(String reportName, String extension) throws IOException {
        final Path path = Paths.get(createFileName(reportName, extension));
        Assert.assertTrue(Files.exists(path));
        if (!"csv".equals(extension)) {
            Assert.assertTrue(Files.size(path) > 0);
        }
    }

    protected static String createFileName(String reportName, String extension) {
        return String.format("target/reports/%s.%s", reportName, extension);
    }

    protected JasperPrint fill(final JasperReport jasperReport, final ReportFiller filler) throws JRException {
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

    protected void createPdf(JasperPrint jasperPrint, String reportName) throws JRException {
        long start = System.currentTimeMillis();
        JasperExportManager.exportReportToPdfFile(jasperPrint, createFileName(reportName, "pdf"));
        LOG.info("PDF creation time: {} ms", (System.currentTimeMillis() - start));
    }

    protected void createXhtml(JasperPrint jasperPrint, String reportName) throws JRException {
        long start = System.currentTimeMillis();
        JasperExportManager.exportReportToHtmlFile(jasperPrint, createFileName(reportName, "html"));
        LOG.info("XHTML creation time: {} ms", (System.currentTimeMillis() - start));
    }

    protected void createCsv(JasperPrint jasperPrint, String reportName) throws JRException {
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
