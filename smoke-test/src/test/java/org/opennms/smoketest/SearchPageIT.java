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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SearchPageIT extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        deleteTestRequisition();
        searchPage();
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertEquals(3, countElementsMatchingCss("h3.panel-title"));
        findElementByXpath("//h3[text()='Search for Nodes']");
        findElementByXpath("//h3[text()='Search Asset Information']");
        findElementByXpath("//h3[text()='Search Options']");
    }

    @Test 
    public void testAllFormsArePresent() throws Exception {
        assertEquals(10, countElementsMatchingCss("form"));
        for (final String matchingElement : new String[] {
                "input[@id='byname_nodename']",
                "input[@id='byip_iplike']",
                "select[@name='mib2Parm']",
                "select[@name='snmpParm']",
                "select[@id='bymonitoringLocation_monitoringLocation']",
                "select[@id='byservice_service']",
                "input[@name='maclike']",
                "input[@name='foreignSource']"
        }) {
            findElementByXpath("//form[@action='element/nodeList.htm']//" + matchingElement);
        }
        
        findElementByXpath("//form[@action='asset/nodelist.jsp']//select[@name='searchvalue']");
        findElementByXpath("//form[@action='asset/nodelist.jsp']//select[@name='column']");
    }

    @Test
    public void testAllLinks() throws InterruptedException {
        findElementByLink("All nodes").click();
        findElementByXpath("//h3//span[text()='Nodes']");

        searchPage();
        findElementByLink("All nodes and their interfaces").click();
        findElementByXpath("//h3[text()='Nodes and their interfaces']");
        findElementByLink("Hide interfaces");

        searchPage();
        findElementByLink("All nodes with asset info").click();
        findElementByXpath("//h3[text()='Assets']");
    }

    @Test
    public void testSearchMacAddress() throws Exception {
        final WebElement maclike = enterText(By.cssSelector("input[name='maclike']"), "0");
        maclike.sendKeys(Keys.ENTER);
        findElementByXpath("//div[@id='content']/ol/li[text()='Node List']");
        findElementByXpath("//h3[@class='panel-title']/span[text()='Nodes']");
    }
}
