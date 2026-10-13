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

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.smoketest.OpenNMSSeleniumIT;
import org.opennms.smoketest.utils.RestClient;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Entry points that link to the Vue Node Details page (NMS-20412): the route itself, the Vue node
 * list redirect, the home page quick search, and a sample of legacy pages.
 */
public class NodeDetailsLinksIT extends OpenNMSSeleniumIT {
    private static final String LABEL = "Test_Node_Links";
    private int nodeId;

    @Before
    public void setUp() throws Exception {
        final RestClient restClient = stack.opennms().getRestClient();

        OnmsNode node = new OnmsNode();
        node.setLabel(LABEL);
        node.setType(OnmsNode.NodeType.ACTIVE);
        node.setForeignSource("test");
        node.setForeignId("links1");
        final Response response = restClient.addNode(node);
        assertEquals(201, response.getStatus());
        nodeId = restClient.getNode("test:links1").getId();

        sendPost("rest/nodes/test:links1/ipinterfaces",
                "<ipInterface isManaged=\"M\" snmpPrimary=\"P\"><ipAddress>10.10.10.10</ipAddress></ipInterface>", 201);
    }

    @After
    public void tearDown() throws Exception {
        sendDelete("rest/nodes/test:links1", 202);
    }

    private void waitForVueNodeDetails() {
        wait.until(ExpectedConditions.urlContains("#/node/" + nodeId));
        wait.until(pageContainsText("Node Details for " + LABEL));
    }

    /**
     * Opens a legacy list page and waits until it shows a link to the Vue Node Details page.
     * The event and alarm only show up after eventd and alarmd processed the event, so the page is
     * reloaded until the link is there.
     */
    private void waitForNodeLinkOn(final String path) {
        final By link = By.xpath("//a[contains(@href, '#/node/" + nodeId + "')]");
        getDriver().get(getBaseUrlInternal() + path);
        try {
            setImplicitWait(1, TimeUnit.SECONDS);
            wait.until(driver -> {
                if (driver.findElements(link).isEmpty()) {
                    sleepQuietly(2000);
                    driver.navigate().refresh();
                    return false;
                }
                return true;
            });
        } finally {
            setImplicitWait();
        }
        clickElement(link);
    }

    // --- the route ---

    @Test
    public void routeTakesTheNodeId() {
        getDriver().get(getBaseUrlInternal() + "opennms/ui/index.html#/node/" + nodeId);
        waitForVueNodeDetails();
    }

    @Test
    public void routeResolvesForeignSourceAndId() {
        getDriver().get(getBaseUrlInternal() + "opennms/ui/index.html#/node/test:links1");
        waitForVueNodeDetails();
    }

    @Test
    public void routeShowsNodeNotFound() {
        getDriver().get(getBaseUrlInternal() + "opennms/ui/index.html#/node/test:no-such-node");
        wait.until(pageContainsText("Node Not Found"));
    }

    @Test
    public void routeShowsInvalidNodeId() {
        getDriver().get(getBaseUrlInternal() + "opennms/ui/index.html#/node/abc");
        wait.until(pageContainsText("Invalid Node ID"));
    }

    // --- Vue entry points ---

    @Test
    public void nodeListNodeIdParamRedirects() {
        getDriver().get(getBaseUrlInternal() + "opennms/ui/index.html#/nodes?nodeId=" + nodeId);
        waitForVueNodeDetails();
    }

    @Test
    public void quickSearchOpensTheNode() {
        getDriver().get(getBaseUrlInternal() + "opennms/index.jsp");
        enterText(By.id("nodeId"), "test:links1");
        clickElement(By.name("nodeIdSearchButton"));
        waitForVueNodeDetails();
    }

    @Test
    public void nodeDetailsLinksToNotificationsForTheNode() {
        getDriver().get(getBaseUrlInternal() + "opennms/ui/index.html#/node/" + nodeId);
        waitForVueNodeDetails();
        clickElement(By.cssSelector("[data-test='all-outstanding-notifications-link']"));
        wait.until(ExpectedConditions.urlContains("preset=allOutstanding"));
        wait.until(ExpectedConditions.urlContains("node=" + nodeId));
        wait.until(pageContainsText("Node: " + LABEL));
    }

    // --- legacy entry points ---

    @Test
    public void interfacePageBreadcrumbOpensTheNode() {
        getDriver().get(getBaseUrlInternal() + "opennms/element/interface.jsp?node=" + nodeId + "&intf=10.10.10.10");
        clickElement(By.xpath("//ol[@class='breadcrumb']//a[text()='Node']"));
        waitForVueNodeDetails();
    }

    @Test
    public void nodeLabelPageLinksToTheNode() {
        getDriver().get(getBaseUrlInternal() + "opennms/admin/nodelabel.jsp?node=" + nodeId);
        clickElement(By.xpath("//a[@title='More information for this node']"));
        waitForVueNodeDetails();
    }

    @Test
    public void eventListLinksToTheNode() throws Exception {
        sendNodeDownEvent();
        waitForNodeLinkOn("opennms/event/list?filter=node%3D" + nodeId);
        waitForVueNodeDetails();
    }

    @Test
    public void alarmListLinksToTheNode() throws Exception {
        sendNodeDownEvent();
        waitForNodeLinkOn("opennms/alarm/list.htm?filter=node%3D" + nodeId);
        waitForVueNodeDetails();
    }

    @Test
    public void centralSearchResultOpensTheNode() {
        getDriver().get(getBaseUrlInternal() + "opennms/index.jsp");
        findElementByXpath("//div[@id='onms-central-search-control']//input[@aria-label='Search']").sendKeys(LABEL);
        clickElement(By.xpath("//div[@id='onms-central-search-control']//div[contains(@class, 'search-result-item')][contains(., '" + LABEL + "')]//button[contains(@class, 'search-result-button')]"));
        waitForVueNodeDetails();
    }

    private void sendNodeDownEvent() {
        final EventBuilder builder = new EventBuilder(EventConstants.NODE_DOWN_EVENT_UEI, getClass().getSimpleName(), new Date());
        builder.setNodeid(nodeId);
        stack.opennms().getRestClient().sendEvent(builder.getEvent());
    }
}
