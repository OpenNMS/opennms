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
package org.opennms.smoketest.ui;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.opennms.smoketest.selenium.AbstractOpenNMSSeleniumHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * The Vue Node Details page ({@code ui/index.html#/node/<id|foreignSource:foreignId>}), for tests
 * that start on a node's page and go on to the pages it links to.
 */
public class NodeDetailsPage {
    private static final String LINKS = "//nav[@data-test='node-details-links']";

    private final AbstractOpenNMSSeleniumHelper testCase;

    public NodeDetailsPage(final AbstractOpenNMSSeleniumHelper testCase) {
        this.testCase = Objects.requireNonNull(testCase);
    }

    /**
     * Opens the page for a node id or foreignSource:foreignId, and waits until the node is loaded.
     * A foreign id is used as given, so it must not need URL encoding.
     */
    public NodeDetailsPage open(final String node) {
        testCase.getDriver().get(testCase.getBaseUrlInternal() + "opennms/ui/index.html#/node/" + node);
        return waitUntilLoaded();
    }

    public NodeDetailsPage open(final int nodeId) {
        return open(String.valueOf(nodeId));
    }

    /** Waits until the links row shows, which the page renders once the node is loaded. */
    public NodeDetailsPage waitUntilLoaded() {
        testCase.waitForElement(By.xpath(LINKS));
        return this;
    }

    public NodeDetailsPage waitForLabel(final String label) {
        testCase.wait.until(ExpectedConditions.textToBe(By.cssSelector(".onms-node-details .heading h2"), "Node Details for " + label));
        return this;
    }

    /**
     * Opens a menu of the links row (Inventory, Monitoring, Graphs, Services, Admin) and clicks
     * one of its items.
     */
    public void clickLink(final String menu, final String item) {
        openMenu(menu);
        testCase.clickElement(By.xpath(menuXpath(menu) + "//ul//li[@aria-label='" + item + "']/div"));
    }

    /** The item labels of a menu of the links row, read at once: callers wait for the page first. */
    public List<String> getLinkLabels(final String menu) {
        openMenu(menu);
        try {
            testCase.setImplicitWait(0, TimeUnit.MILLISECONDS);
            return testCase.getDriver().findElements(By.xpath(menuXpath(menu) + "//ul//li[@role='menuitem']")).stream()
                    .map(item -> item.getAttribute("aria-label"))
                    .collect(Collectors.toList());
        } finally {
            testCase.setImplicitWait();
        }
    }

    /** Confirms the dialog the asset editor link shows first for a node from a requisition. */
    public void confirmAssetEdit() {
        testCase.clickElement(By.xpath("//div[contains(@class, 'p-dialog')]//button[normalize-space(.)='Edit Assets']"));
    }

    /** Clicks a service in the Availability panel, which opens that service's page. */
    public void clickService(final String serviceName) {
        testCase.clickElement(By.xpath("//tr[@data-test='timeline-service-row']//a[text()='" + serviceName + "']"));
    }

    /** Clicks an address in the IP Interfaces table, which opens that interface's page. */
    public void clickIpInterface(final String ipAddress) {
        testCase.clickElement(By.xpath("//*[@data-test='ip-interfaces-table']//a[@data-test='ip-address-link' and text()='" + ipAddress + "']"));
    }

    /** Selects the SNMP Interfaces tab of the Node Interfaces panel. */
    public void showSnmpInterfaces() {
        testCase.clickElement(By.xpath("//*[@role='tab' and normalize-space(.)='SNMP Interfaces']"));
        testCase.waitForElement(By.cssSelector("[data-test='snmp-interfaces-table']"));
    }

    /**
     * The cell texts of a table's first row: the IP or SNMP interfaces table. Waits until the table
     * has a row, so the "No results found" row of a table still loading does not count.
     */
    public List<String> getFirstRowCells(final String tableDataTest) {
        final By firstRowCells = By.cssSelector("[data-test='" + tableDataTest + "'] tbody tr:first-child:not(.p-datatable-empty-message) td");
        testCase.wait.until(ExpectedConditions.visibilityOfElementLocated(firstRowCells));
        return testCase.getDriver().findElements(firstRowCells).stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    private void openMenu(final String menu) {
        final By root = By.xpath(menuXpath(menu));
        testCase.waitForElement(root);
        if (!"true".equals(testCase.getDriver().findElement(root).getAttribute("aria-expanded"))) {
            testCase.clickElement(By.xpath(menuXpath(menu) + "/div"));
        }
        testCase.wait.until(ExpectedConditions.attributeToBe(root, "aria-expanded", "true"));
    }

    private static String menuXpath(final String menu) {
        return LINKS + "//ul[@role='menubar']/li[@role='menuitem' and @aria-label='" + menu + "']";
    }
}
