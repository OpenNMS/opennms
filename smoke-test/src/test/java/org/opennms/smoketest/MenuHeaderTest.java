/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MenuHeaderTest extends OpenNMSSeleniumTestCase {
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
        m_driver.switchTo().frame("surveillance-view-ui");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Surveillance view: default']")));
        frontPage();

        clickMenuItem("Reports", "Charts", "charts/index.jsp");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("include-charts")));

        clickMenuItem("Reports", "Resource Graphs", "graph/index.jsp");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[contains(text(), 'Standard Resource')]")));

        clickMenuItem("Reports", "KSC Reports", "KSC/index.htm");
        findElementByXpath("//h3[text()='Customized Reports']");

        clickMenuItem("Reports", "Statistics", "statisticsReports/index.htm");
        findElementByXpath("//h3[text()='Statistics Report List']");

        clickMenuItem("Dashboards", "Dashboard", "dashboard.jsp");
        m_driver.switchTo().frame("surveillance-view-ui");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Surveillance view: default']")));
        frontPage();

        clickMenuItem("Dashboards", "Ops Board", "vaadin-wallboard");
        findElementByXpath("//select[@class='v-select-select']");

        frontPage();
        clickMenuItem("Maps", "Distributed", "RemotePollerMap/index.jsp");
        m_driver.switchTo().frame("app");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("gwt-uid-1")));

        frontPage();
        clickMenuItem("Maps", "Topology", "topology");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(), 'Last update time')]")));

        frontPage();
        clickMenuItem("Maps", "Geographical", "node-maps");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[text()='Show Severity >=']")));

        frontPage();
        clickMenuItem("Maps", "SVG", "map/index.jsp");
        findElementById("opennmsSVGMaps");
        try {
            final Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            alert.dismiss();
        } catch (final Exception e) {
        }

        frontPage();
        clickMenuItem("name=nav-admin-top", "Configure OpenNMS", BASE_URL + "opennms/admin/index.jsp");
        findElementByXpath("//h3[text()='OpenNMS System']");
        findElementByXpath("//h3[text()='Operations']");

        frontPage();
        clickMenuItem("name=nav-admin-top", "Quick-Add Node", BASE_URL + "opennms/admin/node/add.htm");
        findElementByXpath("//h3[text()='Node Quick-Add']");

        frontPage();
        clickMenuItem("name=nav-admin-top", "Help/Support", BASE_URL + "opennms/support/index.htm");
        findElementByXpath("//h3[text()='Commercial Support']");

        frontPage();
        clickMenuItem("name=nav-admin-top", "Log Out", BASE_URL + "opennms/j_spring_security_logout");
        findElementById("input_j_username");
    }

}
