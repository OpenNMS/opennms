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

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry points that link to the Vue Node Details page (NMS-20412): the route itself, the Vue node
 * list redirect, the home page quick search, and a sample of legacy pages. Also the legacy node
 * page URL, which now redirects to the Vue page (NMS-20441).
 */
public class NodeDetailsLinksIT extends OpenNMSSeleniumIT {
    private static final Logger LOG = LoggerFactory.getLogger(NodeDetailsLinksIT.class);
    private static final String LABEL = "Test_Node_Links";
    private int nodeId;

    @Before
    public void setUp() throws Exception {
        final RestClient restClient = stack.opennms().getRestClient();

        // The previous test's delete is asynchronous (202), so the old node may still exist.
        waitUntilNodeIsGone("test:links1");

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
        // Leave nothing behind for the next test or test class.
        waitUntilNodeIsGone("test:links1");
    }

    /** The delete answers 202 and runs later, so poll (up to 30 s) until the node answers 404. */
    private void waitUntilNodeIsGone(final String nodeCriteria) {
        final RestClient restClient = stack.opennms().getRestClient();
        await().atMost(30, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .until(() -> {
                    try (Response response = restClient.getResponseForNode(nodeCriteria)) {
                        return response.getStatus() == 404;
                    }
                });
    }

    private void waitForVueNodeDetails() {
        wait.until(ExpectedConditions.urlMatches(".*#/node/" + nodeId + "([?&].*)?$"));
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
    public void routeResolvesAForeignIdWithASlash() throws Exception {
        final String slashLabel = "Test_Node_Links_Slash";
        Integer slashNodeId = null;

        try {
            final RestClient restClient = stack.opennms().getRestClient();

            OnmsNode node = new OnmsNode();
            node.setLabel(slashLabel);
            node.setType(OnmsNode.NodeType.ACTIVE);
            node.setForeignSource("test");
            node.setForeignId("links/2");
            final Response response = restClient.addNode(node);
            assertEquals(201, response.getStatus());
            // The v1 path lookup cannot resolve a '/' in a foreign id (and neither can delete by
            // foreignSource:foreignId), so take the numeric id from the Location header, whose last
            // segment is the new node's id.
            assertNotNull(response.getLocation());
            String location = response.getLocation().getPath();
            while (location.endsWith("/")) {
                location = location.substring(0, location.length() - 1);
            }
            slashNodeId = Integer.parseInt(location.substring(location.lastIndexOf('/') + 1));

            getDriver().get(getBaseUrlInternal() + "opennms/ui/index.html#/node/test:links%2F2");
            wait.until(ExpectedConditions.urlMatches(".*#/node/" + slashNodeId + "([?&].*)?$"));
            wait.until(pageContainsText("Node Details for " + slashLabel));

            // The legacy URL takes the foreign id as a query parameter; the redirect encodes it as
            // one path segment of the hash route.
            getDriver().get(getBaseUrlInternal() + "opennms/element/node.jsp?node=test:links%2F2");
            wait.until(ExpectedConditions.urlMatches(".*#/node/" + slashNodeId + "([?&].*)?$"));
            wait.until(pageContainsText("Node Details for " + slashLabel));
        } finally {
            if (slashNodeId != null) {
                try {
                    sendDelete("rest/nodes/" + slashNodeId, 202);
                } catch (final Exception e) {
                    LOG.warn("Could not delete node {}", slashNodeId, e);
                }
            }
        }
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

    // --- the legacy node page URL ---

    @Test
    public void legacyUrlRedirectsByNodeId() {
        getDriver().get(getBaseUrlInternal() + "opennms/element/node.jsp?node=" + nodeId);
        waitForVueNodeDetails();
    }

    @Test
    public void legacyUrlRedirectsByForeignSourceAndId() {
        getDriver().get(getBaseUrlInternal() + "opennms/element/node.jsp?node=test:links1");
        waitForVueNodeDetails();
    }

    @Test
    public void legacyUrlWithoutNodeOpensTheNodeList() {
        getDriver().get(getBaseUrlInternal() + "opennms/element/node.jsp");
        wait.until(ExpectedConditions.urlMatches(".*#/nodes([?&].*)?$"));
    }

    /**
     * Links from outside the web UI (RSS, email, stored event text) use the legacy URL, so it must
     * lead to the node after login too.
     */
    @Test
    public void legacyUrlSurvivesLogin() {
        getDriver().manage().deleteAllCookies();
        getDriver().get("about:blank");

        getDriver().get(getBaseUrlInternal() + "opennms/element/node.jsp?node=" + nodeId);
        wait.until(ExpectedConditions.urlContains("login.jsp"));

        // not login(): it waits for elements of JSP pages, which the Vue app does not have
        enterText(By.name("j_username"), BASIC_AUTH_USERNAME);
        enterText(By.name("j_password"), BASIC_AUTH_PASSWORD);
        clickElement(By.name("Login"));

        // admin/admin is sent to the password gate first; skipping it continues to the saved page
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("passwordGate.jsp"),
                ExpectedConditions.urlMatches(".*#/node/" + nodeId + "([?&].*)?$")));
        if (getDriver().getCurrentUrl().contains("passwordGate.jsp")) {
            clickElement(By.id("btn_skip"));
        }
        waitForVueNodeDetails();
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
        clickElement(By.xpath("//div[@id='onms-central-search-control']//div[contains(@class, 'search-result-item')][.//button[contains(@class, 'search-result-button')]//div[not(*)][normalize-space(.)='" + LABEL + "']]//button[contains(@class, 'search-result-button')]"));
        waitForVueNodeDetails();
    }

    private void sendNodeDownEvent() {
        final EventBuilder builder = new EventBuilder(EventConstants.NODE_DOWN_EVENT_UEI, getClass().getSimpleName(), new Date());
        builder.setNodeid(nodeId);
        stack.opennms().getRestClient().sendEvent(builder.getEvent());
    }
}
