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
package org.opennms.smoketest.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.smoketest.OpenNMSSeleniumIT;
import org.opennms.smoketest.utils.RestClient;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Smoke tests for the Vue Node Availability page, which replaced the legacy availability JSP.
 *
 * The node has 27 managed interfaces, one more page than the default page size of 25 interfaces
 * needs, so pagination can be exercised. Each interface has an ICMP service; the first one also has
 * an HTTP service in the forced-unmanaged state, so the status label can be checked.
 */
public class NodeAvailabilityPageIT extends OpenNMSSeleniumIT {
    private static final String LABEL = "Test_Node_Availability_Page";
    private static final String NODE_CRITERIA = "smoketests:availabilityPage";
    private static final int INTERFACE_COUNT = 27;

    private int nodeId;

    @Before
    public void setUp() throws Exception {
        final String node = "<node type=\"A\" label=\"" + LABEL + "\" foreignSource=\"smoketests\" foreignId=\"availabilityPage\">" +
                "<labelSource>H</labelSource></node>";
        sendPost("rest/nodes", node, 201);

        for (int i = 1; i <= INTERFACE_COUNT; i++) {
            final String address = "10.20.0." + i;
            final String ipInterface = "<ipInterface isManaged=\"M\" snmpPrimary=\"N\"><ipAddress>" + address + "</ipAddress></ipInterface>";
            sendPost("rest/nodes/" + NODE_CRITERIA + "/ipinterfaces", ipInterface, 201);

            final String icmp = "<service status=\"A\"><serviceType id=\"1\"><name>ICMP</name></serviceType></service>";
            sendPost("rest/nodes/" + NODE_CRITERIA + "/ipinterfaces/" + address + "/services", icmp, 201);
        }

        // The service type is looked up by name, and created if it does not exist yet.
        final String http = "<service status=\"F\"><serviceType><name>HTTP</name></serviceType></service>";
        sendPost("rest/nodes/" + NODE_CRITERIA + "/ipinterfaces/10.20.0.1/services", http, 201);

        final RestClient restClient = stack.opennms().getRestClient();
        final OnmsNode saved = restClient.getNode(NODE_CRITERIA);
        assertEquals(LABEL, saved.getLabel());
        nodeId = saved.getId();
    }

    @After
    public void tearDown() throws Exception {
        sendDelete("rest/nodes/" + NODE_CRITERIA, 202);
    }

    private void openAvailabilityPage() {
        getDriver().get(getBaseUrlInternal() + "opennms/ui/index.html#/node/" + nodeId + "/availability");
        wait.until(pageContainsText("Availability for " + LABEL));
    }

    private int groupCount() {
        return getDriver().findElements(By.cssSelector("[data-test='timeline-interface-group']")).size();
    }

    @Test
    public void rendersTheAvailabilityPage() {
        openAvailabilityPage();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-test='availability-timeline']")));
        assertEquals("The availability page reported a load failure.",
                0, getDriver().findElements(By.cssSelector("[data-test='availability-error']")).size());
    }

    @Test
    public void paginatesInterfaces() {
        openAvailabilityPage();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-test='availability-timeline']")));
        assertEquals("The first page should hold the default 25 interfaces.", 25, groupCount());

        final WebElement next = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                "[data-test='availability-paginator'] button[aria-label='Next Page']")));
        next.click();

        wait.until(driver -> groupCount() == INTERFACE_COUNT - 25);
        assertEquals(INTERFACE_COUNT - 25, groupCount());
    }

    @Test
    public void showsForcedUnmanagedStatus() {
        openAvailabilityPage();

        // The HTTP service is on 10.20.0.1, which sorts first and so is on the first page.
        final WebElement row = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//tbody[@data-test='timeline-interface-group'][.//*[contains(text(),'10.20.0.1')]]" +
                "//tr[@data-test='timeline-service-row'][.//a[text()='HTTP']]")));
        assertTrue("Expected 'Forced Unmanaged' in the HTTP row but found: " + row.getText(),
                row.getText().contains("Forced Unmanaged"));
    }

    @Test
    public void reachedFromNodeDetails() {
        getDriver().get(getBaseUrlInternal() + "opennms/ui/index.html#/node/" + nodeId);
        wait.until(pageContainsText("Node Details for " + LABEL));

        final WebElement button = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-test='availability-details-button']")));
        button.click();

        wait.until(ExpectedConditions.urlContains("/node/" + nodeId + "/availability"));
        assertTrue(getDriver().getCurrentUrl().endsWith("/node/" + nodeId + "/availability"));
        wait.until(pageContainsText("Availability for " + LABEL));
    }

    /** The Monitoring menu of the links row links to this page (ui/#/node/&lt;id&gt;/availability under the base href). */
    @Test
    public void reachedFromNodeDetailsLinksRow() {
        new NodeDetailsPage(this).open(nodeId).waitForLabel(LABEL).clickLink("Monitoring", "Availability");

        wait.until(ExpectedConditions.urlContains("/ui/#/node/" + nodeId + "/availability"));
        assertTrue(getDriver().getCurrentUrl().endsWith("/node/" + nodeId + "/availability"));
        wait.until(pageContainsText("Availability for " + LABEL));
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-test='availability-timeline']")));
    }
}
