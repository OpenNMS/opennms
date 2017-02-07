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

package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;

/**
 * Helper class to map Vaadin {@link MenuBar.Command} to OpenNMS {@link Operation}s.
 * Also contains the current visible {@link MenuBar} of the {@link TopologyUI}.
 *
 * @author mvrueden
 */
public class TopologyMenuBar extends MenuBar {

    private final Map<MenuBar.Command, Operation> commandToOperationMap = new HashMap<>();
    private final List<MenuItemUpdateListener> menuItemUpdateListeners = new ArrayList<>();

    public TopologyMenuBar() {
        setWidth(100, Unit.PERCENTAGE);
    }

    // Builds the menu
    public void buildMenu(GraphContainer graphContainer, UI mainWindow, CommandManager commandManager) {
        removeItems();

        final DefaultOperationContext operationContext = new DefaultOperationContext(mainWindow, graphContainer, OperationContext.DisplayLocation.MENUBAR);
        final MenuBarBuilder menuBarBuilder = new MenuBarBuilder();
        menuBarBuilder.setTopLevelMenuOrder(commandManager.getTopLevelMenuOrder());
        menuBarBuilder.setSubMenuGroupOrder(commandManager.getSubMenuGroupOrder());

        for (org.opennms.features.topology.app.internal.Command command : commandManager.getCommandList()) {
            String menuPosition = command.getMenuPosition();
            MenuBar.Command menuCommand = (Command) selectedItem -> {
                List<VertexRef> targets = new ArrayList<>(graphContainer.getSelectionManager().getSelectedVertexRefs());
                operationContext.setChecked(selectedItem.isChecked());
                command.doCommand(targets, operationContext);
                notifyMenuUpdateListener();
            };
            menuBarBuilder.addMenuCommand(menuCommand, menuPosition);
            commandToOperationMap.put(menuCommand, command.getOperation());
        }
        menuBarBuilder.apply(this);
    }

    // Renders the menu items (checked, not checked, etc.) recursively
    public void updateMenuItems(GraphContainer graphContainer, UI mainWindow) {
        updateMenuItems(getItems(), graphContainer, mainWindow);
    }

    private void updateMenuItems(List<MenuBar.MenuItem> menuItems, GraphContainer graphContainer, UI mainWindow) {
        for(MenuBar.MenuItem menuItem : menuItems) {
            if(menuItem.hasChildren()) {
                updateMenuItems(menuItem.getChildren(), graphContainer, mainWindow);
            }else {
                updateMenuItem(menuItem, graphContainer, mainWindow);
            }
        }
    }

    private void updateMenuItem(MenuBar.MenuItem menuItem, GraphContainer graphContainer, UI mainWindow) {
        try {
            final Operation operation = commandToOperationMap.get(menuItem.getCommand());
            // Check for null because separators have no Operation
            if(operation != null) {
                final DefaultOperationContext operationContext = new DefaultOperationContext(mainWindow, graphContainer, OperationContext.DisplayLocation.MENUBAR);
                final List<VertexRef> selectedVertices = new ArrayList<VertexRef>(graphContainer.getSelectionManager().getSelectedVertexRefs());
                final boolean visibility = operation.display(selectedVertices, operationContext);
                final boolean enabled = operation.enabled(selectedVertices, operationContext);

                menuItem.setVisible(visibility);
                menuItem.setEnabled(enabled);

                if (operation instanceof CheckedOperation) {
                    if (!menuItem.isCheckable()) {
                        menuItem.setCheckable(true);
                    }
                    menuItem.setChecked(((CheckedOperation) operation).isChecked(selectedVertices, operationContext));
                }
            }
        } catch (final RuntimeException e) {
            LoggerFactory.getLogger(this.getClass()).warn("updateMenuItem: operation failed", e);
        }
    }

    protected void notifyMenuUpdateListener() {
        for(MenuItemUpdateListener listener : menuItemUpdateListeners) {
            listener.updateMenuItems();
        }
    }

    public void addMenuItemUpdateListener(MenuItemUpdateListener newListener) {
        menuItemUpdateListeners.add(newListener);
    }
}
