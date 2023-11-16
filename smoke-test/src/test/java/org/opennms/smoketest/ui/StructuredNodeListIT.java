/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.smoketest.OpenNMSSeleniumIT;
import org.opennms.smoketest.utils.RestClient;

/**
 * Basic validation of the Vue Structured Node List page.
 */
public class StructuredNodeListIT extends OpenNMSSeleniumIT {
    protected int savedNodeId;

    @Before
    public void setUp() throws Exception {
        // Add a node 'test:node1' for use in testing

        // Connect to our instance
        final RestClient restClient = stack.opennms().getRestClient();

        // Create a node
        OnmsNode node = new OnmsNode();
        node.setLabel("Test_Node1");
        node.setType(OnmsNode.NodeType.ACTIVE);

        // Set foreignSource and foreignId to use it as nodeCriteria
        node.setForeignSource("test");
        node.setForeignId("node1");

        // Verify that node was added
        Response response = restClient.addNode(node);
        assertEquals(201, response.getStatus());
        node = restClient.getNode("test:node1");
        assertEquals("Test_Node1", node.getLabel());

        savedNodeId = node.getId();

        // Navigate to the Structured Node List page
        getDriver().get(getBaseUrlInternal() + "opennms/ui/index.html#/nodes");
    }

    @After
    public void tearDown() throws Exception {
        sendDelete("rest/nodes/test:node1", 202);
    }

    @Test
    public void canRender() {
        // Ensure we are on the Structured Node List page and the test node is displayed
        wait.until(pageContainsText("Structured Node List"));
        wait.until(pageContainsText("Test_Node1"));

        // find the Actions menu for this node
        findElementByXpath("//div[@class='node-table']/table/tbody/tr/td//button[@title='Node Actions']");

        // find the node row in the table
        findElementByXpath("//div[@class='node-table']/table/tbody/tr/td/a[text()='Test_Node1']");

        // find the node link
        String nodeLinkXpath = String.format("//div[@class='node-table']/table/tbody/tr/td/a[contains(@href, 'element/node.jsp?node=%d')]", savedNodeId);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(nodeLinkXpath)));

        WebElement nodeLink = findElementByXpath(nodeLinkXpath);
        assertNotNull("Node link element was not found.", nodeLink);
        assertTrue("Node link element was not displayed.", nodeLink.isDisplayed());

        // Sleep for 500ms before clicking, otherwise link may be obscured
        sleepQuietly(500);

        // Click on the node link and navigate to the "classic" Node page for that node
        nodeLink.click();

        // Ensure we are on the "classic" node page for that link
        wait.until(pageContainsText("Node: Test_Node1"));
    }
}

