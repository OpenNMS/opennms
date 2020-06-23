/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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
