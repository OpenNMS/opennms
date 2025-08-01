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

import com.vaadin.contextmenu.ContextMenu;
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
	public TopologyContextMenu(UI parentComponent, MenuBar menuBar) {
		super(parentComponent, true);
		addItems(menuBar);
	}

	public TopologyContextMenu(UI parentComponent) {
		super(parentComponent, true);
	}

	public void updateMenu(GraphContainer graphContainer, UI mainWindow, OperationManager operationManager, List<VertexRef> targets) {
		final OperationContext operationContext = new DefaultOperationContext(mainWindow, graphContainer, OperationContext.DisplayLocation.CONTEXTMENU);

		// Clear Menu
		removeItems();

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

		// Now convert the MenuBar to a context menu
		addItems(menu);
	}

	private void addItems(MenuBar menuBar) {
		for (MenuBar.MenuItem eachItem : menuBar.getItems()) {
			addItems(() ->
				this.addItem(
					eachItem.getText(),
					eachItem.getIcon(),
					(MenuBar.Command) selectedItem -> {
						if (eachItem.getCommand() != null) {
							eachItem.getCommand().menuSelected(eachItem);
						}
					}), eachItem);
		}
	}

	private void addItems(ItemAddBehaviour<MenuBar.MenuItem> behaviour, MenuBar.MenuItem child) {
		MenuBar.MenuItem contextMenuItem = behaviour.addItem();
		contextMenuItem.setEnabled(child.isEnabled());
		if (child.isSeparator()) {
			contextMenuItem.addSeparator();
		}
		if (child.getCommand() != null) {
			contextMenuItem.setCommand((MenuBar.Command) contextMenuItemClickEvent -> child.getCommand().menuSelected(child));
		}
		if (child.hasChildren()) {
			for (MenuBar.MenuItem eachChild : child.getChildren()) {
				addItems(() ->
						contextMenuItem.addItem(
								eachChild.getText(),
								eachChild.getIcon(),
								(MenuBar.Command) selectedItem -> {
									if (eachChild.getCommand() != null) {
										eachChild.getCommand().menuSelected(eachChild);
									}
								}), eachChild);
			}
		}
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
