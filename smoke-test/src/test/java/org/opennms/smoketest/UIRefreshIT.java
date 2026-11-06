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
package org.opennms.smoketest;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.smoketest.utils.RestClient;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Basic validation of the new UI
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UIRefreshIT extends OpenNMSSeleniumIT {

    @Before
    public void setUp() throws Exception {
        // Connect to our instance
        final RestClient restClient = stack.opennms().getRestClient();

        // Create a node
        OnmsNode node = new OnmsNode();
        node.setLabel("ItsRainingItsPouring");
        node.setType(OnmsNode.NodeType.ACTIVE);

        // Set foreignSource and foreignId to use it as nodeCriteria
        node.setForeignSource("test");
        node.setForeignId("node1");

        // Verify that node was added
        Response response = restClient.addNode(node);
        assertEquals(201, response.getStatus());
        node = restClient.getNode("test:node1");
        assertEquals("ItsRainingItsPouring", node.getLabel());

        // Switch to the new UI
        clickMenuItem("Inventory", "Nodes");
    }

    @After
    public void tearDown() throws Exception {
        sendDelete("rest/nodes/test:node1", 202);
    }

    @Test
    public void canRender() {
        // The node label should be displayed on the landing page
        wait.until(pageContainsText("ItsRainingItsPouring"));

        // If I click on the node, I should see its Vue Node Details page
        clickElement(By.linkText("ItsRainingItsPouring"));
        wait.until(ExpectedConditions.urlContains("#/node/"));
        wait.until(pageContainsText("Node Details for ItsRainingItsPouring"));

        // If I refresh the browser, I should still see the node's details
        // (This tests URL mapping to the SPA on the web server)
        driver.navigate().refresh();
        wait.until(pageContainsText("Node Details for ItsRainingItsPouring"));
    }
}
