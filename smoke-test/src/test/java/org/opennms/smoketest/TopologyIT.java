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

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class TopologyIT extends OpenNMSSeleniumTestCase {

    private static final String TOPOLOGY_UI_URL = BASE_URL + "opennms/topology";

    private final TopologyUiPage topologyUiPage = new TopologyUiPage();

    private final static List<String> availableLayouts = Lists.newArrayList(
            "Circle Layout",
            "D3 Layout",
            "FR Layout",
            "Hierarchy Layout",
            "ISOM Layout",
            "KK Layout",
            "Manual Layout",
            "Real Ultimate Layout",
            "Spring Layout"
            );

    /**
     * Class to control the inputs and workflow of the "Topology UI" Page
     */
    private class TopologyUiPage {
        public TopologyUiPage open() {
            m_driver.get(TOPOLOGY_UI_URL);
            return this;
        }
 
        public TopologyUiPage selectLayout(String layoutName) {
            clickItemInMenuBar("View");
            clickItemInMenuBar(layoutName);
            showEntireMap();
            return this;
        }

        private void clickItemInMenuBar(String itemName) {
            findElementByXpath("//span[@class='v-menubar-menuitem-caption' and text()='" + itemName + "']").click();
        }

        private void showEntireMap() {
            findElementByXpath("//i[@class='icon-globe']").click();
        }
    }

    @Before
    public void setUp() {
        topologyUiPage.open();
    }

    @Test
    public void canSelectLayouts() throws InterruptedException {
        for (String layout : availableLayouts) {
            topologyUiPage.selectLayout(layout);
        }
    }
}
