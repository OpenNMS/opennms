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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import javax.ws.rs.core.Response;

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
 * Basic validation of the Availability panel on the Vue Node Details page.
 *
 * The panel renders its timeline in the browser from JSON rather than fetching server-drawn PNGs,
 * so what is worth checking here is that the panel reaches a real state at all: the two requests it
 * makes both resolve, and it settles on either a timeline or an explicit empty state rather than on
 * an error or a half-rendered figure.
 *
 * A freshly provisioned node has no monitored services until the poller creates them, so the
 * expected steady state here is the empty state. The assertions are deliberately shallow -- the
 * geometry, the clamping and the axis are covered by unit tests, and asserting on them through
 * Selenium would be brittle without testing anything those do not already cover.
 */
public class NodeDetailsAvailabilityIT extends OpenNMSSeleniumIT {
    private int savedNodeId;

    @Before
    public void setUp() throws Exception {
        final RestClient restClient = stack.opennms().getRestClient();

        OnmsNode node = new OnmsNode();
        node.setLabel("Test_Node_Availability");
        node.setType(OnmsNode.NodeType.ACTIVE);
        node.setForeignSource("test");
        node.setForeignId("availability1");

        final Response response = restClient.addNode(node);
        assertEquals(201, response.getStatus());

        node = restClient.getNode("test:availability1");
        assertEquals("Test_Node_Availability", node.getLabel());
        savedNodeId = node.getId();

        getDriver().get(getBaseUrlInternal() + "opennms/ui/index.html#/node/" + savedNodeId);
    }

    @After
    public void tearDown() throws Exception {
        sendDelete("rest/nodes/test:availability1", 202);
    }

    @Test
    public void canRenderTheAvailabilityPanel() {
        wait.until(pageContainsText("Node Details for Test_Node_Availability"));
        wait.until(pageContainsText("Availability"));

        // The range control is part of the panel, so its presence also confirms the panel's
        // actions slot rendered.
        final WebElement range = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-test='availability-range']")));
        assertNotNull("The availability range control was not found.", range);
    }

    @Test
    public void settlesOnAContentStateRatherThanAnError() {
        wait.until(pageContainsText("Node Details for Test_Node_Availability"));

        // Either the timeline or the empty state is a successful outcome; both requests having
        // failed is not. A node with no monitored services yet reaches the empty state.
        //
        // The empty state's selector is specific to this panel. data-test='empty-list' is shared by
        // ten components, three of them on this same page -- the IP interface, SNMP interface and
        // events tables -- so waiting on it would have been satisfied by one of those before the
        // availability panel had asked for anything.
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("[data-test='availability-timeline']")),
                ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("[data-test='availability-empty']"))));

        assertEquals("The availability panel reported a load failure.",
                0, getDriver().findElements(By.cssSelector("[data-test='availability-error']")).size());
    }

    @Test
    public void reportsNoAvailabilityFigureBeforeOneIsKnown() {
        wait.until(pageContainsText("Node Details for Test_Node_Availability"));

        final WebElement value = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-test='node-availability']")));

        // The previous panel painted 'NaN%' on first render, because it multiplied an
        // as-yet-unfetched availability by 100.
        assertFalse("The availability figure rendered as NaN.", value.getText().contains("NaN"));
    }
}
