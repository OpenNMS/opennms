/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MenuHeaderIT extends OpenNMSSeleniumIT {

    @Test
    public void testMenuEntries() throws Exception {
        clickMenuItem("Search", null, "element/index.jsp");
        findElementByXpath("//h3[@class='panel-title' and text()='Search for Nodes']");

        clickMenuItem("Info", "Nodes", "element/nodeList.htm");
        findElementByXpath("//h3[@class='panel-title']/span[text()='Nodes' or text()='Availability']");

        clickMenuItem("Info", "Assets", "asset/index.jsp");
        findElementByXpath("//h3[@class='panel-title' and text()='Search Asset Information']");

        clickMenuItem("Info", "Path Outages", "pathOutage/index.jsp");
        findElementByXpath("//h3[@class='panel-title' and text()='All Path Outages']");

        clickMenuItem("Status", "Events", "event/index");
        findElementByXpath("//h3[@class='panel-title' and text()='Event Queries']");

        clickMenuItem("Status", "Alarms", "alarm/index.htm");
        findElementByXpath("//h3[@class='panel-title' and text()='Alarm Queries']");

        clickMenuItem("Status", "Notifications", "notification/index.jsp");
        findElementByXpath("//h3[@class='panel-title' and text()='Notification queries']");

        clickMenuItem("Status", "Outages", "outage/index.jsp");
        findElementByXpath("//h3[@class='panel-title' and text()='Outage Menu']");

        /*
        clickMenuItem("Status", "Distributed Status", "distributedStatusSummary.htm");
        findElementByXpath("//h3[@class='panel-title' and contains(text(), 'Distributed Status Summary')]");
        */

        clickMenuItem("Status", "Surveillance", "surveillance-view.jsp");
        // switchTo() by xpath is much faster than by ID
        //driver.switchTo().frame("surveillance-view-ui");
        driver.switchTo().frame(findElementByXpath("/html/body/div/iframe"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Surveillance view: default']")));
        driver.switchTo().parentFrame();
        frontPage();

        final String reportsMenuName = "name=nav-Reports-top";
        clickMenuItem(reportsMenuName, "Charts", "charts/index.jsp");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("include-charts")));

        clickMenuItem(reportsMenuName, "Resource Graphs", "graph/index.jsp");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h3[contains(text()[normalize-space()], 'Standard Resource')]")));

        clickMenuItem(reportsMenuName, "KSC Reports", "KSC/index.htm");
        findElementByXpath("//h3[@class='panel-title' and text()='Customized Reports']");

        clickMenuItem(reportsMenuName, "Statistics", "statisticsReports/index.htm");
        findElementByXpath("//h3[@class='panel-title' and text()='Statistics Report List']");

        final String dashboardsMenuName = "name=nav-Dashboards-top";
        clickMenuItem(dashboardsMenuName, "Dashboard", "dashboard.jsp");
        // switchTo() by xpath is much faster than by ID
        //driver.switchTo().frame("surveillance-view-ui");
        driver.switchTo().frame(findElementByXpath("/html/body/div/iframe"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Surveillance view: default']")));
        driver.switchTo().parentFrame();
        frontPage();

        clickMenuItem(dashboardsMenuName, "Ops Board", "vaadin-wallboard");
        findElementByXpath("//select[@class='v-select-select']");

        frontPage();
        final String mapsMenuName = "name=nav-Maps-top";
        clickMenuItem(mapsMenuName, "Topology", "topology");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='v-button-caption']//i[@class='icon-location-arrow']")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='navbar']//a[@name='nav-Maps-top']")));

        frontPage();
        clickMenuItem(mapsMenuName, "Geographical", "node-maps");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[text()='Show Severity >=']")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='navbar']//a[@name='nav-Maps-top']")));

        frontPage();
        final String adminMenuName = "name=nav-admin-top";
        clickMenuItem(adminMenuName, "Configure OpenNMS", "opennms/admin/index.jsp");
        findElementByXpath("//h3[@class='panel-title' and text()='OpenNMS System']");
        findElementByXpath("//h3[@class='panel-title' and text()='Provisioning']");
        findElementByXpath("//h3[@class='panel-title' and text()='Event Management']");
        findElementByXpath("//h3[@class='panel-title' and text()='Service Monitoring']");
        findElementByXpath("//h3[@class='panel-title' and text()='Performance Measurement']");
        findElementByXpath("//h3[@class='panel-title' and text()='Distributed Monitoring']");
        findElementByXpath("//h3[@class='panel-title' and text()='Additional Tools']");

        frontPage();
        clickMenuItem(adminMenuName, "Help/Support", "opennms/support/index.htm");
        findElementByXpath("//h3[@class='panel-title' and text()='Documentation']");
        findElementByXpath("//h3[@class='panel-title' and text()='Commercial Support']");
        findElementByLink("About the OpenNMS Web Console").click();
        findElementByXpath("//h3[@class='panel-title' and text()='OpenNMS Web Console']");

        frontPage();
        clickMenuItem(adminMenuName, "Log Out", "opennms/j_spring_security_logout");
        findElementById("input_j_username");
    }

    @Test
    public void verifyReportsPage() {
        // testAllTextIsPresent
        reportsPage();
        findElementByXpath("//h3[@class='panel-title' and text()='Reports']");
        findElementByXpath("//h3[@class='panel-title' and text()='Descriptions']");

        // testAllFormsArePresent()
        reportsPage();
        findElementByName("resourceGraphs");
        findElementByName("kscReports");


        // testAllLinks
        reportsPage();
        findElementByLink("Resource Graphs").click();
        findElementByXpath("//h3[contains(text()[normalize-space()], 'Standard Resource')]");
        findElementByXpath("//h3[@class='panel-title' and text()='Network Performance Data']");

        reportsPage();
        findElementByLink("KSC Performance, Nodes, Domains").click();
        findElementByXpath("//h3[@class='panel-title' and text()='Customized Reports']");
        findElementByXpath("//h3[@class='panel-title' and text()='Descriptions']");

        reportsPage();
        findElementByLink("Database Reports").click();
        pageContainsText("List reports");
        pageContainsText("View and manage pre-run reports");
        pageContainsText("Manage the batch report schedule");

        reportsPage();
        findElementByLink("Statistics Reports").click();
        findElementByXpath("//h3[@class='panel-title' and text()='Statistics Report List']");
    }

}
