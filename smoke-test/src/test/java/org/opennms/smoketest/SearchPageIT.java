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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SearchPageIT extends OpenNMSSeleniumIT {
    @Before
    public void setUp() throws Exception {
        deleteTestRequisition();
        searchPage();
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertEquals(3, countElementsMatchingCss("div.card-header"));
        findElementByXpath("//span[text()='Search for Nodes']");
        findElementByXpath("//span[text()='Search Asset Information']");
        findElementByXpath("//span[text()='Search Options']");
    }

    @Test
    public void testAllFormsArePresent() throws Exception {
        await().atMost(20, SECONDS).pollInterval(5, SECONDS).until(() -> countElementsMatchingCss("form") == 14);
        for (final String matchingElement : new String[] {
                "input[@id='byname_nodename']",
                "input[@id='byip_iplike']",
                "select[@name='mib2Parm']",
                "select[@name='snmpParm']",
                "select[@id='bymonitoringLocation_monitoringLocation']",
                "select[@id='byservice_service']",
                "input[@name='maclike']",
                "input[@name='foreignSource']",
                "select[@name='flows']",
                "input[@name='topology']"
        }) {
            findElementByXpath("//form[@action='element/nodeList.htm']//" + matchingElement);
        }

        findElementByXpath("//form[@action='asset/nodelist.jsp']//select[@name='searchvalue']");
        findElementByXpath("//form[@action='asset/nodelist.jsp']//select[@name='column']");
    }

    @Test
    public void testAllLinks() throws InterruptedException {
        findElementByLink("All nodes").click();
        findElementByXpath("//div[@class='btn-toolbar']/span[text()='Nodes']");

        searchPage();
        findElementByLink("All nodes and their interfaces").click();
        findElementByXpath("//span[text()='Nodes and their interfaces']");
        findElementByLink("Hide interfaces");

        searchPage();
        findElementByLink("All nodes with asset info").click();
        findElementByXpath("//span[text()='Assets']");
    }

    @Test
    public void testSearchMacAddress() throws Exception {
        final WebElement maclike = enterText(By.cssSelector("input[name='maclike']"), "0");
        maclike.sendKeys(Keys.ENTER);
        findElementByXpath("//div[@id='content']/nav/ol/li[text()='Node List']");
        findElementByXpath("//div[@class='card-header']//div[@class='btn-toolbar']/span[text()='Nodes']");
    }
}
