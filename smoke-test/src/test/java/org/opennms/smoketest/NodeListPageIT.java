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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.google.common.collect.Iterables;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NodeListPageIT extends OpenNMSSeleniumIT {
    public final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("y-MM-dd'T'HH:mm:ss.SSSXXX");

    @Before
    public void setUp() throws Exception {
        deleteTestRequisition();
        createLocation("Pittsboro");
        createLocation("Fulda");

        createNode("loc1node1", "Pittsboro");
        createNode("loc1node2", "Pittsboro", true);
        createNode("loc2node1", "Fulda");
        createNode("loc2node2", "Fulda", true);

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
        createNode(foreignId, location, false);
    }

    private void createNode(final String foreignId, final String location, boolean hasFlows) throws Exception {
        final String currentDate = SIMPLE_DATE_FORMAT.format(new Date());

        final String node = "<node type=\"A\" " + (hasFlows ? "lastIngressFlow=\"" + currentDate + "\" lastEgressFlow=\"" + currentDate + "\"" : "") + " label=\"TestMachine " + foreignId + "\" foreignSource=\""+ REQUISITION_NAME +"\" foreignId=\"" + foreignId + "\">" +
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
    public void testNodesWithFlows() {
        driver.get(this.getBaseUrlInternal() + "opennms/element/nodeList.htm?flows=true");
        assertThat(Iterables.transform(driver.findElements(By.xpath("//div[@class='NLnode']//a")), WebElement::getText),
                containsInAnyOrder("TestMachine loc1node2", "TestMachine loc2node2"));
    }

    @Test
    public void testNodesWithoutFlows() {
        driver.get(this.getBaseUrlInternal() + "opennms/element/nodeList.htm?flows=false");
        assertThat(Iterables.transform(driver.findElements(By.xpath("//div[@class='NLnode']//a")), WebElement::getText),
                containsInAnyOrder("TestMachine loc1node1", "TestMachine loc2node1"));
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        findElementByXpath("//div[@class='btn-toolbar']/span[text()='Nodes']");
        findElementByXpath("//ol[@class=\"breadcrumb\"]//li[text()='Node List']");
    }

    @Test
    public void testAllLinks() throws InterruptedException {
        findElementByLink("Show interfaces").click();
        findElementByXpath("//div[@class='card-header']/span[text()='Nodes and their interfaces']");
        findElementByLink("Hide interfaces");
    }

    @Test
    public void testAvailableLocations() throws Exception {
        // We use hasItems() instead of containsInAnyOrder() at some points because other tests do
        // not properly clean up their created nodes ans locations.

        // Check if default selection is 'all locations' and all locations are listed
        findElementByXpath("//select[@id='monitoringLocation']//option[text()='All locations' and @selected]");
        assertThat(Iterables.transform(driver.findElements(By.xpath("//select[@id='monitoringLocation']//option")), WebElement::getText),
                hasItems("All locations",
                        "Pittsboro",
                        "Fulda"));

        // Check the default lists all nodes
        assertThat(Iterables.transform(driver.findElements(By.xpath("//div[@class='NLnode']//a")), WebElement::getText),
                hasItems("TestMachine loc1node1",
                        "TestMachine loc1node2",
                        "TestMachine loc2node1",
                        "TestMachine loc2node2"));

        // Check switching to first location
        findElementByXpath("//select[@id='monitoringLocation']//option[text()='Pittsboro']").click();
        findElementByXpath("//select[@id='monitoringLocation']//option[text()='Pittsboro' and @selected]");
        assertThat(Iterables.transform(driver.findElements(By.xpath("//div[@class='NLnode']//a")), WebElement::getText),
                containsInAnyOrder("TestMachine loc1node1",
                        "TestMachine loc1node2"));

        // Check switching to second location
        findElementByXpath("//select[@id='monitoringLocation']//option[text()='Fulda']").click();
        findElementByXpath("//select[@id='monitoringLocation']//option[text()='Fulda' and @selected]");
        assertThat(Iterables.transform(driver.findElements(By.xpath("//div[@class='NLnode']//a")), WebElement::getText),
                containsInAnyOrder("TestMachine loc2node1",
                        "TestMachine loc2node2"));

        // Check switching to unfiltered
        findElementByXpath("//select[@id='monitoringLocation']//option[text()='All locations']").click();
        findElementByXpath("//select[@id='monitoringLocation']//option[text()='All locations' and @selected]");
        assertThat(Iterables.transform(driver.findElements(By.xpath("//div[@class='NLnode']//a")), WebElement::getText),
                hasItems("TestMachine loc1node1",
                        "TestMachine loc1node2",
                        "TestMachine loc2node1",
                        "TestMachine loc2node2"));
    }
}
