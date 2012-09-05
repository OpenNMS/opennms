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

public class AssetsPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        selenium.click("link=Assets");
        waitForPageToLoad();
    }

    @Test
    public void testAllTextIsPresent() { 
        assertTrue(selenium.isTextPresent("Search Asset Information"));
        assertTrue(selenium.isTextPresent("Assets Inventory"));
        assertTrue(selenium.isTextPresent("nter the data by hand"));
        assertTrue(selenium.isTextPresent("Assets with asset numbers"));
        assertTrue(selenium.isTextPresent("Assets in category"));
    }    

    @Test 
    public void testAllLinksArePresent() {
        assertTrue(selenium.isElementPresent("css=input[type=submit]"));
        assertTrue(selenium.isElementPresent("name=searchvalue"));
        assertTrue(selenium.isElementPresent("link=All nodes with asset info"));
    }
    @Test
    public void testAllLinks() {
        selenium.click("link=All nodes with asset info");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Assets"));
        selenium.click("//div[@id='content']/div/h2/a[2]");
        waitForPageToLoad();
        selenium.click("link=Log out");
        waitForPageToLoad();
    }

}
