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
public class InstrumentationLogReaderPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        super.setUp();
        selenium.open("/opennms/admin/index.jsp");
        waitForPageToLoad();
        clickAndWait("link=Instrumentation Log Reader");
    }

    @Test
    public void a_testInstrumentationLogReaderPage() throws Exception {
        selenium.type("name=searchString", "test");
        clickAndWait("css=#ILRfilter > input[type=submit]");
        assertEquals("test", selenium.getValue("name=searchString"));
        clickAndWait("css=#ILRreset > input[type=submit]");
        assertEquals("", selenium.getValue("name=searchString"));
        waitForText("Service");
        waitForText("Threads Used:");
        waitForElement("link=Collections");
        waitForElement("link=Average Collection Time");
        waitForElement("link=Unsuccessful Percentage");
        waitForElement("link=Average Persistence Time");
    }

    @Test
    public void b_testSortingLinks() throws InterruptedException {
        clickAndWait("link=Collections");
        waitForElement("link=Collections ^");
        clickAndWait("link=Collections ^");
        waitForElement("link=Collections v");
        clickAndWait("link=Average Successful Collection Time");
        waitForElement("link=Average Successful Collection Time ^");
        clickAndWait("link=Average Successful Collection Time ^");
        waitForElement("link=Average Successful Collection Time v");
        clickAndWait("link=Average Persistence Time");
        waitForElement("link=Average Persistence Time ^");
        clickAndWait("link=Average Persistence Time ^");
        waitForElement("link=Average Persistence Time v");
    }
}
