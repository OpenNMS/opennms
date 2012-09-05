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

public class AlarmsPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        selenium.click("link=Alarms");
        waitForPageToLoad();
    }

    @Test
    public void testAllTextIsPresent() {
        assertTrue(selenium.isTextPresent("Alarm Queries"));
        assertTrue(selenium.isTextPresent("Outstanding and acknowledged alarms"));
        assertTrue(selenium.isTextPresent("To view acknowledged alarms"));
        assertTrue(selenium.isElementPresent("css=input[type=submit]"));
        assertTrue(selenium.isTextPresent("Alarm ID:"));
    }

    @Test
    public void testAllLinksArePresent() { 
        assertTrue(selenium.isElementPresent("link=All alarms (summary)"));
        assertTrue(selenium.isElementPresent("link=All alarms (detail)"));
        assertTrue(selenium.isElementPresent("link=Advanced Search"));
    }

    @Test
    public void testAllLinks(){
        selenium.click("link=All alarms (summary)");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("alarm is outstanding"));
        assertTrue(selenium.isTextPresent("alarm is outstanding"));
        assertTrue(selenium.isElementPresent("//input[@value='Go']"));
        assertTrue(selenium.isElementPresent("css=input[type='submit']"));
        selenium.click("css=a[title='Alarms System Page']");
        waitForPageToLoad();
        selenium.click("link=All alarms (detail)");
        waitForPageToLoad();
        assertTrue(selenium.isElementPresent("link=First Event Time"));
        assertTrue(selenium.isElementPresent("link=Last Event Time"));
        assertTrue(selenium.isElementPresent("css=input[type='reset']"));
        assertTrue(selenium.isTextPresent("Ack"));
        selenium.click("css=a[title='Alarms System Page']");
        waitForPageToLoad();
        selenium.click("link=Advanced Search");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Alarm Text Contains:"));
        assertTrue(selenium.isTextPresent("Advanced Alarm Search"));
        selenium.open("/opennms/alarm/advsearch.jsp");
        assertTrue(selenium.isTextPresent("Advanced Alarm Search page"));
        assertTrue(selenium.isElementPresent("css=input[type='submit']"));
        assertTrue(selenium.isElementPresent("name=beforefirsteventtimemonth"));
        selenium.click("//div[@id='content']/div/h2/a[2]");
        waitForPageToLoad();
    }

}
