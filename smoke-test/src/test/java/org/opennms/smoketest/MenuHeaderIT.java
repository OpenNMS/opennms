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
import static org.junit.Assert.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opennms.smoketest.ui.framework.TextInput;
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
        LOG.debug("testMenuEntries");

        frontPage();

        WebElement foundElement = null;
        final WebDriverWait shortWait = new WebDriverWait(getDriver(), Duration.ofSeconds(1));

        clickMenuItem("searchMenu", "Search");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Search for Nodes']")));

        clickMenuItem("info", "Nodes");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='btn-toolbar']/span[text()='Nodes' or text()='Availability']")));

        clickMenuItem("info", "Assets");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@id='content']//div[@class='card-header']/span[text()='Search Asset Information']")));

        clickMenuItem("info", "Path Outages");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='All Path Outages']")));

        clickMenuItem("statusMenu", "Events");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Event Queries']")));

        clickMenuItem("statusMenu", "Alarms");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Alarm Queries']")));

        clickMenuItem("statusMenu", "Notifications");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Notification queries']")));

        clickMenuItem("statusMenu", "Outages");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Outage Menu']")));

        clickMenuItem("statusMenu", "Application");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//li[text()='Application Status']")));

        clickMenuItem("statusMenu", "Surveillance");

        // switchTo() by xpath is much faster than by ID
        //driver.switchTo().frame("surveillance-view-ui");
        driver.switchTo().frame(findElementByXpath("/html/body/div/iframe"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[text()='Surveillance view: default']")));
        driver.switchTo().parentFrame();
        frontPage();

        clickMenuItem("reportsMenu", "Charts");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("include-charts")));

        clickMenuItem("reportsMenu", "Resource Graphs");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//label[contains(text()[normalize-space()], 'Standard Resource')]")));

        clickMenuItem("reportsMenu", "KSC Reports");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Customized Reports']")));

        clickMenuItem("reportsMenu", "Statistics");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span")));

        clickMenuItem("dashboardsMenu", "Dashboard");
        driver.switchTo().frame(findElementByXpath("/html/body/div/iframe"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//span[text()='Surveillance view: default']")));
        driver.switchTo().parentFrame();
        frontPage();

        clickMenuItem("dashboardsMenu", "Ops Board");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//select[@class='v-select-select']")));

        frontPage();
        clickMenuItem("mapsMenu", "Topology");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(text(), 'Selection Context')]")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[starts-with(@id, 'opennmstopology-')]")));

        frontPage();
        clickMenuItem("administration", "Configure OpenNMS");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='OpenNMS System']")));

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Provisioning']")));

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Event Management']")));

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Service Monitoring']")));

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Performance Measurement']")));

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Distributed Monitoring']")));

        frontPage();
        clickMenuItem("helpMenu", "Help");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Documentation']")));

        clickMenuItem("helpMenu", "About");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Version Details']")));

        clickMenuItem("helpMenu", "Support");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='card-header']/span[text()='Commercial Support']")));

        // Navigation to new Vue UI page
        frontPage();
        clickMenuItem("info", "Device Configs");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("app")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='app']//span[text()='Device Configuration']")));

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
    public void verifyCentralSearch() {
        frontPage();

        // get the central search text input control and search for "Configure"
        WebElement searchInput = findElementByXpath("//div[@id='onms-central-search-control']/div[@class='onms-search-input-wrapper']//div[@class='feather-input-wrapper']//input[@class='feather-input']");
        TextInput ti = new TextInput(getDriver(), searchInput.getAttribute("id"));
        ti.setInput("Configure");

        // Get the search results, including the search results header
        List<WebElement> searchResults = findElementsByXpath("//div[@id='onms-central-search-control']/div[@class='onms-search-dropdown-wrapper']//ul[@class='feather-dropdown']/li");

        // 10 actual results plus 1 header
        assertThat(searchResults.size(), is(11));

        List<WebElement> searchResultsHeader = findElementsByXpath("//div[@class='onms-search-result-header']");
        assertThat(searchResultsHeader.size(), is(1));
        assertThat(searchResultsHeader.get(0).getText(), is("Action"));

        List<WebElement> searchResultItems = findElementsByXpath("//a[contains(@class, 'onms-search-result-item')]");
        assertThat(searchResultItems.size(), is(10));

        frontPage();

        if (false) {
            // final List<WebElement> elements = getDriver().findElements(By.xpath("//div[@id='onms-search-result']//*[contains(@class, 'list-group-item')]"));

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