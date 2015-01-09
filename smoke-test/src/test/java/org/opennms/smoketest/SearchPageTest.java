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

import org.junit.Before;
import org.junit.Test;

public class SearchPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        searchPage();
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        findElementByXpath("//h3[text()='Search for Nodes']");
        findElementByXpath("//h3[text()='Search Asset Information']");
        findElementByXpath("//h3[text()='Search Options']");
    }

    @Test 
    public void testAllFormsArePresent() throws InterruptedException {
        for (final String matchingElement : new String[] {
                "input[@name='nodename']",
                "input[@name='iplike']",
                "input[@name='snmpParmValue']",
                "select[@name='service']",
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

}
