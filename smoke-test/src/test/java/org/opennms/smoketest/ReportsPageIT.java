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

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ReportsPageIT extends OpenNMSSeleniumTestCase {
    @Before
    public void setUp() throws Exception {
        reportsPage();
        setImplicitWait(10, TimeUnit.SECONDS);
    }

    @After
    public void resetTimer() {
        setImplicitWait();
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        findElementByXpath("//h3[@class='panel-title' and text()='Reports']");
        findElementByXpath("//h3[@class='panel-title' and text()='Descriptions']");
    }

    @Test
    public void testAllFormsArePresent() throws InterruptedException {
        findElementByName("resourceGraphs");
        findElementByName("kscReports");
    }

    @Test
    public void testAllLinks() throws Exception {
        clickElement(By.xpath("//div[@id='content']//a[text()='Resource Graphs']"));
        findElementByXpath("//label[contains(text()[normalize-space()], 'Standard Resource')]");
        findElementByXpath("//h3[text()='Network Performance Data']");

        reportsPage();
        clickElement(By.linkText("KSC Performance, Nodes, Domains"));
        findElementByXpath("//h3[text()='Customized Reports']");
        findElementByXpath("//h3[text()='Descriptions']");

        reportsPage();
        clickElement(By.xpath("//div[@id='content']//a[text()='Database Reports']"));
        findElementByXpath("//h3[text()='Database Reports']");
        findElementByXpath("//h3[text()='Descriptions']");
        findElementByLink("List reports");
        findElementByLink("View and manage pre-run reports");
        findElementByLink("Manage the batch report schedule");

        reportsPage();
        clickElement(By.linkText("Statistics Reports"));
        findElementByXpath("//h3[text()='Statistics Report List']");
    }

}
