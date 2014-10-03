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
public class OutagePageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        super.setUp();
        clickAndWait("link=Outages");
    }

    @Test
    public void a_testAllTextIsPresent() throws Exception {
        waitForText("Outage Menu");
        waitForText("Outages and Service Level Availability");
        waitForText("Outage ID");
        waitForText("create notifications");
    }  

    @Test
    public void b_testAllLinksArePresent() throws InterruptedException {
        waitForElement("link=Current outages");
        waitForElement("link=All outages");
    }

    @Test
    public void c_testAllFormsArePresent() {
        assertEquals("Get details", selenium.getValue("css=input[type='submit']"));
    }

    @Test
    public void d_testAllLinks() throws InterruptedException {
        clickAndWait("link=Current outages");
        waitForElement("name=outtype");
        waitForElement("css=input[type='submit']");
        waitForElement("link=Interface");
        clickAndWait("css=a[title='Outages System Page']");
        clickAndWait("link=All outages");
        waitForElement("name=outtype");
        waitForText("Current Resolved Both Current & Resolved");
        waitForText("Interface");
        clickAndWait("css=a[title='Outages System Page']");
        clickAndWait("css=input[type='submit']");
        assertEquals("Please enter a valid outage ID.", selenium.getAlert());
    }

}
