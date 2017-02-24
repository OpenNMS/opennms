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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.link.Layout;
import org.opennms.features.topology.link.TopologyProvider;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
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

    private static final Logger LOG = LoggerFactory.getLogger(TopologyIT.class);

    private TopologyUIPage topologyUiPage;

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
            waitForTransition();
        }

        public void removeFromFocus() {
            try {
                ui.testCase.setImplicitWait(1, TimeUnit.SECONDS);
                getElement().findElement(By.xpath("//a[@class='icon-remove']")).click();
            } finally {
                ui.testCase.setImplicitWait();
            }
            waitForTransition();
        }

        private WebElement getElement() {
            return ui.testCase.findElementByXpath("//*/table[@class='search-token-field']"
                    + "//div[@class='search-token-label' and contains(text(),'" + label + "')]");
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj instanceof FocusedVertex) {
                FocusedVertex other = (FocusedVertex) obj;
                boolean equals = Objects.equals(getNamespace(), other.getNamespace())
                        && Objects.equals(getLabel(), other.getLabel())
                        && Objects.equals(ui, other.ui);
                return equals;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(ui, namespace, label);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("namespace", getNamespace())
                    .add("label", getLabel())
                    .toString();
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
            testCase.waitUntil(null, null, new Callable<Boolean>() {
                @Override public Boolean call() throws Exception {
                    getElement().findElement(By.xpath("//*[@class='svgIconOverlay']")).click();
                    return true;
                }
            });
        }

        private WebElement getElement() {
            return testCase.findElementByXpath("//*[@id='TopologyComponent']"
                    + "//*[@class='vertex-label' and text()='" + label + "']/..");
        }

        @Override
        public String toString() {
            return String.format("VisibleVertex[label='%s']", label);
        }

        public ContextMenu contextMenu() {
            try {
                testCase.setImplicitWait(1, TimeUnit.SECONDS);
                Actions action = new Actions(testCase.m_driver);
                action.contextClick(getElement());
                action.build().perform();
                return new ContextMenu(testCase);
            } finally {
                testCase.setImplicitWait();
            }
        }

        public PingWindow ping() {
            return new PingWindow(testCase, contextMenu()).open();
        }
    }

    public static class PingWindow {
        private final OpenNMSSeleniumTestCase testCase;
        private final ContextMenu contextMenu;

        private PingWindow(OpenNMSSeleniumTestCase testCase, ContextMenu contextMenu) {
            this.testCase = Objects.requireNonNull(testCase);
            this.contextMenu = Objects.requireNonNull(contextMenu);
        }

        private PingWindow open() {
            contextMenu.click("Ping");
            testCase.findElementByXpath("//div[@class='popupContent']//div[@class='v-window-header' and contains(text(), 'Ping')]");
            return this;
        }

        public void close() {
            testCase.findElementByXpath("//div[@class='v-window-closebox']").click();
        }
    }

    /**
     * Represents the Context Menu
     */
    public static class ContextMenu {

        private final OpenNMSSeleniumTestCase testCase;

        public ContextMenu(OpenNMSSeleniumTestCase testCase) {
            this.testCase = Objects.requireNonNull(testCase);
        }

        /**
         * Closes the context menu without clicking on an item
         */
        public void close() {
            testCase.m_driver.findElement(By.id("TopologyComponent")).click();
        }

        public void click(String... menuPath) {
            if (menuPath != null && menuPath.length > 0) {
                try {
                    testCase.setImplicitWait(1, TimeUnit.SECONDS);
                    for (String eachPath : menuPath) {
                        testCase.findElementByXpath("//*[@class='v-context-menu-container']//*[@class='v-context-menu']//*[text()='" + eachPath + "']").click();
                    }
                    waitForTransition();
                } finally {
                    testCase.setImplicitWait();
                }
            }
        }
    }

    /**
     * Represents the Breadcrumbs
     */
    public static class Breadcrumbs {

        private final OpenNMSSeleniumTestCase testCase;

        public Breadcrumbs(OpenNMSSeleniumTestCase testCase) {
            this.testCase = Objects.requireNonNull(testCase);
        }

        public List<String> getLabels() {
            try {
                testCase.setImplicitWait(1, TimeUnit.SECONDS);
                List<WebElement> breadcrumbs = testCase.m_driver.findElements(By.xpath("//*[@id='breadcrumbs']//span[@class='v-button-caption']"));
                return breadcrumbs.stream().map(eachBreadcrumb -> eachBreadcrumb.getText()).collect(Collectors.toList());
            } finally {
                testCase.setImplicitWait();
            }
        }

        public void click(String label) {
            try {
                testCase.setImplicitWait(1, TimeUnit.SECONDS);
                WebElement element = testCase.findElementByXpath("//*[@id='breadcrumbs']//*[contains(text(), '" + label + "')]");
                element.click();
                waitForTransition();
            } finally {
                testCase.setImplicitWait();
            }
        }
    }

    /**
     * Controls the workflow of the "Topology UI" Page
     */
    public static class TopologyUIPage {
        private final OpenNMSSeleniumTestCase testCase;
        private final String topologyUiUrl;

        public TopologyUIPage(OpenNMSSeleniumTestCase testCase, String baseUrl) {
            this.testCase = Objects.requireNonNull(testCase);
            this.topologyUiUrl = Objects.requireNonNull(baseUrl) + "opennms/topology";
        }

        public TopologyUIPage open() {
            testCase.m_driver.get(topologyUiUrl);
            // Wait for the "View" menu to be clickable before returning control to the test in order
            // to make sure that the page is fully loaded
            testCase.wait.until(ExpectedConditions.elementToBeClickable(getCriteriaForMenubarElement("View")));
            return this;
        }
 
        public TopologyUIPage clickOnMenuItemsWithLabels(String... labels) {
            resetMenu();
            Actions actions = new Actions(testCase.m_driver);
            for (String label : labels) {
                try {
                    // we should wait, otherwise the menu has not yet updated
                    waitForTransition();
                    WebElement menuElement = getMenubarElement(label);
                    actions.moveToElement(menuElement);
                    menuElement.click();
                } catch (Throwable e) {
                    LOG.error("Unexpected exception while clicking on menu item {}", label, e);
                    throw e;
                }
            }
            // Wait to give the menu a chance to update
            waitForTransition();
            return this;
        }

        public TopologyUIPage selectLayout(Layout layout) {
            clickOnMenuItemsWithLabels("View", layout.getLabel());
            waitForTransition();
            return this;
        }

        public TopologyUIPage selectTopologyProvider(TopologyProvider topologyProvider) {
            Objects.requireNonNull(topologyProvider);
            try {
                testCase.setImplicitWait(1, TimeUnit.SECONDS);
                clickOnMenuItemsWithLabels("View", topologyProvider.getLabel());
                waitForTransition(); // we have to wait for the UI to re-settle
                return this;
            } finally {
                testCase.setImplicitWait();
            }
        }

        public TopologyUIPage setAutomaticRefresh(boolean enabled) {
            LOG.info("setAutomaticRefresh: {} refresh", enabled ? "enabling" : "disabling");
            boolean alreadyEnabled = isMenuItemChecked("Automatic Refresh", "View");
            if ((alreadyEnabled && !enabled) || (!alreadyEnabled && enabled)) {
                LOG.info("setAutomaticRefresh: toggling setting to {} refresh", enabled ? "enable" : "disable");
                clickOnMenuItemsWithLabels("View", "Automatic Refresh");
            } else {
                LOG.info("setAutomaticRefresh: refresh is already {}", enabled ? "enabled" : "disabled");
            }
            return this;
        }

        public TopologyUIPage refreshNow() {
            testCase.findElementById("refreshNow").click();
            waitForTransition();
            return this;
        }

        public TopologyUIPage showEntireMap() {
            getShowEntireMapElement().click();
            waitForTransition();
            return this;
        }

        public TopologyUISearchResults search(String query) {
            testCase.enterText(By.xpath("//*[@id='info-panel-component']//input[@type='text']"), query);
            waitForTransition();
            return new TopologyUISearchResults(this);
        }

        public List<FocusedVertex> getFocusedVertices() {
            final List<FocusedVertex> verticesInFocus = Lists.newArrayList();
            try {
                // Reduce the timeout so we don't wait around for too long if there are no vertices in focus
                testCase.setImplicitWait(1, TimeUnit.SECONDS);
                for (WebElement el : testCase.m_driver.findElements(By.xpath("//*/table[@class='search-token-field']"))) {
                    final String namespace = el.findElement(By.xpath(".//div[@class='search-token-namespace']")).getText();
                    final String label = el.findElement(By.xpath(".//div[@class='search-token-label']")).getText();
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
            Preconditions.checkArgument(szl >= 0, "The semantic zoom level must be >= 0");
            int currentSzl = Integer.valueOf(testCase.findElementById("szlInputLabel").getText()).intValue();
            if (szl != currentSzl) {
                boolean shouldIncrease = currentSzl - szl< 0;
                WebElement button = testCase.findElementById(shouldIncrease ? "szlInBtn" : "szlOutBtn");
                for (int i=0; i < Math.abs(szl - currentSzl); i++) {
                    button.click();
                }
                waitForTransition();
            }
            return this;
        }

        /**
         * Convenient method to clear the focus.
         */
        public void clearFocus() {
            for (TopologyIT.FocusedVertex focusedVerted : getFocusedVertices()) {
                focusedVerted.removeFromFocus();
            }
            assertEquals(0, getFocusedVertices().size());
        }

        public Layout getSelectedLayout() {
            try {
                testCase.setImplicitWait(1, TimeUnit.SECONDS);
                clickOnMenuItemsWithLabels("View"); // we have to click the View menubar, otherwise the menu elements are NOT visible
                List<WebElement> elements = testCase.m_driver.findElements(By.xpath("//span[@class='v-menubar-menuitem v-menubar-menuitem-checked']"));
                for (WebElement eachElement : elements) {
                    Layout layout = Layout.createFromLabel(eachElement.getText());
                    if (layout != null) {
                        return layout;
                    }
                }
                throw new RuntimeException("No Layout is selected. This should not be possible");
            } finally {
                resetMenu(); // finally reset the menu
                testCase.setImplicitWait();
            }
        }

        public void selectLayer(String layerName) {
            Objects.requireNonNull(layerName, "The layer name cannot be null");
            try {
                testCase.setImplicitWait(1, TimeUnit.SECONDS);
                openLayerSelectionComponent();
                WebElement layerElement = testCase.findElementById("layerComponent").findElement(By.xpath("//div[text() = '" + layerName + "']"));
                layerElement.click();
                waitForTransition();
            } finally {
                testCase.setImplicitWait();
            }
        }

        public VisibleVertex findVertex(String label) {
            return getVisibleVertices().stream().filter(eachVertex -> eachVertex.getLabel().equals(label)).findFirst().orElse(null);
        }

        public String getSelectedLayer() {
            try {
                testCase.setImplicitWait(1, TimeUnit.SECONDS);
                openLayerSelectionComponent();
                WebElement selectedLayer = testCase.findElementByXpath("//div[@id='layerComponent']//div[contains(@class, 'selected')]//div[contains(@class, 'v-label')]");
                if (selectedLayer != null) {
                    return selectedLayer.getText();
                }
                return null;
            } finally {
                testCase.setImplicitWait();
            }
        }

        public Breadcrumbs getBreadcrumbs() {
            return new Breadcrumbs(testCase);
        }

        private void openLayerSelectionComponent() {
            if (!isLayoutComponentVisible()) {
                testCase.findElementById("layerToggleButton").click();
            }
        }

        /**
         * Returns if the layerToggleButton has been pressed already and the layers are visible.
         *
         * @return true if the layerToggleButton has been pressed already and the layers are visible, otherwise false
         */
        private boolean isLayoutComponentVisible() {
            WebElement layerToggleButton = testCase.findElementById("layerToggleButton");
            return layerToggleButton.getCssValue("class").contains("expanded");
        }

        private WebElement getMenubarElement(String itemName) {
            return testCase.m_driver.findElement(getCriteriaForMenubarElement(itemName));
        }

        private By getCriteriaForMenubarElement(String itemName) {
            return By.xpath("//span[@class='v-menubar-menuitem-caption' and text()='" + itemName + "']/parent::*");
        }

        private WebElement getShowEntireMapElement() {
            return testCase.findElementById("showEntireMapBtn");
        }

        private void resetMenu() {
            // The menu can act weirdly if we're already hovering over it so we mouse-over to
            // a known element off of the menu, and click a couples times just to make sure
            WebElement showEntireMap = getShowEntireMapElement();
            Actions actions = new Actions(testCase.m_driver);
            actions.moveToElement(showEntireMap);
            actions.clickAndHold();
            waitForTransition();
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

        public void defaultFocus() {
            clearFocus();
            try {
                testCase.setImplicitWait(1, TimeUnit.SECONDS);
                testCase.findElementById("defaultFocusBtn").click();
                waitForTransition();
            } finally {
                testCase.setImplicitWait();
            }
        }

        public SaveLayoutButton getSaveLayoutButton() {
            return new SaveLayoutButton(testCase);
        }
    }

    public static class SaveLayoutButton {
        private final OpenNMSSeleniumTestCase testCase;

        private SaveLayoutButton(OpenNMSSeleniumTestCase testCase) {
            this.testCase = Objects.requireNonNull(testCase);
        }

        public boolean isEnabled() {
            String disabled = getButton().getAttribute("aria-disabled");
            return !"true".equalsIgnoreCase(disabled);
        }

        public void click() {
            getButton().click();
            waitForTransition();
        }

        private WebElement getButton() {
            WebElement saveLayerButton = testCase.findElementById("saveLayerButton");
            Objects.requireNonNull(saveLayerButton);
            return saveLayerButton;
        }
    }

    public static class TopologyUISearchResults {
        private static final String XPATH = "//*/td[@role='menuitem']/div[contains(text(),'%s')]";

        private final TopologyUIPage ui;

        public TopologyUISearchResults(TopologyUIPage ui) {
            this.ui = Objects.requireNonNull(ui);
        }

        public TopologyUIPage selectItemThatContains(String substring) {
            ui.testCase.findElementByXpath(String.format(XPATH, substring)).click();
            waitForTransition();
            return ui;
        }

        public long countItemsThatContain(String substring) {
            return ui.testCase.m_driver.findElements(By.xpath(String.format(XPATH, substring))).size();
        }
    }

    @Before
    public void setUp() {
        topologyUiPage = new TopologyUIPage(this, getBaseUrl());
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
        for (Field eachField : TopologyProvider.class.getDeclaredFields()) {
            try {
                TopologyProvider topologyProvider = (TopologyProvider) eachField.get(null);
                topologyUiPage.selectTopologyProvider(topologyProvider);
            } catch (IllegalAccessException e) {
                throw Throwables.propagate(e);
            }
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

    // Verifies that the ping operation is available. See NMS-9019
    @Test
    public void verifyPingOperation() throws InterruptedException, IOException {
        // Create Dummy Node
        final String foreignSourceXML = "<foreign-source name=\"" + OpenNMSSeleniumTestCase.REQUISITION_NAME + "\">\n" +
                "<scan-interval>1d</scan-interval>\n" +
                "<detectors/>\n" +
                "<policies/>\n" +
                "</foreign-source>";
        createForeignSource(REQUISITION_NAME, foreignSourceXML);
        final String requisitionXML = "<model-import foreign-source=\"" + OpenNMSSeleniumTestCase.REQUISITION_NAME + "\">" +
                "   <node foreign-id=\"tests\" node-label=\"Dummy Node\">" +
                "       <interface ip-addr=\"127.0.0.1\" status=\"1\" snmp-primary=\"N\">" +
                "           <monitored-service service-name=\"ICMP\"/>" +
                "       </interface>" +
                "   </node>" +
                "</model-import>";
        createRequisition(REQUISITION_NAME, requisitionXML, 1);

        // Send an event to force reload of topology
        final EventBuilder builder = new EventBuilder(EventConstants.RELOAD_TOPOLOGY_UEI, getClass().getSimpleName());
        builder.setTime(new Date());
        builder.setParam(EventConstants.PARAM_TOPOLOGY_NAMESPACE, "all");
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            JAXB.marshal(builder.getEvent(), outputStream);
            sendPost("/rest/events", new String(outputStream.toByteArray()), 204);
        }
        Thread.sleep(5000); // Wait to allow the event to be processed

        // Find Node and try select ping from context menu
        topologyUiPage.selectTopologyProvider(TopologyProvider.ENLINKD);
        topologyUiPage.clearFocus();
        topologyUiPage.refreshNow();
        topologyUiPage.search("Dummy Node").selectItemThatContains("Dummy Node");
        PingWindow pingWindow = topologyUiPage.findVertex("Dummy Node").ping();
        pingWindow.close();
    }

    /**
     * This method is used to block and wait for any transitions to occur.
     * This should be used after adding or removing vertices from focus and/or
     * changing the SZL.
     */
    public static void waitForTransition() {
        try {
            // TODO: Find a better way that does not require an explicit sleep
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        }
    }
}
