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

import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.jasper.helper.MeasurementsHelper;

/**
 * Verifies that the {@link MeasurementQueryExecutor} works correctly when running not in jvm mode.
 * Verifies multiple reports.
 */
public class MeasurementQueryExecutorRemoteIT extends AbstractMeasurementQueryExecutorTest {

    @Before
    public void before() throws IOException {
        super.before();

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

        // Ensure that we are NOT running in jvm mode
        Assert.assertEquals(Boolean.FALSE, MeasurementsHelper.isRunInOpennmsJvm());
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
}
