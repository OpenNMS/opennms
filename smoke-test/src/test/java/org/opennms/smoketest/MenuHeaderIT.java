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
public class MenuHeaderIT extends OpenNMSSeleniumTestCase {
    @Test
    public void testMenuEntries() throws Exception {
        clickMenuItem("Search", null, "element/index.jsp");
        findElementByXpath("//h3[text()='Search for Nodes']");

        clickMenuItem("Info", "Nodes", "element/nodeList.htm");
        findElementByXpath("//h3//span[text()='Nodes' or text()='Availability']");

        clickMenuItem("Info", "Assets", "asset/index.jsp");
        findElementByXpath("//h3[text()='Search Asset Information']");

        clickMenuItem("Info", "Path Outages", "pathOutage/index.jsp");
        findElementByXpath("//h3[text()='All Path Outages']");

        clickMenuItem("Status", "Events", "event/index");
        findElementByXpath("//h3[text()='Event Queries']");

        clickMenuItem("Status", "Alarms", "alarm/index.htm");
        findElementByXpath("//h3[text()='Alarm Queries']");

        clickMenuItem("Status", "Notifications", "notification/index.jsp");
        findElementByXpath("//h3[text()='Notification queries']");

        clickMenuItem("Status", "Outages", "outage/index.jsp");
        findElementByXpath("//h3[text()='Outage Menu']");

        clickMenuItem("Status", "Distributed Status", "distributedStatusSummary.htm");
        findElementByXpath("//h3[contains(text(), 'Distributed Status Summary')]");

        clickMenuItem("Status", "Surveillance", "surveillance-view.jsp");
        // switchTo() by xpath is much faster than by ID
        //m_driver.switchTo().frame("surveillance-view-ui");
        m_driver.switchTo().frame(findElementByXpath("/html/body/div/iframe"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Surveillance view: default']")));
        m_driver.switchTo().parentFrame();
        frontPage();

        final String reportsMenuName = "name=nav-Reports-top";
        clickMenuItem(reportsMenuName, "Charts", "charts/index.jsp");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("include-charts")));

        clickMenuItem(reportsMenuName, "Resource Graphs", "graph/index.jsp");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//label[contains(text()[normalize-space()], 'Standard Resource')]")));

        clickMenuItem(reportsMenuName, "KSC Reports", "KSC/index.jsp");
        findElementByXpath("//h3[text()='Customized Reports']");

        clickMenuItem(reportsMenuName, "Statistics", "statisticsReports/index.htm");
        findElementByXpath("//h3[text()='Statistics Report List']");

        final String dashboardsMenuName = "name=nav-Dashboards-top";
        clickMenuItem(dashboardsMenuName, "Dashboard", "dashboard.jsp");
        // switchTo() by xpath is much faster than by ID
        //m_driver.switchTo().frame("surveillance-view-ui");
        m_driver.switchTo().frame(findElementByXpath("/html/body/div/iframe"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Surveillance view: default']")));
        m_driver.switchTo().parentFrame();
        frontPage();

        clickMenuItem(dashboardsMenuName, "Ops Board", "vaadin-wallboard");
        findElementByXpath("//select[@class='v-select-select']");

        frontPage();
        final String mapsMenuName = "name=nav-Maps-top";
        clickMenuItem(mapsMenuName, "Distributed", "RemotePollerMap/index.jsp");
        // switchTo() by xpath is much faster than by ID
        //m_driver.switchTo().frame("app");
        m_driver.switchTo().frame(findElementByXpath("/html/body/div/iframe"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("gwt-uid-1")));
        m_driver.switchTo().parentFrame();
        frontPage();

        clickMenuItem(mapsMenuName, "Topology", "topology");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(), 'Selection Context')]")));

        frontPage();
        clickMenuItem(mapsMenuName, "Geographical", "node-maps");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[text()='Show Severity >=']")));

        frontPage();
        final String adminMenuName = "name=nav-admin-top";
        clickMenuItem(adminMenuName, "Configure OpenNMS", "opennms/admin/index.jsp");
        findElementByXpath("//h3[text()='OpenNMS System']");
        findElementByXpath("//h3[text()='Provisioning']");
        findElementByXpath("//h3[text()='Event Management']");
        findElementByXpath("//h3[text()='Service Monitoring']");
        findElementByXpath("//h3[text()='Performance Measurement']");
        findElementByXpath("//h3[text()='Distributed Monitoring']");

        frontPage();
        clickMenuItem(adminMenuName, "Help", "opennms/help/index.jsp");
        findElementByXpath("//h3[text()='Documentation']");
        clickMenuItem(adminMenuName, "Support", "opennms/support/index.htm");
        findElementByXpath("//h3[text()='Commercial Support']");
        clickMenuItem(adminMenuName, "About", "opennms/about/index.jsp");
        findElementByXpath("//h3[text()='Version Details']");

        frontPage();
        clickMenuItem(adminMenuName, "Log Out", "opennms/j_spring_security_logout");
        findElementById("input_j_username");
    }

}
