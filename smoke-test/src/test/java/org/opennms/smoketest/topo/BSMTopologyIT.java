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

package org.opennms.smoketest.topo;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.smoketest.BSMAdminIT;
import org.opennms.smoketest.BSMAdminIT.BsmAdminPage;
import org.opennms.smoketest.BSMAdminIT.BsmAdminPageEditWindow;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.TopologyIT.FocusedVertex;
import org.opennms.smoketest.TopologyIT.TopologyProvider;
import org.opennms.smoketest.TopologyIT.TopologyUiPage;

import com.google.common.collect.Lists;

/**
 * Tests the 'Business Services' topology provider.
 *
 * @author jwhite
 */
public class BSMTopologyIT extends OpenNMSSeleniumTestCase {

    private BsmAdminPage bsmAdminPage;
    private TopologyUiPage topologyUiPage;

    @Before
    public void setUp() {
        bsmAdminPage = new BsmAdminPage(this);
        topologyUiPage = new TopologyUiPage(this);
    }

    @Test
    public void canViewVertices() throws InterruptedException {
        final List<String> businessServiceNames = createChainOfBusinessServices(5);
        try {
            // Open up the Topology UI and select the Business Services topology provider
            topologyUiPage.open();
            topologyUiPage.selectTopologyProvider(TopologyProvider.BUSINESSSERVICE);

            // Remove any existing vertices from focus
            for (FocusedVertex focusedVerted : topologyUiPage.getFocusedVertices()) {
                focusedVerted.removeFromFocus();
                Thread.sleep(500); // TODO: Remove this explicit sleep
            }
            assertEquals(0, topologyUiPage.getFocusedVertices().size());

            // Search for and select the first business service in our list
            final String businessServiceName = businessServiceNames.get(0);
            topologyUiPage.search(businessServiceName.substring(0,  12)).selectItemThatContains(businessServiceName);

            // We should have a single vertex in focus
            assertEquals(1, topologyUiPage.getFocusedVertices().size());

            // TODO: Verify that the vertices appear on the layout and adjust the SZL
        } finally {
            deleteBusinessServices(businessServiceNames);
        }
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
            final BsmAdminPageEditWindow bsmAdminPageEditWindow = bsmAdminPage.openEditDialog(linkFrom);
            bsmAdminPageEditWindow.addChildEdge(linkTo, "Identity", 1);
            bsmAdminPageEditWindow.save();
        }

        // Reload the daemon to load the hierarchy into the state machine
        bsmAdminPage.reloadDaemon();
        return businessServiceNames;
    }

    /**
     * TODO: Make this a utility method that just deletes everything
     * without requiring an explicit list.
     */
    private void deleteBusinessServices(List<String> businessServiceNames) {
        // Delete the business services we created
        bsmAdminPage.open();
        for (int i = 0; i < businessServiceNames.size(); i++) {
            final boolean isLast = i == businessServiceNames.size() - 1;
            final String eachServiceName = businessServiceNames.get(i);
            bsmAdminPage.delete(eachServiceName, !isLast);
        }
    }
}
