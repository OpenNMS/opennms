/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.topo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.link.TopologyProvider;
import org.opennms.smoketest.BSMAdminIT;
import org.opennms.smoketest.BSMAdminIT.BsmAdminPage;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.TopologyIT.TopologyUIPage;
import org.opennms.smoketest.TopologyIT.VisibleVertex;

import com.google.common.collect.Lists;

/**
 * Tests the 'Business Services' topology provider.
 *
 * @author jwhite
 */
public class BSMTopologyIT extends OpenNMSSeleniumTestCase {

    private BsmAdminPage bsmAdminPage;
    private TopologyUIPage topologyUiPage;
    private List<String> businessServiceNames = new ArrayList<>();

    @Before
    public void setUp() throws InterruptedException {
        bsmAdminPage = new BsmAdminPage(this);
        topologyUiPage = new TopologyUIPage(this, getBaseUrl());

        businessServiceNames = createChainOfBusinessServices(5);

        // Open up the Topology UI and select the Business Services topology provider
        topologyUiPage.open();
        topologyUiPage.selectTopologyProvider(TopologyProvider.BUSINESS_SERVICE);
    }

    @After
    public void tearDown() {
        deleteBusinessServices(businessServiceNames);
    }

    @Test
    public void canViewVertices() {
        // Remove any existing vertices from focus
        topologyUiPage.clearFocus();

        // Search for and select the first business service in our list
        final String businessServiceName = businessServiceNames.get(0);
        topologyUiPage.search(businessServiceName.substring(0,  12)).selectItemThatContains(businessServiceName);

        // We should have a single vertex in focus
        assertEquals(1, topologyUiPage.getFocusedVertices().size());

        // Make sure the SZL is set to 1
        topologyUiPage.setSzl(1);

        // We should have two vertices visible
        List<VisibleVertex> visibleVertices = topologyUiPage.getVisibleVertices();
        assertEquals(2, visibleVertices.size());

        // The two vertices visible, should have the labels from the first two business services
        Set<String> vertexNames = visibleVertices.stream()
            .map(v -> v.getLabel())
            .collect(Collectors.toSet());
        assertThat(vertexNames, hasItems(businessServiceNames.get(0), businessServiceNames.get(1)));

        // Now bump up the SZL
        topologyUiPage.setSzl(4);

        // We should have five vertices visible
        visibleVertices = topologyUiPage.getVisibleVertices();
        assertEquals(5, visibleVertices.size());

        // Sort the vertices in the same order as the business services
        List<VisibleVertex> sortedVertices = Lists.newArrayList();
        for (String businessService : businessServiceNames) {
            for (VisibleVertex visibleVertex : visibleVertices) {
                if (businessService.equals(visibleVertex.getLabel())) {
                    sortedVertices.add(visibleVertex);
                    break;
                }
            }
        }
        String msg = String.format("Could not find all vertices for: %s, found: %s, candidates: %s",
                businessServiceNames, sortedVertices, visibleVertices);
        assertEquals(msg, 5, sortedVertices.size());

        // By default, we should be using the hierarchical layout,
        // so all of the vertices should be aligned horizontally
        int expectedX = sortedVertices.get(0).getLocation().getX();
        for (VisibleVertex visibleVertex : sortedVertices) {
            assertEquals(visibleVertex + "is not at the expected location.", expectedX, visibleVertex.getLocation().getX());
        }

        // and every vertex should be lower than the last
        Integer expectedY = -1;
        for (VisibleVertex visibleVertex : sortedVertices) {
            Integer currentY = visibleVertex.getLocation().getY();
            assertThat(visibleVertex.toString(), currentY, greaterThan(expectedY));
            expectedY = currentY;
        }

        // Now verify that every vertex can be selected
        sortedVertices.stream()
            .forEach(v -> v.select());
    }

    // See NMS-8542
    @Test
    public void canChangeFocusWhenInSimulationMode() {
        // Ensure that simulation mode is disabled and enable it afterwards
        Assert.assertEquals(Boolean.FALSE, topologyUiPage.isSimulationModeEnabled());
        topologyUiPage.clickOnMenuItemsWithLabels("Simulate", "Simulation Mode");
        Assert.assertEquals(Boolean.TRUE, topologyUiPage.isSimulationModeEnabled());

        // Apply default focus before continuing
        topologyUiPage.clearFocus();
        topologyUiPage.defaultFocus();
        topologyUiPage.setSzl(4);

        // Verify pre conditions
        Assert.assertTrue("We need at least 3 visible vertices", 3 <= topologyUiPage.getVisibleVertices().size());
        Assert.assertTrue("Only one vertex should be in focus", 1 == topologyUiPage.getFocusedVertices().size());
        Assert.assertTrue("At least one vertex must not be focused", topologyUiPage.getNotFocusedVertices().size() >= 1);

        // Add a vertex to focus, which is not yet in focus
        topologyUiPage.getNotFocusedVertices().get(0).contextMenu().addToFocus();

        // Simulation mode should still be enabled
        Assert.assertEquals(Boolean.TRUE, topologyUiPage.isSimulationModeEnabled());

        // Set some vertex as focus
        Assert.assertTrue("At least one vertex must not be focused", topologyUiPage.getNotFocusedVertices().size() >= 1);
        topologyUiPage.getNotFocusedVertices().get(0).contextMenu().setAsFocus();

        // Simulation mode should still be enabled
        Assert.assertEquals(Boolean.TRUE, topologyUiPage.isSimulationModeEnabled());
    }

    private List<String> createChainOfBusinessServices(int length) throws InterruptedException {
        // Create a series of business services
        bsmAdminPage.open();
        final List<String> businessServiceNames = Lists.newArrayList();
        for (int i = 1; i <= length; i++) {
            businessServiceNames.add(String.format("topo_%s_%d", BSMAdminIT.createUniqueBusinessServiceName(), i));
        }
        for (String eachServiceName : businessServiceNames) {
            bsmAdminPage.openNewDialog(eachServiceName).save();
        }

        // Chain the business services, linking the 1st the 2nd, the 2nd to 3rd and so on...
        for (int i = 0; i < businessServiceNames.size() - 1; i++) {
            bsmAdminPage.expandAll();
            final String linkFrom = businessServiceNames.get(i);
            final String linkTo = businessServiceNames.get(i+1);
            final BSMAdminIT.BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openEditDialog(linkFrom);
            bsmAdminPageEditWindow.addChildEdge(linkTo, "Identity", 1);
            bsmAdminPageEditWindow.save();
        }

        // Reload the daemon to load the hierarchy into the state machine
        bsmAdminPage.reloadDaemon();
        return businessServiceNames;
    }

    private void deleteBusinessServices(List<String> businessServiceNames) {
        // Delete the business services we created
        bsmAdminPage.open();
        for (int i = 0; i < businessServiceNames.size(); i++) {
            final boolean isLast = i == businessServiceNames.size() - 1;
            final String eachServiceName = businessServiceNames.get(i);
            bsmAdminPage.delete(eachServiceName, !isLast);
        }
        // Reload the daemon to remove the services from the state machine
        bsmAdminPage.reloadDaemon();
    }
}
