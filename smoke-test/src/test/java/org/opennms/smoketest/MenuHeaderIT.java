/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.smoketest;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opennms.smoketest.ui.framework.search.CentralSearch;
import org.opennms.smoketest.ui.framework.search.result.ContextSearchResult;
import org.opennms.smoketest.ui.framework.search.result.SearchContext;
import org.opennms.smoketest.ui.framework.search.result.SearchResult;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MenuHeaderIT extends OpenNMSSeleniumIT {
    private static final Logger LOG = LoggerFactory.getLogger(MenuHeaderIT.class);

    @Test
    public void testMenuEntries() throws Exception {
        // See opennms-webapp/src/main/webapp/WEB-INF/menu-template.json for the menu template
        LOG.debug("testMenuEntries");

        frontPage();

        WebElement foundElement = null;

        // Dashboards Menu
        clickMenuItem("dashboardsMenu", "Wallboard");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[starts-with(@id, 'opennmsvaadinwallboard-')]")));

        frontPage();
        clickMenuItem("dashboardsMenu", "Heatmap");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='card-header']/a[starts-with(text(), 'Alarm Heatmap')]")));

        clickMenuItem("dashboardsMenu", "Trends");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Trend']")));

        clickMenuItem("dashboardsMenu", "Charts");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("include-charts")));

        clickMenuItem("dashboardsMenu", "Database Reports");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@data-name='report-templates']")));

        clickMenuItem("dashboardsMenu", "Metrics Statistics (statsd)");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span")));

        clickMenuItem("dashboardsMenu", "Metrics Dashboard (KSC Reports)");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Customized Reports']")));

        clickMenuItem("dashboardsMenu", "Surveillance Dashboard");
        driver.switchTo().frame(findElementByXpath("/html/body/div/iframe"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[text()='Surveillance view: default']")));

        driver.switchTo().parentFrame();
        frontPage();

        // Inventory Menu
        // Note, some items are below under Vue UI checks
        clickMenuItem("inventoryMenu", "Nodes");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='btn-toolbar']/span[text()='Nodes' or text()='Availability']")));

        clickMenuItem("inventoryMenu", "Assets");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='content']//div[@class='card-header']/span[text()='Search Asset Information']")));

        clickMenuItem("inventoryMenu", "Search Inventory");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Search for Nodes']")));

        // Monitoring Menu
        clickMenuItem("monitoringMenu", "Applications");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//li[text()='Application Status']")));

        clickMenuItem("monitoringMenu", "Alarms");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Alarm Queries']")));

        clickMenuItem("monitoringMenu", "Outages");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Outage Menu']")));

        clickMenuItem("monitoringMenu", "Events");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Event Queries']")));

        clickMenuItem("monitoringMenu", "Path Outages");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='All Path Outages']")));

        clickMenuItem("monitoringMenu", "Surveillance View");
        // switchTo() by xpath is much faster than by ID
        driver.switchTo().frame(findElementByXpath("/html/body/div/iframe"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[text()='Surveillance view: default']")));
        driver.switchTo().parentFrame();
        frontPage();

        // Metrics Menu
        clickMenuItem("metricsMenu", "Resource Graphs");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Resource Graphs')]")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//label[contains(text()[normalize-space()], 'Standard Resource Performance Reports')]")));

        // Distributed Monitoring
        clickMenuItem("distributedMonitoringMenu", "Manage Minions");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Manage Minions')]")));

        clickMenuItem("distributedMonitoringMenu", "Manage Applications");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Applications')]")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Applications']")));

        clickMenuItem("distributedMonitoringMenu", "Manage Monitoring Locations");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Monitoring Locations')]")));

        // Manage Inventory Menu
        clickMenuItem("manageInventoryMenu", "Provisioning Requisitions");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Provisioning Requisitions')]")));

        clickMenuItem("manageInventoryMenu", "Scheduled Node Discovery");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Modify Configuration')]")));

        clickMenuItem("manageInventoryMenu", "One-shot Node Discovery");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Create Discovery Scan')]")));

        // quick-add node screen
        clickMenuItem("manageInventoryMenu", "Add a Single Node");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Quick-Add Node')]")));

        clickMenuItem("manageInventoryMenu", "Delete Nodes");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Delete Nodes']")));

        clickMenuItem("manageInventoryMenu", "Manage Business Services");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Business Services')]")));
        frontPage();

        // User Management Menu
        clickMenuItem("userManagementMenu", "Manage Users");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'User List')]")));

        clickMenuItem("userManagementMenu", "Manage Groups");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Group List')]")));

        clickMenuItem("userManagementMenu", "Manage On-call Roles");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Role List')]")));

        // Integrations Menu
        clickMenuItem("integrationsMenu", "SNMP Agent Configuration");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Configure SNMP by IP')]")));

        clickMenuItem("integrationsMenu", "Geocoding Services");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Geocoder Configuration')]")));

        clickMenuItem("integrationsMenu", "Grafana PDF Reporting");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Endpoint Configuration')]")));

        // Omitting Zenith Connect for now

        // Tools Menu
        clickMenuItem("toolsMenu", "SNMP MIB Compiler");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'SNMP MIB Compiler')]")));

        clickMenuItem("toolsMenu", "JMX Metric Configuration Generator");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'JMX Configuration Generator')]")));

        clickMenuItem("toolsMenu", "Import/Export Node Asset Information");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Import/Export Assets')]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Import and Export Assets']")));

        clickMenuItem("toolsMenu", "Send Custom Events");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Send Event')]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Send Event to OpenNMS']")));

        // Administration Menu
        clickMenuItem("administrationMenu", "Surveillance Categories");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Surveillance Categories']")));

        clickMenuItem("administrationMenu", "Configure Thresholds");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Threshold Configuration']")));

        // Omitting 'Flow Classification' for now - needs Flows role

        clickMenuItem("administrationMenu", "Notifications");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Notification queries']")));

        clickMenuItem("administrationMenu", "Customize Events");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Manage Events Configuration')]")));

        clickMenuItem("administrationMenu", "Customize Data Collection Groups");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Manage SNMP Collections and Data Collection Groups')]")));

        clickMenuItem("administrationMenu", "Manage SNMP Data Collection per Interface");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Manage SNMP Data Collection per Interface']")));

        clickMenuItem("administrationMenu", "Scheduled Outages");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Scheduled Outages')]")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/h4[text()='Scheduled Outages']")));

        clickMenuItem("administrationMenu", "Wallboard Configuration");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Ops Board Configuration')]")));

        clickMenuItem("administrationMenu", "Product Update Enrollment");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Product Update Enrollment')]")));

        clickMenuItem("administrationMenu", "Configure OpenNMS");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='OpenNMS System']")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Provisioning']")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Event Management']")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Service Monitoring']")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Performance Measurement']")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Distributed Monitoring']")));

        // Internal Logs Menu
        clickMenuItem("internalLogsMenu", "Instrumentation Log Reader");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Instrumentation Log Reader')]")));

        // API Documentation Menu
        // Omit clicking for now, some of these are external links
        foundElement = findMenuItemLink("apiDocumentationMenu", "REST Open API Documentation");
        assertNotNull("apiDocumentationMenu / REST Open API Documentation", foundElement);

        foundElement = findMenuItemLink("apiDocumentationMenu", "REST API Reference Documentation");
        assertNotNull("apiDocumentationMenu / REST API Reference Documentation", foundElement);

        foundElement = findMenuItemLink("apiDocumentationMenu", "Source Code");
        assertNotNull("apiDocumentationMenu / Source Code", foundElement);

        // Help Documentation menu
        clickMenuItem("helpDocumentationMenu", "Help");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Help')]")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Documentation']")));

        clickMenuItem("helpDocumentationMenu", "About");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'About')]")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Version Details']")));

        clickMenuItem("helpDocumentationMenu", "Support");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'Support')]")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Commercial Support']")));

        // Support Menu
        foundElement = findMenuItemLink("supportMenu", "Professional Support");
        assertNotNull("supportMenu / Professional Support", foundElement);

        foundElement = findMenuItemLink("supportMenu", "Chat");
        assertNotNull("supportMenu / Chat", foundElement);

        foundElement = findMenuItemLink("supportMenu", "Community Support (Discourse)");
        assertNotNull("supportMenu / Community Support (Discourse)", foundElement);

        foundElement = findMenuItemLink("supportMenu", "Public Issue Tracker");
        assertNotNull("supportMenu / Public Issue Tracker", foundElement);

        clickMenuItem("supportMenu", "Generate System Report");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ol[@class='breadcrumb']/li[contains(text()[normalize-space()], 'System Reports')]")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-body']//div[@class='form-group']/input[@type='submit' and @value='Generate System Report']")));

        // Omitting for now - need to fix!
        // Vaadin Topology page
        frontPage();
        clickTopMenuItem("topologiesMenu");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(text(), 'Selection Context')]")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[starts-with(@id, 'opennmstopology-')]")));

        // Navigation on Vue UI pages
        frontPage();
        clickMenuItem("inventoryMenu", "Structured Node List");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='app']//div[@class='card']//span[text()='Node List']")));

        clickMenuItem("inventoryMenu", "Device Configs");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='app']//div[@class='link']/a[text()='Device Config Backup']")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='app']//span[text()='Device Configuration']")));

        clickMenuItem("toolsMenu", "Secure Credentials Vault");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='app']//div[@class='link']/a[text()='Secure Credentials Vault']")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='app']//div[@class='scv-container']//p[text()='Add Credentials']")));

        // Omitting for now - need ROLE_FILESYSTEM_EDITOR
        // clickMenuItem("toolsMenu", "File Editor");
        // wait.until(ExpectedConditions.presenceOfElementLocated(By.id("app")));
        // wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='app']//span[text()='File Editor']")));

        clickMenuItem("integrationsMenu", "External Requisitions");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='app']//div[@class='link']/a[text()='External Requisitions and Thread Pools']")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='app']//h2[text()='External Requisitions and Thread Pools']")));

        clickMenuItem("administrationMenu", "Usage Statistics Sharing");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='app']//div[@class='link']/a[text()='Usage Statistics Collection']")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='app']//div[@class='usage-stats-container']//span[text()='Usage Statistics Sharing']")));

        clickMenuItem("internalLogsMenu", "Log Viewer");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='app']//div[@class='link']/a[text()='Logs']")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='app']//div[@class='logs-sidebar']")));

        // Omitting for now - need to fix!
        // Geographical map page
        frontPage();
        clickTopMenuItem("mapsMenu");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='app']//div[@class='geo-map']")));

        // Omitting this for now - it takes too long for the Swagger API page to display
        // clickMenuItem("apiDocumentationMenu", "REST Open API Documentation");
        // final WebDriverWait longerWait = new WebDriverWait(getDriver(), Duration.ofSeconds(2));
        // longerWait.until(ExpectedConditions.presenceOfElementLocated(By.id("app")));
        // wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='app']//div[@class='link']/a[text()='Endpoints']")));
        // wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='api-title' and contains(text()[normalize-space()], 'OpenNMS V2 RESTful API')]")));

        Unreliables.retryUntilSuccess(60, TimeUnit.SECONDS, () -> {
            frontPage();
            Thread.sleep(200);

            logout();

            findElementById("input_j_username");
            return null;
        });
    }

    @Test
    public void testLogout() {
        Unreliables.retryUntilSuccess(60, TimeUnit.SECONDS, () -> {
            frontPage();
            Thread.sleep(200);

            logout();

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

    @Test
    public void testSelfServiceMenu() {
        LOG.debug("In testSelfServiceMenu");

        clickChangePassword();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='card-header']/span[contains(text()[normalize-space()], 'Please enter the old and new passwords and confirm.')]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//form[@name='goForm']//label[contains(text()[normalize-space()], 'Current Password')]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//form[@name='goForm']//label[contains(text()[normalize-space()], 'New Password')]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//form[@name='goForm']//label[contains(text()[normalize-space()], 'Confirm New Password')]")));
    }

    @Test
    public void verifyCentralSearch() {
        LOG.debug("In verifyCentralSearch");

        frontPage();

        // get the central search text input control and search for "Configure"
        WebElement searchInput = findElementByXpath("//div[@id='onms-central-search-control']/div[@class='onms-search-input-wrapper']/input[@class='search-input']");
        searchInput.sendKeys("Configure");

        // Get the search result context header
        List<WebElement> searchHeaderResults = findElementsByXpath("//div[@id='onms-central-search-control']/div[@class='search-results-dropdown']/div[@class='search-category']");
        assertThat(searchHeaderResults.size(), is(1));

        List<WebElement> searchResultsHeaderElem = findElementsByXpath("//div[@id='onms-central-search-control']/div[@class='search-results-dropdown']/div[@class='search-category']/div[@class='onms-search-result-header']");
        assertThat(searchResultsHeaderElem .size(), is(1));
        assertThat(searchResultsHeaderElem .get(0).getText(), is("Action"));

        // Get the search result items
        List<WebElement> searchResultItems = findElementsByXpath("//div[@id='onms-central-search-control']/div[@class='search-results-dropdown']/div[@class='search-result-item']");

        // 10 actual results
        assertThat(searchResultItems.size(), is(10));

        // TODO:
        // - load more search results
        // - possibly remove CentralSearch code (ui/framework/search) if no longer being used

        frontPage();

        // TODO: Will probably remove this code
        if (false) {
            // Kick off search
            final SearchResult searchResult = new CentralSearch(getDriver()).search("Configure");
            assertThat(searchResult.size(), is(10L));

            // TODO!
            // Load more elements
            final ContextSearchResult contextSearchResult = searchResult.forContext(SearchContext.Action);
            assertThat(contextSearchResult.hasMore(), is(true));
            contextSearchResult.loadMore();
            assertThat(contextSearchResult.size(), is(14L));

            // Select last element from the now loaded elements
            contextSearchResult.getItem("Configure Users").click();
            getDriver().getCurrentUrl().endsWith("/opennms/admin/userGroupView/users/list.jsp");

            // Go back to start page
            new CentralSearch(getDriver()).search("Home").getSingleItem().click();
            getDriver().getCurrentUrl().endsWith("/opennms/index.jsp");
        }
    }

    // We need this helper method, as with the icon used in some menus the contains(text(),...) method does not work anymore
    private void clickMenuItemWithIcon(String menuEntryName, String submenuText, String submenuHref) {
        LoggerFactory.getLogger(getClass()).debug("clickMenuItemWithIcon: menuEntryName={}, submenuText={}, submenuHref={}", menuEntryName, submenuText, submenuHref);

        // Repeat the process altering the offset slightly each time
        final AtomicInteger offset = new AtomicInteger(10);
        final WebDriverWait shortWait = new WebDriverWait(getDriver(), Duration.ofSeconds(1));

        try {
            setImplicitWait(5, TimeUnit.SECONDS);
            Unreliables.retryUntilSuccess(30, TimeUnit.SECONDS, () -> {
                final Actions action = new Actions(getDriver());
                final WebElement headerElement = findElementByXpath(String.format("//li/a[@name='%s']", menuEntryName));

                // Move to element to make sub menu visible
                action.moveToElement(headerElement, offset.get(), offset.get()).perform();
                if (offset.incrementAndGet() > 10) {
                    offset.set(0);
                }

                // Wait until sub menu element is visible, then click
                final WebElement submenuElement = headerElement.findElement(By.xpath(String.format("./../div/a[contains(@class, 'dropdown-item') and contains(@href, '%s')]", submenuHref)));
                shortWait.until(ExpectedConditions.visibilityOf(submenuElement));
                assertEquals(submenuText, submenuElement.getText().trim());
                submenuElement.click();
                return null;
            });
        } finally {
            setImplicitWait();
        }
    }
}