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

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

public class OutagePageTest extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        super.setUp();
        m_driver.get(BASE_URL + "opennms/outage/index.jsp");
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        findElementByXpath("//h3[text()='Outage Menu']");
        findElementByXpath("//h3[text()='Outages and Service Level Availability']");
        findElementByName("outageIdForm").findElement(By.name("id"));
    }  

    @Test
    public void testAllLinks() throws InterruptedException {
        findElementByLink("Current outages").click();
        Select select = new Select(findElementByName("outage_search_constraints_box_outtype_form").findElement(By.name("outtype")));
        assertEquals("Current", select.getFirstSelectedOption().getText());
        findElementByLink("Interface");
        goBack();

        findElementByLink("All outages").click();
        select = new Select(findElementByName("outage_search_constraints_box_outtype_form").findElement(By.name("outtype")));
        assertEquals("Both Current & Resolved", select.getFirstSelectedOption().getText());
        findElementByLink("Interface");
        goBack();

        findElementByName("outageIdForm").findElement(By.xpath("//input[@type='submit']")).click();
        final Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertNotNull(alert);
        assertEquals("Please enter a valid outage ID.", alert.getText());
    }

}
