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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
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

    private void assertLabelShownAsText(final String path) {
        getDriver().get(getBaseUrlInternal() + "opennms/" + path);
        new WebDriverWait(getDriver(), Duration.ofSeconds(10))
                .until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
        try {
            // Give AngularJS time to run.
            new WebDriverWait(getDriver(), Duration.ofSeconds(3))
                    .until(d -> Boolean.TRUE.equals(((JavascriptExecutor) d).executeScript("return window.__xss === true")));
            fail("AngularJS evaluated the node label on " + path);
        } catch (final TimeoutException e) {
            // Expected: the page did not run the expression.
        }
        // The page must show the label as text. Otherwise, the test can pass because the page does not show it.
        assertTrue("The page does not show the node label: " + path,
                Boolean.TRUE.equals(((JavascriptExecutor) getDriver()).executeScript("return document.body.textContent.indexOf(arguments[0]) >= 0", LABEL)));
    }
}
