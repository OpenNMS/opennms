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
public class EventsPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
    	super.setUp();
        clickAndWait("link=Events");
    }

    @Test
    public void a_testAllTextIsPresent() throws Exception {       
        waitForText("Event Queries");
        waitForText("Outstanding and acknowledged events");
        waitForText("hit [Enter]");
        waitForText("Event ID:");
    }

    @Test
    public void b_testAllLinksArePresent() throws InterruptedException {
        assertEquals("Get details", selenium.getValue("css=input[type='submit']"));
        waitForElement("link=All events");
        waitForElement("link=Advanced Search");
    }

    @Test 
    public void c_testAllLinks() throws InterruptedException {
        clickAndWait("link=All events");
        assertFalse(selenium.isTextPresent("Ack"));
        waitForText("Event(s) outstanding");
        waitForText("Event Text");
        waitForElement("link=Interface");
        clickAndWait("css=a[title='Events System Page']");
        clickAndWait("link=Advanced Search");
        waitForText("Advanced Event Search");
        waitForText("Searching Instructions");
        waitForText("Advanced Event Search");
        waitForElement("name=usebeforetime");
        waitForElement("name=limit");
        waitForElement("css=input[type='submit']");
        clickAndWait("//div[@id='content']/div/h2/a[2]");
    }

    @Test
    public void d_testNodeIdNotFoundPage() throws InterruptedException {
        selenium.open("/opennms/event/detail.jsp?id=999999999");
        waitForText("Event Not Found in Database");
    }

}
