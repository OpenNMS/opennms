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


public class OutagePageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        selenium.click("link=Outages");
        waitForPageToLoad();
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertTrue(selenium.isTextPresent("Outage Menu"));
        assertTrue(selenium.isTextPresent("Outages and Service Level Availability"));
        assertTrue(selenium.isTextPresent("Outage ID"));
        assertTrue(selenium.isTextPresent("create notifications"));
    }  

    @Test
    public void testAllLinksArePresent() {
        assertTrue(selenium.isElementPresent("link=Current outages"));
        assertTrue(selenium.isElementPresent("link=All outages"));
    }

    @Test
    public void testAllFormsArePresent() {
        assertEquals("Get details", selenium.getValue("css=input[type='submit']"));
    }
    
    @Test
    public void testAllLinks() {
        selenium.click("link=Current outages");
        waitForPageToLoad();
        assertTrue(selenium.isElementPresent("name=outtype"));
        assertTrue(selenium.isElementPresent("css=input[type='submit']"));
        assertTrue(selenium.isElementPresent("link=Interface"));
        selenium.click("css=a[title='Outages System Page']");
        waitForPageToLoad();
        selenium.click("link=All outages");
        waitForPageToLoad();
        assertTrue(selenium.isElementPresent("name=outtype"));
        assertTrue(selenium.isTextPresent("Current Resolved Both Current & Resolved"));
        assertTrue(selenium.isTextPresent("Interface"));
        selenium.click("css=a[title='Outages System Page']");
        waitForPageToLoad();
        selenium.click("css=input[type='submit']");
        assertEquals("Please enter a valid outage ID.", selenium.getAlert());
        selenium.click("link=Log out");
        waitForPageToLoad();
    }

}
