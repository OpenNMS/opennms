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
        await().atMost(20, SECONDS).pollInterval(5, SECONDS).until(() -> countElementsMatchingCss("form") == 13);
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
