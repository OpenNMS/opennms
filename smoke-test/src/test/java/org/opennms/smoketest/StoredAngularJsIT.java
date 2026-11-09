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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.junit.Test;
import org.opennms.smoketest.ui.NodeDetailsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testcontainers.containers.Container;

/**
 * Stores an AngularJS expression in a node label and makes sure that the pages show it as text.
 * A device can set the node label through its SNMP sysName. The expression sets the marker
 * {@code window.__xss}. The test fails if a page sets the marker.
 */
public class StoredAngularJsIT extends OpenNMSSeleniumIT {

    private static final String LABEL = "{{constructor.constructor('window.__xss=true')()}}";

    @Test
    public void testNodeLabelIsNotEvaluated() throws Exception {
        // The graph page shows the node label only for a resource with data.
        // The fixture uses a TEST-NET-1 address that no other test uses.
        final String fixtureIp = "192.0.2.12";
        final String rrdDir = "/opt/opennms/share/rrd/response/" + fixtureIp;
        try {
            sendPost("rest/nodes", "<node type=\"A\" label=\"" + LABEL + "\" foreignSource=\"smoketests\" foreignId=\"xssLabelNode\"/>", 201);
            sendPost("rest/nodes/smoketests:xssLabelNode/ipinterfaces",
                    "<ipInterface isManaged=\"M\" snmpPrimary=\"N\"><ipAddress>" + fixtureIp + "</ipAddress></ipInterface>", 201);
            final Container.ExecResult result = stack.opennms().execInContainer("sh", "-c",
                    "mkdir -p " + rrdDir + " && rrdtool create " + rrdDir + "/icmp.rrd --step 300 DS:icmp:GAUGE:600:U:U RRA:AVERAGE:0.5:1:12");
            assertEquals(result.getStderr(), 0, result.getExitCode());

            assertLabelShownAsText("element/node.jsp?node=smoketests:xssLabelNode");
            assertLabelShownAsText("graph/results.htm?reports=all&resourceId="
                    + URLEncoder.encode("node[smoketests:xssLabelNode].responseTime[" + fixtureIp + "]", StandardCharsets.UTF_8));
        } finally {
            stack.opennms().execInContainer("rm", "-rf", rrdDir);
            sendDelete("rest/nodes/smoketests:xssLabelNode", 202);
        }
    }

    @Test
    public void testIfAliasIsNotEvaluated() throws Exception {
        // A device sets ifAlias. The Node Details page shows it in the SNMP interfaces table, and the graph page shows it
        // in the label of the SNMP interface resource.
        final String fixtureIp = "192.0.2.13";
        final String ifName = "eth1";
        final String physAddr = "74d02b3f2671";
        String rrdDir = null;
        try {
            sendPost("rest/nodes", "<node type=\"A\" label=\"xssAliasNode\" foreignSource=\"smoketests\" foreignId=\"xssAliasNode\"/>", 201);
            sendPost("rest/nodes/smoketests:xssAliasNode/ipinterfaces",
                    "<ipInterface isManaged=\"M\" snmpPrimary=\"P\"><ipAddress>" + fixtureIp + "</ipAddress></ipInterface>", 201);
            sendPost("rest/nodes/smoketests:xssAliasNode/snmpinterfaces",
                    "<snmpInterface collectFlag=\"C\" ifIndex=\"1\" pollFlag=\"N\">"
                    + "<ifAlias>" + LABEL + "</ifAlias><ifDescr>" + ifName + "</ifDescr><ifName>" + ifName + "</ifName>"
                    + "<ifType>6</ifType><physAddr>" + physAddr + "</physAddr></snmpInterface>", 201);

            // The OpenNMS container stores RRD files by foreign source.
            rrdDir = "/opt/opennms/share/rrd/snmp/fs/smoketests/xssAliasNode/" + ifName + "-" + physAddr;
            final Container.ExecResult result = stack.opennms().execInContainer("sh", "-c",
                    "mkdir -p " + rrdDir
                    + " && rrdtool create " + rrdDir + "/ifInOctets.rrd --step 300 DS:ifInOctets:COUNTER:600:U:U RRA:AVERAGE:0.5:1:12"
                    + " && rrdtool create " + rrdDir + "/ifOutOctets.rrd --step 300 DS:ifOutOctets:COUNTER:600:U:U RRA:AVERAGE:0.5:1:12");
            assertEquals(result.getStderr(), 0, result.getExitCode());

            // The Node Details page must show the ifAlias in the SNMP interfaces table. Otherwise, the marker check proves nothing.
            final NodeDetailsPage nodePage = new NodeDetailsPage(this).open("smoketests:xssAliasNode");
            nodePage.showSnmpInterfaces();
            new WebDriverWait(getDriver(), Duration.ofSeconds(20))
                    .until(d -> d.findElements(By.cssSelector("[data-test='snmp-interfaces-table'] td")).stream()
                            .anyMatch(td -> LABEL.equals(td.getText())));
            assertLabelNotEvaluated("the Node Details page");

            assertLabelShownAsText("graph/results.htm?reports=all&resourceId="
                    + URLEncoder.encode("node[smoketests:xssAliasNode].interfaceSnmp[" + ifName + "-" + physAddr + "]", StandardCharsets.UTF_8));
            assertAddToKscShowsLabel();
        } finally {
            if (rrdDir != null) {
                stack.opennms().execInContainer("rm", "-rf", rrdDir);
            }
            sendDelete("rest/nodes/smoketests:xssAliasNode", 202);
        }
    }

    @Test
    public void testResourceIdIsNotEvaluated() throws Exception {
        // The hrStorageIndex resource type gets the resource name from hrStorageDescr, which a device sets.
        // Thus, the resource ID on the graph page can contain the expression.
        String rrdDir = null;
        try {
            sendPost("rest/nodes", "<node type=\"A\" label=\"xssResourceNode\" foreignSource=\"smoketests\" foreignId=\"xssResourceNode\"/>", 201);

            // The OpenNMS container stores RRD files by foreign source.
            rrdDir = "/opt/opennms/share/rrd/snmp/fs/smoketests/xssResourceNode/hrStorageIndex/" + LABEL;
            // Pass the directory as an argument, so that the shell does not read the quotes and parentheses in it.
            final Container.ExecResult result = stack.opennms().execInContainer("sh", "-c",
                    "mkdir -p \"$1\" && cd \"$1\" && printf 'hrStorageDescr=%s\\n' \"$2\" > strings.properties"
                    + " && for ds in hrStorageSize hrStorageUsed hrStorageAllocUnits; do"
                    + " rrdtool create $ds.rrd --step 300 DS:$ds:GAUGE:600:U:U RRA:AVERAGE:0.5:1:12 || exit 1; done",
                    "sh", rrdDir, LABEL);
            assertEquals(result.getStderr(), 0, result.getExitCode());

            assertLabelShownAsText("graph/results.htm?reports=all&resourceId="
                    + URLEncoder.encode("node[smoketests:xssResourceNode].hrStorageIndex[" + LABEL + "]", StandardCharsets.UTF_8));
            assertAddToKscShowsLabel();
        } finally {
            if (rrdDir != null) {
                stack.opennms().execInContainer("rm", "-rf", rrdDir);
            }
            sendDelete("rest/nodes/smoketests:xssResourceNode", 202);
        }
    }

    /**
     * Clicks the Add to KSC control on the graph page. AngularJS runs the ng-click expression, which contains
     * the encoded label. The modal must show the label as text, and the label must not run.
     */
    private void assertAddToKscShowsLabel() {
        new WebDriverWait(getDriver(), Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(".graph-aux-controls button[ng-click^='open(']")))
                .click();
        final WebElement resourceLabel = new WebDriverWait(getDriver(), Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(By.id("resourceLabel")));
        final String value = resourceLabel.getAttribute("value");
        assertTrue("The Add to KSC dialog does not show the label: " + value, value != null && value.contains(LABEL));
        assertLabelNotEvaluated("the Add to KSC dialog");
        getDriver().findElement(By.cssSelector(".modal-footer .btn-warning")).click();
    }

    /**
     * Waits for AngularJS on the current page, and fails if it evaluated the label.
     */
    private void assertLabelNotEvaluated(final String path) {
        try {
            new WebDriverWait(getDriver(), Duration.ofSeconds(3))
                    .until(d -> Boolean.TRUE.equals(((JavascriptExecutor) d).executeScript("return window.__xss === true")));
            fail("AngularJS evaluated the label on " + path);
        } catch (final TimeoutException e) {
            // Expected: the page did not run the expression.
        }
    }

    /**
     * Opens the page, waits until it shows the label as text, and fails if AngularJS evaluated the label.
     * If the page does not show the label, the marker check proves nothing. Thus, the label check is first.
     */
    private void assertLabelShownAsText(final String path) {
        getDriver().get(getBaseUrlInternal() + "opennms/" + path);
        try {
            new WebDriverWait(getDriver(), Duration.ofSeconds(10))
                    .until(d -> Boolean.TRUE.equals(((JavascriptExecutor) d).executeScript(
                            "return document.readyState === 'complete' && document.body.textContent.indexOf(arguments[0]) >= 0", LABEL)));
        } catch (final TimeoutException e) {
            fail("The page does not show the label: " + path);
        }
        assertLabelNotEvaluated(path);
    }
}
