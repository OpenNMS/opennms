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



public class NotificationsPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        selenium.click("link=Notifications");
        waitForPageToLoad();
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertTrue(selenium.isTextPresent("Notification queries"));
        assertTrue(selenium.isTextPresent("Outstanding and Acknowledged Notices"));
        assertTrue(selenium.isTextPresent("Notification Escalation"));
        assertTrue(selenium.isTextPresent("Check your outstanding notices"));
        assertTrue(selenium.isTextPresent("Once a notice is sent"));
        assertTrue(selenium.isTextPresent("User:"));
        assertTrue(selenium.isTextPresent("Notice:"));
    }

    @Test
    public void testAllLinksArePresent() {
        assertTrue(selenium.isElementPresent("link=Your outstanding notices"));
        assertTrue(selenium.isElementPresent("link=All outstanding notices"));
        assertTrue(selenium.isElementPresent("link=All acknowledged notices"));
    }

    @Test 
    public void testAllFormsArePresent() {
        assertTrue(selenium.isElementPresent("css=input[type=submit]"));
        assertTrue(selenium.isElementPresent("//input[@value='Get details']"));
    }

    @Test
    public void testAllLinks() {
        selenium.click("link=Your outstanding notices");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("admin was notified"));
        assertTrue(selenium.isElementPresent("link=[Remove all]"));
        assertTrue(selenium.isElementPresent("link=Sent Time"));
        assertTrue(selenium.isElementPresent("//input[@value='Acknowledge Notices']"));
        selenium.click("link=Notices");
        waitForPageToLoad();
        selenium.click("link=All outstanding notices");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("only outstanding notices"));
        assertTrue(selenium.isElementPresent("link=Respond Time"));
        assertTrue(selenium.isElementPresent("css=input[type=button]"));
        selenium.click("link=Notices");
        waitForPageToLoad();
        selenium.click("link=All acknowledged notices");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("only acknowledged notices"));
        assertTrue(selenium.isElementPresent("link=Node"));
        assertTrue(selenium.isElementPresent("css=input[type=submit]"));
        selenium.click("link=Notices");
        waitForPageToLoad();
        selenium.click("link=Log out");
        waitForPageToLoad();
    }

}
