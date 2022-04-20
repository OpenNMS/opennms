/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.smoketest.utils.RestClient;
import org.openqa.selenium.By;

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
        wait.until(pageContainsText("UI Preview"));
        clickElement(By.id("ui-preview-btn"));
        wait.until(pageContainsText("Back to main page"));
    }

    @After
    public void tearDown() throws Exception {
        sendDelete("rest/nodes/test:node1", 202);
    }

    @Test
    public void canRender() {
        // The node label should be displayed on the landing page
        wait.until(pageContainsText("ItsRainingItsPouring"));

        // If I click on the node, I should see the node's recent events
        clickElement(By.linkText("ItsRainingItsPouring"));
        wait.until(pageContainsText("Recent Events"));

        // If I refresh the browser, I should still the node's recent events
        // (This tests URL mapping to the SPA on the web server)
        driver.navigate().refresh();
        wait.until(pageContainsText("Recent Events"));
    }

}
