/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2026 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2026 The OpenNMS Group, Inc.
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

package org.opennms.smoketest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testcontainers.containers.Container;

/**
 * Sends script payloads in request parameters and makes sure that the pages do not run them.
 * Each payload sets the marker {@code window.__xss}. The test fails if a page sets the marker.
 */
public class ReflectedXssIT extends OpenNMSSeleniumIT {

    /** Breaks out of an HTML attribute or element. The payload has no digits, because some parameters remove digits only. */
    private static final String HTML = "\"><img src=x onerror=window.__xss=true>";

    /** An AngularJS expression. Pages with ng-app evaluate it even when the value is HTML-escaped. */
    private static final String NG = "{{constructor.constructor('window.__xss=true')()}}";

    @Test
    public void testGraphResults() throws Exception {
        // The zoom value goes into each graph container. A graph renders only for a resource with data.
        // Thus, create a node and an ICMP response time file for it. The fixture uses a TEST-NET-1 address
        // that no other test uses, so the cleanup does not remove other data.
        final String fixtureIp = "192.0.2.10";
        final String rrdDir = "/opt/opennms/share/rrd/response/" + fixtureIp;
        try {
            sendPost("rest/nodes", "<node type=\"A\" label=\"xssGraphNode\" foreignSource=\"smoketests\" foreignId=\"xssGraphNode\"/>", 201);
            sendPost("rest/nodes/smoketests:xssGraphNode/ipinterfaces",
                    "<ipInterface isManaged=\"M\" snmpPrimary=\"N\"><ipAddress>" + fixtureIp + "</ipAddress></ipInterface>", 201);
            final Container.ExecResult result = stack.opennms().execInContainer("sh", "-c",
                    "mkdir -p " + rrdDir + " && rrdtool create " + rrdDir + "/icmp.rrd --step 300 DS:icmp:GAUGE:600:U:U RRA:AVERAGE:0.5:1:12");
            assertEquals(result.getStderr(), 0, result.getExitCode());

            final String common = "&resourceId=" + enc("node[smoketests:xssGraphNode].responseTime[" + fixtureIp + "]") + "&reports=all";
            assertNoScriptRuns("graph/results.htm?zoom=" + enc(HTML) + common, ".graph-container[data-graph-zooming]");
            assertNoScriptRuns("graph/results.htm?zoom=" + enc(NG) + common, ".graph-container[data-graph-zooming]");
        } finally {
            stack.opennms().execInContainer("rm", "-rf", rrdDir);
            sendDelete("rest/nodes/smoketests:xssGraphNode", 202);
        }
    }

    @Test
    public void testChooseResource() {
        assertNoScriptRuns("graph/chooseresource.jsp?node=" + enc(NG) + "&reports=all");
        assertNoScriptRuns("graph/chooseresource.jsp?node=1&reports=all&endUrl=" + enc(NG));
    }

    @Test
    public void testNewInterface() {
        assertNoScriptRuns("admin/newInterface.jsp?action=redo&ipAddress=" + enc(HTML));
    }

    @Test
    public void testNewGroup() {
        assertNoScriptRuns("admin/userGroupView/groups/modifyGroup?operation=create&action=redo&groupName=" + enc(HTML));
    }

    @Test
    public void testNodeCategoryBox() {
        assertNoScriptRuns("includes/nodeCategory-box.htm?node=1" + enc(HTML));
    }

    @Test
    public void testHomePageMap() {
        assertNoScriptRuns("index.jsp?mapId=" + enc(HTML));
        assertNoScriptRuns("index.jsp?height=" + enc(NG));
        assertNoScriptRuns("geomap/standalone.jsp?hideControlsOnStartup=" + enc("window.__xss=true"));
    }

    @Test
    public void testStatusPage() {
        assertNoScriptRuns("status/index.jsp?title=" + enc(NG));
        assertNoScriptRuns("status/index.jsp?type=" + enc(NG));
        assertNoScriptRuns("status/index.jsp?title=" + enc("${param.x}") + "&x=" + enc(HTML));
    }

    @Test
    public void testEventList() {
        assertNoScriptRuns("event/list?display=" + enc(HTML));
    }

    @Test
    public void testPathOutageWizard() {
        assertNoScriptRuns("admin/notification/noticeWizard/buildPathOutage.jsp?mode=" + enc(HTML + "failed"));
    }

    @Test
    public void testRoleScheduleEntry() {
        // Equal start and end times select the error branch, which does not change the role.
        final String times = "&startMonth=1&startDate=1&startYear=2020&startHour=1&startMinute=0&startAmOrPm=AM"
                + "&endMonth=1&endDate=1&endYear=2020&endHour=1&endMinute=0&endAmOrPm=AM";
        assertNoScriptRuns("admin/userGroupView/roles?operation=saveEntry&save=1&role=x&timeIndex=0&schedIndex=0" + enc(HTML) + times);
    }

    @Test
    public void testSnmpInterfaces() {
        assertNoScriptRuns("admin/snmpInterfaces.jsp?node=1&nodelabel=" + enc(NG));
    }

    private void assertNoScriptRuns(final String path) {
        assertNoScriptRuns(path, null);
    }

    /**
     * @param sinkSelector a CSS selector for the element that contains the value, or null.
     *                     If it is set, the test makes sure that the page renders the element.
     *                     Otherwise, the test can pass because the page does not show the value.
     */
    private void assertNoScriptRuns(final String path, final String sinkSelector) {
        getDriver().get(getBaseUrlInternal() + "opennms/" + path);
        new WebDriverWait(getDriver(), Duration.ofSeconds(10))
                .until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
        if (sinkSelector != null) {
            assertFalse("The page does not render " + sinkSelector + ": " + path,
                    getDriver().findElements(By.cssSelector(sinkSelector)).isEmpty());
        }
        try {
            // Give AngularJS and image error handlers time to run.
            new WebDriverWait(getDriver(), Duration.ofSeconds(3))
                    .until(d -> Boolean.TRUE.equals(((JavascriptExecutor) d).executeScript("return window.__xss === true")));
            fail("Injected script ran on " + path);
        } catch (final TimeoutException e) {
            // Expected: the page did not run the payload.
        }
    }

    private static String enc(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
