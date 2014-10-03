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
public class NotificationsPageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        super.setUp();
        clickAndWait("link=Notifications");
    }

    @Test
    public void a_testAllTextIsPresent() throws Exception {
        waitForText("Notification queries");
        waitForText("Outstanding and Acknowledged Notices");
        waitForText("Notification Escalation");
        waitForText("Check your outstanding notices");
        waitForText("Once a notice is sent");
        waitForText("User:");
        waitForText("Notice:");
    }

    @Test
    public void b_testAllLinksArePresent() throws InterruptedException {
        waitForElement("link=Your outstanding notices");
        waitForElement("link=All outstanding notices");
        waitForElement("link=All acknowledged notices");
    }

    @Test 
    public void c_testAllFormsArePresent() throws InterruptedException {
        waitForElement("css=input[type=submit]");
        waitForElement("//input[@value='Get details']");
    }

    @Test
    public void d_testAllLinks() throws InterruptedException {
        clickAndWait("link=Your outstanding notices");
        waitForText("admin was notified");
        waitForElement("link=[Remove all]");
        waitForElement("link=Sent Time");
        waitForElement("//input[@value='Acknowledge Notices']");
        clickAndWait("link=Notices");
        clickAndWait("link=All outstanding notices");
        waitForText("only outstanding notices");
        waitForElement("link=Respond Time");
        waitForElement("css=input[type=button]");
        clickAndWait("link=Notices");
        clickAndWait("link=All acknowledged notices");
        waitForText("only acknowledged notices");
        waitForElement("link=Node");
        waitForElement("css=input[type=submit]");
        clickAndWait("link=Notices");
    }

}
