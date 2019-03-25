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

 import static org.junit.Assert.assertEquals;

 import java.util.concurrent.TimeUnit;

 import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.interactions.Actions;
 import org.openqa.selenium.support.ui.ExpectedConditions;

 @FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MenuHeaderIT extends OpenNMSSeleniumTestCase {
    @Test
    public void testMenuEntries() throws Exception {
//        clickMenuItem("Search", null, "element/index.jsp");
//        findElementByXpath("//div[@class='card-header']/span[text()='Search for Nodes']");
//
//        clickMenuItem("Info", "Nodes", "element/nodeList.htm");
//        findElementByXpath("//div[@class='btn-toolbar']/span[text()='Nodes' or text()='Availability']");
//
//        clickMenuItem("Info", "Assets", "asset/index.jsp");
//        findElementByXpath("//div[@class='card-header']/span[text()='Search Asset Information']");
//
//        clickMenuItem("Info", "Path Outages", "pathOutage/index.jsp");
//        findElementByXpath("//div[@class='card-header']/span[text()='All Path Outages']");
//
//        clickMenuItem("Status", "Events", "event/index");
//        findElementByXpath("//div[@class='card-header']/span[text()='Event Queries']");
//
//        clickMenuItem("Status", "Alarms", "alarm/index.htm");
//        findElementByXpath("//div[@class='card-header']/span[text()='Alarm Queries']");
//
//        clickMenuItem("Status", "Notifications", "notification/index.jsp");
//        findElementByXpath("//div[@class='card-header']/span[text()='Notification queries']");
//
//        clickMenuItem("Status", "Outages", "outage/index.jsp");
//        findElementByXpath("//div[@class='card-header']/span[text()='Outage Menu']");
//
//        clickMenuItem("Status", "Distributed Status", "distributedStatusSummary.htm");
//        findElementByXpath("//div[@class='card-header']/span[contains(text(), 'Distributed Status Summary')]");
//
//        clickMenuItem("Status", "Surveillance", "surveillance-view.jsp");
//        // switchTo() by xpath is much faster than by ID
//        //m_driver.switchTo().frame("surveillance-view-ui");
//        m_driver.switchTo().frame(findElementByXpath("/html/body/div/iframe"));
//        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Surveillance view: default']")));
//        m_driver.switchTo().parentFrame();
//        frontPage();
//
//        final String reportsMenuName = "name=nav-Reports-top";
//        clickMenuItem(reportsMenuName, "Charts", "charts/index.jsp");
//        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("include-charts")));
//
//        clickMenuItem(reportsMenuName, "Resource Graphs", "graph/index.jsp");
//        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//label[contains(text()[normalize-space()], 'Standard Resource')]")));
//
//        clickMenuItem(reportsMenuName, "KSC Reports", "KSC/index.jsp");
//        findElementByXpath("//div[@class='card-header']/span[text()='Customized Reports']");
//
//        clickMenuItem(reportsMenuName, "Statistics", "statisticsReports/index.htm");
//        findElementByXpath("//div[@class='card-header']/span");
//
//        final String dashboardsMenuName = "name=nav-Dashboards-top";
//        clickMenuItem(dashboardsMenuName, "Dashboard", "dashboard.jsp");
//        // switchTo() by xpath is much faster than by ID
//        //m_driver.switchTo().frame("surveillance-view-ui");
//        m_driver.switchTo().frame(findElementByXpath("/html/body/div/iframe"));
//        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Surveillance view: default']")));
//        m_driver.switchTo().parentFrame();
//        frontPage();
//
//        clickMenuItem(dashboardsMenuName, "Ops Board", "vaadin-wallboard");
//        findElementByXpath("//select[@class='v-select-select']");
//
//        frontPage();
//        final String mapsMenuName = "name=nav-Maps-top";
//        clickMenuItem(mapsMenuName, "Topology", "topology");
//        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(), 'Selection Context')]")));
//        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='navbar']//a[@name='nav-Maps-top']")));
//
//        frontPage();
//        clickMenuItem(mapsMenuName, "Geographical", "node-maps");
//        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[text()='Show Severity >=']")));
//        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='navbar']//a[@name='nav-Maps-top']")));
//
//        frontPage();
//        findElementByXpath("//nav//a[contains(@title, 'Configure OpenNMS') and contains(@href, 'opennms/admin/index.jsp')]").click();
//        findElementByXpath("//div[@class='card-header']/span[text()='OpenNMS System']");
//        findElementByXpath("//div[@class='card-header']/span[text()='Provisioning']");
//        findElementByXpath("//div[@class='card-header']/span[text()='Event Management']");
//        findElementByXpath("//div[@class='card-header']/span[text()='Service Monitoring']");
//        findElementByXpath("//div[@class='card-header']/span[text()='Performance Measurement']");
//        findElementByXpath("//div[@class='card-header']/span[text()='Distributed Monitoring']");

        frontPage();
        final String helpMenuName = "nav-help-top";
        clickMenuItemWithIcon(helpMenuName, "Help", "opennms/help/index.jsp");
        findElementByXpath("//div[@class='card-header']/span[text()='Documentation']");
        clickMenuItemWithIcon(helpMenuName, "Support", "opennms/support/index.htm");
        findElementByXpath("//div[@class='card-header']/span[text()='Commercial Support']");
        clickMenuItemWithIcon(helpMenuName, "About", "opennms/about/index.jsp");
        findElementByXpath("//div[@class='card-header']/span[text()='Version Details']");

        frontPage();
        final String userMenuName = "nav-user-top";
        clickMenuItemWithIcon(userMenuName, "Log Out", "opennms/j_spring_security_logout");
        findElementById("input_j_username");
    }

    // We need this helper method, as with the icon used in some menus the contains(text(),...) method does not work anymore
    private void clickMenuItemWithIcon(String menuEntryName, String submenuText, String submenuHref) {
        try {
            setImplicitWait(5, TimeUnit.SECONDS);
            final Actions action = new Actions(m_driver);
            final WebElement headerElement = findElementByXpath(String.format("//li/a[@name='%s']", menuEntryName));

            // Move to element to make sub menu visible
            action.moveToElement(headerElement).perform();

            // Wait until sub menu element is visible, then click
            final WebElement submenuElement = headerElement.findElement(By.xpath(String.format("./../div/a[contains(@class, 'dropdown-item') and contains(@href, '%s')]", submenuHref)));
            this.wait.until(ExpectedConditions.visibilityOf(submenuElement));
            assertEquals(submenuText, submenuElement.getText().trim());
            submenuElement.click();

        } finally {
            setImplicitWait();
        }
    }
}
