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
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OutagePageIT extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        outagePage();
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        findElementByXpath("//ol[@class='breadcrumb']/li[text()='Outages']");
        findElementByXpath("//th/a[text()='ID']");
        findElementByXpath("//th/a[text()='Foreign Source']");
        findElementByXpath("//th/a[text()='Node']");
    }

    @Test
    public void testAllLinks() throws InterruptedException {
        findElementByXpath("//button[text()='Current']").click();
        findElementByXpath("//button[contains(@class, 'active') and text()='Current']");
        findElementByXpath("//button[not(contains(@class, 'active')) and text()='Resolved']");
        findElementByXpath("//button[not(contains(@class, 'active')) and text()='Both']");

        findElementByXpath("//button[text()='Both']").click();
        findElementByXpath("//button[not(contains(@class, 'active')) and text()='Current']");
        findElementByXpath("//button[not(contains(@class, 'active')) and text()='Resolved']");
        findElementByXpath("//button[contains(@class, 'active') and text()='Both']");

        findElementByXpath("//button[text()='Resolved']").click();
        findElementByXpath("//button[not(contains(@class, 'active')) and text()='Current']");
        findElementByXpath("//button[contains(@class, 'active') and text()='Resolved']");
        findElementByXpath("//button[not(contains(@class, 'active')) and text()='Both']");

        // TODO: Test search
    }

}
