/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.DefaultOperationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.client.ContextMenuState;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;

/**
 * Context-Menu implementation for the TopologyUI.
 */
public class TopologyContextMenu extends ContextMenu {

	private static final Logger LOG = LoggerFactory.getLogger(TopologyContextMenu.class);

	private final List<MenuUpdateListener> menuItemUpdateListeners = new ArrayList<>();

	/**
	 * This constructor is used to convert the {@link MenuBar} to a {@link ContextMenu}
	 *
	 * @param menuBar the {@link MenuBar} to convert
	 */
	public TopologyContextMenu(MenuBar menuBar) {
		addItems(menuBar);
	}

	public TopologyContextMenu() {

	}

	public void updateMenu(GraphContainer graphContainer, UI mainWindow, OperationManager operationManager, List<VertexRef> targets) {
		final OperationContext operationContext = new DefaultOperationContext(mainWindow, graphContainer, OperationContext.DisplayLocation.CONTEXTMENU);

		// Clear Menu
		removeAllItems();

		// Rebuild menu
		MenuBuilder menuBuilder = new MenuBuilder();
		for (OperationServiceWrapper operationServiceWrapper : operationManager.getOperationWrappers()) {
			if (operationServiceWrapper.getContextMenuPosition() != null) {
				MenuItem item = new OperationMenuItem(operationServiceWrapper);
				menuBuilder.addMenuItem(item, operationServiceWrapper.getContextMenuPosition().isEmpty() ? null: operationServiceWrapper.getContextMenuPosition().split("\\|"));
			}
		}
		addNavigateToItems(menuBuilder, targets, operationContext);

		MenuBar menu = menuBuilder.build(targets, operationContext, this::notifyMenuUpdateListener);
		addItems(menu);
	}

	private void addItems(MenuBar menuBar) {
		for (MenuBar.MenuItem eachItem : menuBar.getItems()) {
			addItems(() -> this.addItem(eachItem.getText(), eachItem.getIcon()), eachItem);
		}
	}

	private void addItems(ItemAddBehaviour<ContextMenuItem> behaviour, MenuBar.MenuItem child) {
		ContextMenuItem contextMenuItem = behaviour.addItem();
		contextMenuItem.setEnabled(child.isEnabled());
		contextMenuItem.setSeparatorVisible(child.isSeparator());
		if (child.getCommand() != null) {
			contextMenuItem.addItemClickListener(contextMenuItemClickEvent -> child.getCommand().menuSelected(child));
		}
		if (child.hasChildren()) {
			for (MenuBar.MenuItem eachChild : child.getChildren()) {
				addItems(() -> contextMenuItem.addItem(eachChild.getText(), eachChild.getIcon()), eachChild);
			}
		}
	}

	public List<ContextMenuState.ContextMenuItemState> getItems() {
		return getState().getRootItems();
	}

	public static List<VertexRef> asVertexList(Object target) {
		if (target != null && target instanceof Collection) {
			return new ArrayList<>((Collection<VertexRef>) target);
		} else if (target != null && target instanceof VertexRef) {
			return Collections.singletonList((VertexRef)target);
		} else {
			return Collections.emptyList();
		}
	}

	protected void notifyMenuUpdateListener() {
		for(MenuUpdateListener listener : menuItemUpdateListeners) {
			listener.updateMenu();
		}
	}

	public void addMenuItemUpdateListener(MenuUpdateListener newListener) {
		menuItemUpdateListeners.add(newListener);
	}

	// adds menu items for the "navigate to" operation
	private static void addNavigateToItems(MenuBuilder menuBuilder, List<VertexRef> targets, OperationContext operationContext) {
		if (!targets.isEmpty()) {
			menuBuilder.createPath("Navigate To");

			final GraphContainer graphContainer = operationContext.getGraphContainer();
			// Find the vertices in other graphs that this vertex links to
			final Collection<VertexRef> oppositeVertices = graphContainer.getTopologyServiceClient().getOppositeVertices(targets.get(0));

			// Find all namespaces
			final Set<String> targetNamespaces = oppositeVertices.stream().map(VertexRef::getNamespace).collect(Collectors.toSet());

			// Find provider for namespaces and add menu entry
			for (String eachTargetNamespace : targetNamespaces) {
				// Find the graph provider for the target namespace
				final GraphProvider targetGraphProvider = graphContainer.getTopologyServiceClient().getGraphProviders().stream()
						.filter(g -> g.getNamespace().equals(eachTargetNamespace))
						.findFirst().orElse(null);
				if (targetGraphProvider == null) {
					LOG.warn("No graph provider found for namespace '{}'.", eachTargetNamespace);
					continue;
				}
				NavigationMenuItem item = new NavigationMenuItem(targetGraphProvider, targets.get(0));
				menuBuilder.addMenuItem(item, "Navigate To");
			}

		}
	}
}
