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
package org.opennms.features.topology.app.internal.menu;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.DefaultOperationContext;
import org.opennms.features.topology.app.internal.TopologyUI;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;

/**
 * Helper class to map Vaadin {@link MenuBar.Command} to OpenNMS {@link Operation}s.
 * Also contains the current visible {@link MenuBar} of the {@link TopologyUI}.
 *
 * @author mvrueden
 */
public class TopologyMenuBar extends MenuBar {

    private final List<MenuUpdateListener> menuItemUpdateListeners = new ArrayList<>();

    public TopologyMenuBar() {
        setWidth(100, Unit.PERCENTAGE);
    }

    // Builds the menu
    public void updateMenu(GraphContainer graphContainer, UI mainWindow, OperationManager operationManager) {
        final DefaultOperationContext operationContext = new DefaultOperationContext(mainWindow, graphContainer, OperationContext.DisplayLocation.MENUBAR);
        final ArrayList<VertexRef> targets = new ArrayList<>(graphContainer.getSelectionManager().getSelectedVertexRefs());

        // Clear menu
        removeItems();

        // Build new Menu
        MenuBuilder menuBuilder = new MenuBuilder();
        menuBuilder.setTopLevelMenuOrder(operationManager.getTopLevelMenuOrder());
        menuBuilder.setSubMenuGroupOrder(operationManager.getSubMenuGroupOrder());
        for (OperationServiceWrapper operationServiceWrapper : operationManager.getOperationWrappers()) {
            if (operationServiceWrapper.getMenuPosition() != null) { // if menu position is null, there is no place to put it
                org.opennms.features.topology.app.internal.menu.MenuItem item = new OperationMenuItem(operationServiceWrapper);
                menuBuilder.addMenuItem(item, operationServiceWrapper.getMenuPosition().split("\\|"));
            }

        }
        menuBuilder.apply(this, targets, operationContext, this::notifyMenuUpdateListener);
    }

    protected void notifyMenuUpdateListener() {
        for(MenuUpdateListener listener : menuItemUpdateListeners) {
            listener.updateMenu();
        }
    }

    public void addMenuItemUpdateListener(MenuUpdateListener newListener) {
        menuItemUpdateListeners.add(newListener);
    }
}
