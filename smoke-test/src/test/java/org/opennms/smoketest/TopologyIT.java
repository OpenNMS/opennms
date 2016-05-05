/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * Generic tests for the Topology UI that do not require any elements
 * to be present in the database.
 *
 * Provides utilities that can be used for more specific tests.
 *
 * @author jwhite
 */
public class TopologyIT extends OpenNMSSeleniumTestCase {

    private static final String TOPOLOGY_UI_URL = BASE_URL + "opennms/topology";

    private TopologyUIPage topologyUiPage;

    /**
     * All known layout algorithms.
     */
    public static enum Layout {
        CIRCLE("Circle Layout"),
        D3("D3 Layout"),
        FR("FR Layout"),
        HIERARCHY("Hierarchy Layout"),
        ISOM("ISOM Layout"),
        KK("KK Layout"),
        REAL("Real Ultimate Layout"),
        SPRING("Spring Layout");

        private final String label;

        Layout(String label) {
            this.label = Objects.requireNonNull(label);
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * All known topology providers.
     */
    public static enum TopologyProvider {
        APPLICATION("Application"),
        BUSINESSSERVICE("Business Services"),
        ENLINKD("Enhanced Linkd"),
        VMWARE("VMware");

        private final String label;

        TopologyProvider(String label) {
            this.label = Objects.requireNonNull(label);
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * Represents a vertex that is focused using
     * criteria listed bellow the search bar.
     */
    public static class FocusedVertex {
        private final TopologyUIPage ui;
        private final String namespace;
        private final String label;

        public FocusedVertex(TopologyUIPage ui, String namespace, String label) {
            this.ui = Objects.requireNonNull(ui);
            this.namespace = Objects.requireNonNull(namespace);
            this.label = Objects.requireNonNull(label);
        }

        public String getNamespace() {
            return namespace;
        }

        public String getLabel() {
            return label;
        }

        public void centerOnMap() {
            getElement().findElement(By.xpath("//a[@class='icon-location-arrow']")).click();
            ui.waitForTransition();
        }

        public void removeFromFocus() {
            getElement().findElement(By.xpath("//a[@class='icon-remove']")).click();
            ui.waitForTransition();
        }

        private WebElement getElement() {
            return ui.testCase.findElementByXpath("//*/table[@class='search-token-field']"
                    + "//div[@class='search-token-label' and contains(text(),'" + label + "')]");
        }
    }

    /**
     * Represents a vertex that is currently visible on the map.
     */
    public static class VisibleVertex {
        private final OpenNMSSeleniumTestCase testCase;
        private final String label;

        public VisibleVertex(OpenNMSSeleniumTestCase testCase, String label) {
            this.testCase = Objects.requireNonNull(testCase);
            this.label = Objects.requireNonNull(label);
        }

        public String getLabel() {
            return label;
        }

        public Point getLocation() {
            return getElement().getLocation();
        }

        public void select() {
            getElement().findElement(By.xpath("//*[@class='svgIconOverlay']")).click();
        }

        private WebElement getElement() {
            return testCase.findElementByXpath("//*[@id='TopologyComponent']"
                    + "//*[@class='vertex-label' and text()='" + label + "']/..");
        }

        @Override
        public String toString() {
            return String.format("VisibleVertex[label='%s']", label);
        }
    }

    /**
     * Controls the workflow of the "Topology UI" Page
     */
    public static class TopologyUIPage {
        private final OpenNMSSeleniumTestCase testCase;

        public TopologyUIPage(OpenNMSSeleniumTestCase testCase) {
            this.testCase = Objects.requireNonNull(testCase);
        }

        public TopologyUIPage open() {
            testCase.m_driver.get(TOPOLOGY_UI_URL);
            // Wait for the "View" menu to be clickable before returning control to the test in order
            // to make sure that the page is fully loaded
            testCase.wait.until(ExpectedConditions.elementToBeClickable(getCriteriaForMenubarElement("View")));
            return this;
        }
 
        public TopologyUIPage clickOnMenuItemsWithLabels(String... labels) {
            resetMenu();
            Actions actions = new Actions(testCase.m_driver);
            for (String label : labels) {
                WebElement menuElement = getMenubarElement(label);
                actions.moveToElement(menuElement);
                menuElement.click();
            }
            return this;
        }

        public TopologyUIPage selectLayout(Layout layout) {
            clickOnMenuItemsWithLabels("View", layout.getLabel());
            return this;
        }

        public TopologyUIPage selectTopologyProvider(TopologyProvider topologyProvider) {
            clickOnMenuItemsWithLabels("View", topologyProvider.getLabel());
            return this;
        }

        public TopologyUIPage setAutomaticRefresh(boolean enabled) {
            boolean alreadyEnabled = isMenuItemChecked("Automatic Refresh", "View");
            if ((alreadyEnabled && !enabled) || (!alreadyEnabled && enabled)) {
                clickOnMenuItemsWithLabels("View", "Automatic Refresh");
            }
            return this;
        }

        public TopologyUIPage showEntireMap() {
            getShowEntireMapElement().click();
            waitForTransition();
            return this;
        }

        public TopologyUISearchResults search(String query) {
            testCase.enterText(By.xpath("//*[@id='info-panel-component']//input[@type='text']"), query);
            return new TopologyUISearchResults(this);
        }

        public List<FocusedVertex> getFocusedVertices() {
            final List<FocusedVertex> verticesInFocus = Lists.newArrayList();
            try {
                // Reduce the timeout so we don't wait around for too long if there are no vertices in focus
                testCase.setImplicitWait(1, TimeUnit.SECONDS);
                for (WebElement el : testCase.m_driver.findElements(By.xpath("//*/table[@class='search-token-field']"))) {
                    final String namespace = el.findElement(By.xpath("//div[@class='search-token-namespace']")).getText();
                    final String label = el.findElement(By.xpath("//div[@class='search-token-label']")).getText();
                    verticesInFocus.add(new FocusedVertex(this, namespace, label));
                }
            } finally {
                testCase.setImplicitWait();
            }
            return verticesInFocus;
        }

        public List<VisibleVertex> getVisibleVertices() {
            final List<VisibleVertex> visibleVertices = Lists.newArrayList();
            try {
                // Reduce the timeout so we don't wait around for too long if there are no visible vertices
                testCase.setImplicitWait(1, TimeUnit.SECONDS);
                for (WebElement el : testCase.m_driver.findElements(By.xpath("//*[@id='TopologyComponent']//*[@class='vertex-label']"))) {
                    visibleVertices.add(new VisibleVertex(testCase, el.getText()));
                }
            } finally {
                testCase.setImplicitWait();
            }
            return visibleVertices;
        }

        public int getSzl() {
            return Integer.parseInt(testCase.findElementById("szlInputLabel").getText());
        }

        public TopologyUIPage setSzl(int szl) {
            testCase.enterText(By.id("szlInputLabel"), Integer.toString(szl));
            waitForTransition();
            return this;
        }

        /**
         * This method is used to block and wait for any transitions to occur.
         * This should be used after adding or removing vertices from focus and/or
         * changing the SZL.
         */
        private void waitForTransition() {
            try {
                // TODO: Find a better way that does not require an explicit sleep
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw Throwables.propagate(e);
            }
        }

        private WebElement getMenubarElement(String itemName) {
            return testCase.m_driver.findElement(getCriteriaForMenubarElement(itemName));
        }

        private By getCriteriaForMenubarElement(String itemName) {
            return By.xpath("//span[@class='v-menubar-menuitem-caption' and text()='" + itemName + "']/parent::*");
        }

        private WebElement getShowEntireMapElement() {
            return testCase.findElementByXpath("//i[@class='icon-globe']");
        }

        private void resetMenu() {
            // The menu can act weirdly if we're already hovering over it so we mouse-over to
            // a known element off of the menu, and click a couples times just to make sure
            WebElement showEntireMap = getShowEntireMapElement();
            Actions actions = new Actions(testCase.m_driver);
            actions.moveToElement(showEntireMap);
            actions.clickAndHold();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw Throwables.propagate(e);
            }
            actions.release();
            showEntireMap.click();
        }

        private boolean isMenuItemChecked(String itemName, String... path) {
            clickOnMenuItemsWithLabels(path);

            final WebElement automaticRefresh = getMenubarElement(itemName);
            final String cssClasses = automaticRefresh.getAttribute("class");
            if (cssClasses != null) {
                if (cssClasses.contains("-unchecked")) {
                    return false;
                } else if (cssClasses.contains("-checked")) {
                    return true;
                } else {
                    throw new RuntimeException("Unknown CSS classes '" + cssClasses + "'."
                                + " Unable to determine if the item is checked or unchecked.");
                }
            } else {
                throw new RuntimeException("Element has no CSS classes!"
                        + " Unable to determine if the item is checked or unchecked.");
            }
        }
    }

    public static class TopologyUISearchResults {
        private final TopologyUIPage ui;

        public TopologyUISearchResults(TopologyUIPage ui) {
            this.ui = Objects.requireNonNull(ui);
        }

        public TopologyUIPage selectItemThatContains(String substring) {
            ui.testCase.findElementByXpath("//*/td[@role='menuitem']/div[contains(text(),'" + substring + "')]").click();
            ui.waitForTransition();
            return ui;
        }
    }

    @Before
    public void setUp() {
        topologyUiPage = new TopologyUIPage(this);
        topologyUiPage.open();
    }

    @Test
    public void canSelectKnownLayouts() throws InterruptedException {
        for (Layout layout : Layout.values()) {
            topologyUiPage.selectLayout(layout);
        }
    }

    @Test
    public void canSelectKnownTopologyProviders() throws InterruptedException {
        for (TopologyProvider topologyProvider : TopologyProvider.values()) {
            topologyUiPage.selectTopologyProvider(topologyProvider);
        }
    }

    @Test
    public void canToggleAutomaticRefresh() {
        // Disable
        topologyUiPage.setAutomaticRefresh(false)
            .setAutomaticRefresh(false)
            .setAutomaticRefresh(false);

        // Enable
        topologyUiPage.setAutomaticRefresh(true)
            .setAutomaticRefresh(true)
            .setAutomaticRefresh(true);

        // Enable/Disable
        topologyUiPage.setAutomaticRefresh(false)
            .setAutomaticRefresh(true)
            .setAutomaticRefresh(false)
            .setAutomaticRefresh(true);
    }
}
