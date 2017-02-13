/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.google.common.collect.Iterables;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NodeListPageIT extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        deleteTestRequisition();
        createLocation("Pittsboro");
        createLocation("Fulda");

        createNode("loc1node1", "Pittsboro");
        createNode("loc1node2", "Pittsboro");
        createNode("loc2node1", "Fulda");
        createNode("loc2node2", "Fulda");

        nodePage();
    }

    @After
    public void tearDown() throws Exception {
        deleteTestRequisition();
        deleteLocation("Pittsboro");
        deleteLocation("Fulda");
    }

    private void deleteLocation(final String location) throws Exception {
        sendDelete("/rest/monitoringLocations/" + location);
    }

    private void createLocation(final String location) throws Exception {
        sendPost("/rest/monitoringLocations", "<location location-name=\"" + location + "\" monitoring-area=\"" + location + "\"/>", 201);
    }

    private void createNode(final String foreignId, final String location) throws Exception {
        final String node = "<node type=\"A\" label=\"TestMachine " + foreignId + "\" foreignSource=\""+ REQUISITION_NAME +"\" foreignId=\"" + foreignId + "\">" +
        "<labelSource>H</labelSource>" +
        "<sysContact>The Owner</sysContact>" +
        "<sysDescription>" +
        "Darwin TestMachine 9.4.0 Darwin Kernel Version 9.4.0: Mon Jun  9 19:30:53 PDT 2008; root:xnu-1228.5.20~1/RELEASE_I386 i386" +
        "</sysDescription>" +
        "<sysLocation>DevJam</sysLocation>" +
        "<sysName>TestMachine" + foreignId + "</sysName>" +
        "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>" +
        "<location>" + location + "</location>" +
        "</node>";
        sendPost("/rest/nodes", node, 201);
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        findElementByXpath("//h3//span[text()='Nodes']");
        findElementByXpath("//ol[@class=\"breadcrumb\"]//li[text()='Node List']");
    }

    @Test
    public void testAllLinks() throws InterruptedException {
        findElementByLink("Show interfaces").click();
        findElementByXpath("//h3[text()='Nodes and their interfaces']");
        findElementByLink("Hide interfaces");
    }

    @Test
    public void testAvailableLocations() throws Exception {
        // We use hasItems() instead of containsInAnyOrder() at some points because other tests do
        // not properly clean up their created nodes ans locations.

        // Check if default selection is 'all locations' and all locations are listed
        findElementByXpath("//select[@id='monitoringLocation']//option[text()='All locations' and @selected]");
        assertThat(Iterables.transform(m_driver.findElements(By.xpath("//select[@id='monitoringLocation']//option")), WebElement::getText),
                   hasItems("All locations",
                            "Pittsboro",
                            "Fulda"));

        // Check the default lists all nodes
        assertThat(Iterables.transform(m_driver.findElements(By.xpath("//div[@class='NLnode']//a")), WebElement::getText),
                   hasItems("TestMachine loc1node1",
                            "TestMachine loc1node2",
                            "TestMachine loc2node1",
                            "TestMachine loc2node2"));

        // Check switching to first location
        findElementByXpath("//select[@id='monitoringLocation']//option[text()='Pittsboro']").click();
        findElementByXpath("//select[@id='monitoringLocation']//option[text()='Pittsboro' and @selected]");
        assertThat(Iterables.transform(m_driver.findElements(By.xpath("//div[@class='NLnode']//a")), WebElement::getText),
                   containsInAnyOrder("TestMachine loc1node1",
                                      "TestMachine loc1node2"));

        // Check switching to second location
        findElementByXpath("//select[@id='monitoringLocation']//option[text()='Fulda']").click();
        findElementByXpath("//select[@id='monitoringLocation']//option[text()='Fulda' and @selected]");
        assertThat(Iterables.transform(m_driver.findElements(By.xpath("//div[@class='NLnode']//a")), WebElement::getText),
                   containsInAnyOrder("TestMachine loc2node1",
                                      "TestMachine loc2node2"));

        // Check switching to unfiltered
        findElementByXpath("//select[@id='monitoringLocation']//option[text()='All locations']").click();
        findElementByXpath("//select[@id='monitoringLocation']//option[text()='All locations' and @selected]");
        assertThat(Iterables.transform(m_driver.findElements(By.xpath("//div[@class='NLnode']//a")), WebElement::getText),
                   hasItems("TestMachine loc1node1",
                            "TestMachine loc1node2",
                            "TestMachine loc2node1",
                            "TestMachine loc2node2"));
    }
}
