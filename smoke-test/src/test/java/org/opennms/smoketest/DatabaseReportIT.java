/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.cxf.helpers.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;

import io.netty.handler.codec.http.HttpResponse;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.filters.ResponseFilter;
import net.lightbody.bmp.util.HttpMessageContents;
import net.lightbody.bmp.util.HttpMessageInfo;

/**
 * Verifies that the database reports can be generated without any exceptions.
 *
 * In order to check that the PDF report was generated we set up a proxy server and tunnel
 * all data through that proxy. A response filter is attached manually to watch out for "report download responses".
 * If such a response is detected, we hijack the response stream and save a copy of the PDF on disk.
 *
 * If we do not copy the response stream the result may be shown in the browser or downloaded (browser dependant).
 * In addition we do not have any possibilities to validate the result.
 *
 * While checking for responses we can verify the stream/result and also see if such a result is present.
 */
@RunWith(Parameterized.class)
public class DatabaseReportIT extends OpenNMSSeleniumTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseReportIT.class);

    private static BrowserMobProxy proxy;

    // Reports to verify
    @Parameterized.Parameters
    public static Object[] data() {
        return new Object[][] {
            {"EarlyMorningReport", "PDF", "Early morning report", 1},
            {"ResponseTimeSummaryForNode", "PDF", "Response Time Summary for node", 1},
            {"AvailabilityByNode", "PDF", "Availability by node", 1},
            {"AvailabilitySummaryPast7Days", "PDF", "Availability Summary -Default configuration for past 7 Days", 1},
            {"ResponseTimeByNode", "PDF", "Response time by node", 1},
            {"SerialInterfaceUtilizationSummary", "PDF", "Serial Interface Utilization Summary", 1},
            {"TotalBytesTransferredByInterface", "PDF", "Total Bytes Transferred by Interface ", 1},
            {"AverageAndPeakTrafficRatesForNodesByInterface", "PDF", "Average and Peak Traffic rates for Nodes by Interface", 1},
            {"InterfaceAvailabilityReport", "PDF", "Interface Availability Report", 2},
            {"SnmpInterfaceAvailabilityReport", "PDF", "Snmp Interface Availability Report", 2},
            {"MaintenanceContractsExpired", "PDF", "Maintenance contracts expired", 2},
            {"MaintenanceContractsStrategy", "PDF", "Maintenance contracts strategy", 2},
            {"EventAnalysisReport", "PDF", "Event Analysis report", 2},
        };
    }

    @AfterClass
    public static void afterClass() {
        proxy.stop(); // shutdown the proxy, we only need it for this test case
    }

    @Parameterized.Parameter(0)
    public String reportId;

    @Parameterized.Parameter(1)
    public String reportFormat;

    @Parameterized.Parameter(2)
    public String reportName;

    @Parameterized.Parameter(3)
    public int page;

    // setup proxy and response filter
    @Override
    protected void customizeCapabilities(DesiredCapabilities caps) {
        proxy = new BrowserMobProxyServer();
        proxy.start(0);

        proxy.addResponseFilter(new ResponseFilter() {
            @Override
            public void filterResponse(HttpResponse response, HttpMessageContents contents, HttpMessageInfo messageInfo) {
                if (isReportDownloadResponse(response)) {
                    LOG.info("Report download response received with headers: {}", response.headers().entries());

                    try (ByteArrayInputStream input = new ByteArrayInputStream(contents.getBinaryContents());
                         FileOutputStream output = new FileOutputStream(getFile())
                    ) {
                        ByteStreams.copy(input, output);
                    } catch (IOException e) {
                        Throwables.propagate(e);
                    }

                }
            }
        });

        // configure the Browser Mob Proxy as a desired capability
        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
        caps.setCapability(CapabilityType.PROXY, seleniumProxy);
    }

    @Before
    public void before() {
        new File("target/reports").mkdirs();

        // we do not want to wait 2 minutes, we only want to wait n seconds
        m_driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        LOG.info("Validate report generation '{}' ({})", reportName, reportFormat);

        Assert.assertNotNull(reportId);
        Assert.assertNotNull(reportFormat);
        Assert.assertNotNull(reportName);
        Assert.assertNotNull(page);
        Assert.assertFalse("Report '" + reportName + "' already exist", getFile().exists());

        LOG.info("Navigation to database reports page {}.", page);
        reportsPage();
        findElementByLink("Database Reports").click();
        findElementByLink("List reports").click();
        if (page > 1) {
            findElementByLink(Integer.toString(page)).click();
        }
    }

    @After
    public void after() {
        FileUtils.removeDir(new File("target/reports"));
    }

    @Test
    public void verifyReportExecution() {
        LOG.info("Verify report '{}'", reportName);

        // execute report (no custom parameter setup)
        getInstantExecutionLink(reportName).click();
        findElementById("run").click(); // run report

        verify();
    }

    private void verify() {
        // verify current page and look out for errors
        // we do not use findElementByXpath(...) on purpose, we explicitly want to use this
        // otherwise we have to wait 2 minutes each time an error already occurred
        List<WebElement> errorElements = m_driver.findElements(By.xpath("//div[@class=\"alert alert-danger\"]"));
        if (!errorElements.isEmpty()) {
            Assert.fail("An error occurred while generating the report: " + errorElements.get(0).getText());
        }

        // verify the file on disk (download might take a while, so we wait up to 2 minutes until the download or
        // the report generation has finished
        File file = getFile();
        long seconds = 120;
        int N = (int) Math.floor(seconds / 5);
        for (int i = 0; i < N && !file.exists(); i++) {
            LOG.info("Wait 5 seconds for the report to be generated/downloaded.");
            waitFor(5);
        }

        // ensure it really has been downloaded and has a file size > 0
        Assert.assertTrue("No report was generated for report '" + reportName + "'", file.exists());
        Assert.assertTrue("The report is empty", file.length() > 0);
    }

    // find the run report link
    private WebElement getInstantExecutionLink(String reportName) {
        WebElement element = findElementByXpath(String.format("//table/tbody/tr/td[text()='%s']/../td[3]/a", reportName));
        return element;
    }

    // is the resposne a download response?
    private boolean isReportDownloadResponse(HttpResponse response) {
        if (response.headers().contains("Content-disposition")
            && response.headers().contains("Content-Type")) {

            String contentType = response.headers().get("Content-Type");
            boolean isPdfOrCsvContentType = contentType.contains("application/pdf") ||contentType.contains("text/csv");

            String contentDisposition = response.headers().get("Content-disposition");
            boolean hasFilename = contentDisposition.contains("inline") && contentDisposition.contains("filename=");

            return isPdfOrCsvContentType && hasFilename;
        }
        return false;
    }

    private File getFile() {
        return new File(String.format("target/reports/%s.%s", reportId, reportFormat));
    }

}
