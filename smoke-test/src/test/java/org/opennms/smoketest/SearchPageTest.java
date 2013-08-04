/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
        super.setUp();
        selenium.click("link=Search");
        waitForPageToLoad();
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertTrue(selenium.isTextPresent("Search for Nodes"));
        assertTrue(selenium.isTextPresent("Search Asset Information"));
        assertTrue(selenium.isTextPresent("Search Options"));
        assertTrue(selenium.isTextPresent("MAC Address"));
    }

    @Test
    public void testAllLinksArePresent() {
        assertTrue(selenium.isElementPresent("link=All nodes"));
        assertTrue(selenium.isElementPresent("link=All nodes and their interfaces"));
        assertTrue(selenium.isElementPresent("link=All nodes with asset info"));
    }

    @Test 
    public void testAllFormsArePresent() {
        assertTrue(selenium.isElementPresent("css=input[type=submit]"));
        assertEquals("Search", selenium.getValue("css=input[type=submit]"));
    }

    @Test
    public void testAllLinks() throws InterruptedException {
        clickAndWait("link=All nodes");
        waitForText("Nodes", LOAD_TIMEOUT);
        clickAndWait("//div[@id='content']/div/h2/a[2]");
        clickAndWait("link=All nodes and their interfaces");
        assertTrue(selenium.isTextPresent("Nodes and their interfaces"));
        assertTrue(selenium.isElementPresent("link=Hide interfaces"));
        clickAndWait("//div[@id='content']/div/h2/a[2]");
        clickAndWait("link=All nodes with asset info");
        assertTrue(selenium.isTextPresent("Assets"));
    }

}
