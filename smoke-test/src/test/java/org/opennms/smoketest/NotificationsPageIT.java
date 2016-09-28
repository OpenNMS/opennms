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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NotificationsPageIT extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        notificationsPage();
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        assertEquals(3, countElementsMatchingCss("h3.panel-title"));
        findElementByXpath("//h3[text()='Notification queries']");
        findElementByXpath("//h3[text()='Outstanding and Acknowledged Notices']");
        findElementByXpath("//h3[text()='Notification Escalation']");
    }

    @Test
    public void testAllLinksArePresent() throws InterruptedException {
        findElementByLink("Your outstanding notices");
        findElementByLink("All outstanding notices");
        findElementByLink("All acknowledged notices");
    }

    @Test 
    public void testAllFormsArePresent() throws InterruptedException {
        findElementByXpath("//button[@type='submit' and text() = 'Check notices']");
        findElementByXpath("//button[@type='submit' and text() = 'Get details']");
    }

    @Test
    public void testAllLinks() throws InterruptedException {
        findElementByLink("Your outstanding notices").click();
        findElementByXpath("//span[@class='label label-default' and contains(text(), 'admin was notified')]");
        findElementByLink("[Remove all]");
        findElementByLink("Sent Time");
        findElementByXpath("//button[@type='button' and text()='Acknowledge Notices']");

        notificationsPage();
        findElementByLink("All outstanding notices").click();
        findElementByXpath("//p//strong[text()='outstanding']");
        findElementByLink("[Show acknowledged]");
        findElementByLink("Respond Time");
        assertElementDoesNotHaveText(By.xpath("//span[@class='label label-default']"), "admin was notified [-]");

        notificationsPage();
        findElementByLink("All acknowledged notices").click();
        findElementByXpath("//p//strong[text()='acknowledged']");
        findElementByLink("[Show outstanding]");
        findElementByLink("Respond Time");
        assertElementDoesNotHaveText(By.xpath("//span[@class='label label-default']"), "admin was notified [-]");
    }

}
