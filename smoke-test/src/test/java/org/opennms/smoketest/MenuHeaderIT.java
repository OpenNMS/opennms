/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.rnorth.ducttape.unreliables.Unreliables;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MenuHeaderIT extends OpenNMSSeleniumIT {

    @Test
    public void testMenuEntries() throws Exception {
        clickMenuItem("Search", null, "element/index.jsp");
        findElementByXpath("//div[@class='card-header']/span[text()='Search for Nodes']");

        clickMenuItem("Info", "Nodes", "element/nodeList.htm");
        findElementByXpath("//div[@class='btn-toolbar']/span[text()='Nodes' or text()='Availability']");

        clickMenuItem("Info", "Assets", "asset/index.jsp");
        findElementByXpath("//div[@class='card-header']/span[text()='Search Asset Information']");

        clickMenuItem("Info", "Path Outages", "pathOutage/index.jsp");
        findElementByXpath("//div[@class='card-header']/span[text()='All Path Outages']");

        clickMenuItem("Status", "Events", "event/index");
        findElementByXpath("//div[@class='card-header']/span[text()='Event Queries']");

        clickMenuItem("Status", "Alarms", "alarm/index.htm");
        findElementByXpath("//div[@class='card-header']/span[text()='Alarm Queries']");

        clickMenuItem("Status", "Notifications", "notification/index.jsp");
        findElementByXpath("//div[@class='card-header']/span[text()='Notification queries']");

        clickMenuItem("Status", "Outages", "outage/index.jsp");
        findElementByXpath("//div[@class='card-header']/span[text()='Outage Menu']");

        clickMenuItem("Status", "Distributed Status", "distributedStatusSummary.htm");
        findElementByXpath("//div[@class='card-header']/span[contains(text(), 'Distributed Status Summary')]");

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
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//label[contains(text()[normalize-space()], 'Standard Resource')]")));

        clickMenuItem(reportsMenuName, "KSC Reports", "KSC/index.jsp");
        findElementByXpath("//div[@class='card-header']/span[text()='Customized Reports']");

        clickMenuItem(reportsMenuName, "Statistics", "statisticsReports/index.htm");
        findElementByXpath("//div[@class='card-header']/span");

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
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(), 'Selection Context')]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='navbar']//a[@name='nav-Maps-top']")));

        frontPage();
        clickMenuItem(mapsMenuName, "Geographical", "node-maps");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[text()='Show Severity >=']")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='navbar']//a[@name='nav-Maps-top']")));

        frontPage();
        final String adminMenuName = "name=nav-admin-top";
        clickMenuItem(adminMenuName, "Configure OpenNMS", "opennms/admin/index.jsp");
        findElementByXpath("//div[@class='card-header']/span[text()='OpenNMS System']");
        findElementByXpath("//div[@class='card-header']/span[text()='Provisioning']");
        findElementByXpath("//div[@class='card-header']/span[text()='Event Management']");
        findElementByXpath("//div[@class='card-header']/span[text()='Service Monitoring']");
        findElementByXpath("//div[@class='card-header']/span[text()='Performance Measurement']");
        findElementByXpath("//div[@class='card-header']/span[text()='Distributed Monitoring']");

        frontPage();
        clickMenuItem(adminMenuName, "Help", "opennms/help/index.jsp");
        findElementByXpath("//div[@class='card-header']/span[text()='Documentation']");
        clickMenuItem(adminMenuName, "Support", "opennms/support/index.htm");
        findElementByXpath("//div[@class='card-header']/span[text()='Commercial Support']");
        clickMenuItem(adminMenuName, "About", "opennms/about/index.jsp");
        findElementByXpath("//div[@class='card-header']/span[text()='Version Details']");

        Unreliables.retryUntilSuccess(60, TimeUnit.SECONDS, () -> {
            frontPage();
            Thread.sleep(200);
            clickMenuItem(adminMenuName, "Log Out", "opennms/j_spring_security_logout", 10);
            findElementById("input_j_username");
            return null;
        });
    }

    @Test
    public void verifyReportsPage() {
        // testAllTextIsPresent
        reportsPage();
        findElementByXpath("//div[@class='card-header']/span[text()='Reports']");
        findElementByXpath("//div[@class='card-header']/span[text()='Descriptions']");

        // testAllFormsArePresent()
        reportsPage();
        findElementByName("resourceGraphs");
        findElementByName("kscReports");


        // testAllLinks
        reportsPage();
        findElementByLink("Resource Graphs").click();
        findElementByXpath("//label[contains(text()[normalize-space()], 'Standard Resource')]");
        findElementByXpath("//div[@class='card-header']/span[text()='Network Performance Data']");

        reportsPage();
        findElementByLink("KSC Performance, Nodes, Domains").click();
        findElementByXpath("//div[@class='card-header']/span[text()='Customized Reports']");
        findElementByXpath("//div[@class='card-header']/span[text()='Descriptions']");

        reportsPage();
        findElementByLink("Database Reports").click();
        pageContainsText("Report Templates");
        pageContainsText("Report Schedules");
        pageContainsText("Persisted Reports");

        reportsPage();
        findElementByLink("Statistics Reports").click();
        findElementByXpath("//div[@class='card-header']/span[text()='Statistics Report List']");
    }

}
