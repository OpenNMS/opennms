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

public class SupportPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        selenium.click("link=Support");
        waitForPageToLoad();
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertTrue(selenium.isTextPresent("Commercial Support"));
        assertTrue(selenium.isTextPresent("About"));
        assertTrue(selenium.isTextPresent("Other Support Options"));
    }

    @Test
    public void testAllLinksArePresent() {		
        assertTrue(selenium.isElementPresent("link=About the OpenNMS Web Console"));
        assertTrue(selenium.isElementPresent("link=Release Notes"));
        assertTrue(selenium.isElementPresent("link=Online Documentation"));
        assertTrue(selenium.isElementPresent("link=Generate a System Report"));
        assertTrue(selenium.isElementPresent("link=Open a Bug or Enhancement Request"));
        assertTrue(selenium.isElementPresent("link=Chat with Developers on IRC"));
        assertTrue(selenium.isElementPresent("link=the OpenNMS.com support page"));
    }
    @Test
    public void testAllFormsArePresent() {
        assertTrue(selenium.isTextPresent("Username:"));
        assertTrue(selenium.isTextPresent("Password:"));
        assertTrue(selenium.isElementPresent("css=input[type=reset]"));
        assertEquals("Log In", selenium.getValue("css=input[type=submit]"));
    }
    @Test
    public void testAllLinks() {
        selenium.click("link=About the OpenNMS Web Console");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("OpenNMS Web Console"));
        assertTrue(selenium.isTextPresent("License and Copyright"));
        assertTrue(selenium.isTextPresent("OSI Certified Open Source Software"));
        assertTrue(selenium.isTextPresent("Version:"));
        selenium.goBack();
        waitForPageToLoad();
        assertTrue(selenium.isElementPresent("//a[@href='http://www.opennms.org/documentation/ReleaseNotesStable.html#whats-new']"));
        assertTrue(selenium.isElementPresent("//a[@href='http://www.opennms.org/wiki/']"));
        selenium.click("link=Generate a System Report");
        waitForPageToLoad();
        assertTrue(selenium.isTextPresent("Plugins"));
        assertTrue(selenium.isTextPresent("Report Type"));
        assertTrue(selenium.isElementPresent("name=formatter"));
        assertEquals("", selenium.getValue("css=input[type=submit]"));
        assertTrue(selenium.isTextPresent("Output"));
        assertTrue(selenium.isTextPresent("Choose which plugins to enable:"));
        selenium.goBack();
        waitForPageToLoad();
        assertTrue(selenium.isElementPresent("//a[@href='http://issues.opennms.org/']"));
        assertTrue(selenium.isElementPresent("//a[@href='irc://irc.freenode.net/%23opennms']"));
        selenium.click("link=Log out");
        waitForPageToLoad();
    }

}
