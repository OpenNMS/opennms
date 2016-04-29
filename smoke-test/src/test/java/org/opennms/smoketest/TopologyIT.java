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

import java.util.Objects;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.google.common.base.Throwables;

public class TopologyIT extends OpenNMSSeleniumTestCase {

    private static final String TOPOLOGY_UI_URL = BASE_URL + "opennms/topology";

    private final TopologyUiPage topologyUiPage = new TopologyUiPage();

    private static enum Layout {
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

    private static enum TopologyProvider {
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
     * Class to control the inputs and workflow of the "Topology UI" Page
     */
    public class TopologyUiPage {
        public TopologyUiPage open() {
            m_driver.get(TOPOLOGY_UI_URL);
            return this;
        }
 
        public TopologyUiPage clickOnMenuItemsWithLabels(String... labels) {
            resetMenu();
            Actions actions = new Actions(m_driver);
            for (String label : labels) {
                WebElement menuElement = getMenubarElement(label);
                actions.moveToElement(menuElement);
                menuElement.click();
            }
            return this;
        }

        public TopologyUiPage selectLayout(Layout layout) {
            clickOnMenuItemsWithLabels("View", layout.getLabel());
            return this;
        }

        public TopologyUiPage selectTopologyProvider(TopologyProvider topologyProvider) {
            clickOnMenuItemsWithLabels("View", topologyProvider.getLabel());
            return this;
        }

        public TopologyUiPage setAutomaticRefresh(boolean enabled) {
            boolean alreadyEnabled = isMenuItemChecked("Automatic Refresh", "View");
            if ((alreadyEnabled && !enabled) || (!alreadyEnabled && enabled)) {
                clickOnMenuItemsWithLabels("View", "Automatic Refresh");
            }
            return this;
        }

        public void showEntireMap() {
            getShowEntireMapElement().click();
        }

        private WebElement getMenubarElement(String itemName) {
            return findElementByXpath("//span[@class='v-menubar-menuitem-caption' and text()='" + itemName + "']/parent::*");
        }

        private WebElement getShowEntireMapElement() {
            return findElementByXpath("//i[@class='icon-globe']");
        }

        private void resetMenu() {
            // The menu can act weirdly if we're already hovering over it so we mouse-over to
            // a known element off of the menu, and click a couples times just to make sure
            WebElement showEntireMap = getShowEntireMapElement();
            Actions actions = new Actions(m_driver);
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

            final WebElement automaticRefresh = getMenubarElement("Automatic Refresh");
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

    @Before
    public void setUp() {
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
