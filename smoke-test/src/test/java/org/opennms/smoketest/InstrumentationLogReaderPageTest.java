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

public class InstrumentationLogReaderPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        super.setUp();
        selenium.open("/opennms/admin/index.jsp");
        waitForPageToLoad();
        selenium.click("link=Instrumentation Log Reader");
        waitForPageToLoad();
    }

    @Test
    public void testInstrumentationLogReaderPage() throws Exception {
        selenium.type("name=searchString", "test");
        selenium.click("css=input[type=submit]");
        waitForPageToLoad();
        assertEquals("test", selenium.getValue("name=searchString"));
        selenium.click("css=form > input[type=submit]");
        waitForPageToLoad();
        assertEquals("", selenium.getValue("name=searchString"));
        assertTrue(selenium.isTextPresent("Service"));
        assertTrue(selenium.isTextPresent("Threads Used:"));
        assertTrue(selenium.isElementPresent("link=Collections"));
        assertTrue(selenium.isElementPresent("link=Average Collection Time"));
        assertTrue(selenium.isElementPresent("link=Unsuccessful Percentage"));
        assertTrue(selenium.isElementPresent("link=Average Persistence Time"));
    }
    
    @Test
    public void testSortingLinks() {
        selenium.click("link=Collections");
        waitForPageToLoad();
        assertTrue(selenium.isElementPresent("link=Collections ^"));
        selenium.click("link=Collections ^");
        waitForPageToLoad();
        assertTrue(selenium.isElementPresent("link=Collections v"));
        selenium.click("link=Average Successful Collection Time");
        waitForPageToLoad();
        assertTrue(selenium.isElementPresent("link=Average Successful Collection Time ^"));
        selenium.click("link=Average Successful Collection Time ^");
        waitForPageToLoad();
        assertTrue(selenium.isElementPresent("link=Average Successful Collection Time v"));
        selenium.click("link=Average Persistence Time");
        waitForPageToLoad();
        assertTrue(selenium.isElementPresent("link=Average Persistence Time ^"));
        selenium.click("link=Average Persistence Time ^");
        waitForPageToLoad();
        assertTrue(selenium.isElementPresent("link=Average Persistence Time v"));
        selenium.click("link=Log out");
        waitForPageToLoad();
    }
}
