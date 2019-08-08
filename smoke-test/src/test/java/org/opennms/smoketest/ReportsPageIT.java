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
public class ReportsPageIT extends OpenNMSSeleniumIT {
    @Before
    public void setUp() throws Exception {
        reportsPage();
    }

    @Test
    public void testAllTextIsPresent() throws Exception {
        findElementByXpath("//div[@class='card-header']/span[text()='Reports']");
        findElementByXpath("//div[@class='card-header']/span[text()='Descriptions']");
    }

    @Test
    public void testAllFormsArePresent() throws InterruptedException {
        findElementByName("resourceGraphs");
        findElementByName("kscReports");
    }

    @Test
    public void testAllLinks() throws Exception {
        findElementByLink("Resource Graphs").click();
        findElementByXpath("//label[contains(text()[normalize-space()], 'Standard Resource')]");
        findElementByXpath("//div[@class='card-header']/span[text()='Network Performance Data']");

        reportsPage();
        findElementByLink("KSC Performance, Nodes, Domains").click();
        findElementByXpath("//div[@class='card-header']/span[text()='Customized Reports']");
        findElementByXpath("//div[@class='card-header']/span[text()='Descriptions']");

        reportsPage();
        findElementByLink("Database Reports").click();
        findElementByXpath("//div[@class='card-header']/span[text()='Database Reports']");
        findElementByXpath("//div[@class='card-header']/span[text()='Descriptions']");
        findElementByLink("List reports");
        findElementByLink("View and manage pre-run reports");
        findElementByLink("Manage the batch report schedule");

        reportsPage();
        findElementByLink("Statistics Reports").click();
        findElementByXpath("//div[@class='card-header']/span[text()='Statistics Report List']");
    }

}
