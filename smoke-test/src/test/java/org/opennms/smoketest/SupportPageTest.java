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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SupportPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
    	supportPage();
    }

    private void supportPage() throws Exception {
        frontPage();
        clickAndWait("link=Support");
    }

    @Test
    public void a_testAllTextIsPresent() throws Exception {
        waitForText("Commercial Support");
        waitForText("About");
        waitForText("Other Support Options");
    }

    @Test
    public void b_testAllLinksArePresent() throws InterruptedException {		
        waitForElement("link=About the OpenNMS Web Console");
        waitForElement("link=Release Notes");
        waitForElement("link=Online Documentation");
        waitForElement("link=Generate a System Report");
        waitForElement("link=Open a Bug or Enhancement Request");
        waitForElement("link=Chat with Developers on IRC");
        waitForElement("link=the OpenNMS.com support page");
    }
    @Test
    public void c_testAllFormsArePresent() throws InterruptedException {
        waitForText("Username:");
        waitForText("Password:");
        waitForElement("css=input[type=reset]");
        assertEquals("Log In", selenium.getValue("css=input[type=submit]"));
    }
    @Test
    public void d_testAllLinks() throws Exception {
        clickAndWait("link=About the OpenNMS Web Console");
        waitForText("OpenNMS Web Console");
        waitForText("License and Copyright");
        waitForText("OSI Certified Open Source Software");
        waitForText("Version:");

        supportPage();
        waitForElement("//a[@href='http://www.opennms.org/documentation/ReleaseNotesStable.html#whats-new']");
        waitForElement("//a[@href='http://www.opennms.org/wiki/']");

        clickAndWait("link=Generate a System Report");
        waitForText("Plugins");
        waitForText("Report Type");
        waitForElement("name=formatter");
        assertEquals("", selenium.getValue("css=input[type=submit]"));
        waitForText("Output");
        waitForText("Choose which plugins to enable:");

        supportPage();
        waitForElement("//a[@href='http://issues.opennms.org/']");
        waitForElement("//a[@href='irc://irc.freenode.net/%23opennms']");
    }

}
