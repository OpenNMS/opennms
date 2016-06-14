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
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.MenuBar;

/**
 * Helper class to manage creation of the {@link MenuBar} and the {@link org.vaadin.peter.contextmenu.ContextMenu}
 * of the Topology UI.
 */
public class MenuManager {

	private static final Logger LOG = LoggerFactory.getLogger(MenuManager.class);

	/**
	 * {@ink Operation} registered to the OSGI service registry (including the service properties).
	 */
	private final List<OperationServiceWrapper> m_operations = new CopyOnWriteArrayList<>();

	/**
	 * Listeners listening to menu updates.
	 */
    private final List<MenuUpdateListener> m_menuUpdateListeners = new ArrayList<>();
    private final List<String> m_topLevelMenuOrder = new ArrayList<>();
    private final Map<String, List<String>> m_subMenuGroupOrder = new HashMap<>();

	protected void updateCommandListeners() {
		for (MenuUpdateListener listener : m_menuUpdateListeners) {
			listener.updateMenu();
		}
	}

	public void addMenuItemUpdateListener(MenuUpdateListener listener) {
		m_menuUpdateListeners.add(listener);
	}

	public void removeMenuItemUpdateListener(MenuUpdateListener listener) {
		m_menuUpdateListeners.remove(listener);
	}

	public MenuBar getMenuBar(List<VertexRef> targets, OperationContext operationContext) {
		MenuBuilder menuBuilder = new MenuBuilder();
		menuBuilder.setTopLevelMenuOrder(m_topLevelMenuOrder);
		menuBuilder.setSubMenuGroupOrder(m_subMenuGroupOrder);
		for (OperationServiceWrapper operationServiceWrapper : m_operations) {
			if (operationServiceWrapper.getMenuPosition() != null) { // if menu position is null, there is no place to put it
				MenuItem item = new OperationMenuItem(operationServiceWrapper);
				menuBuilder.addMenuItem(item, operationServiceWrapper.getMenuPosition().split("\\|"));
			}

		}
		MenuBar menu = menuBuilder.build(targets, operationContext, () -> updateCommandListeners());
		return menu;
	}

	/**
	 * Gets the ContextMenu add-on for the app based on OSGi Operations
	 * @param operationContext
	 * @return
	 */
	public TopologyContextMenu getContextMenu(List<VertexRef> targets, OperationContext operationContext) {
		MenuBuilder menuBuilder = new MenuBuilder();
		for (OperationServiceWrapper operationServiceWrapper : m_operations) {
			if (operationServiceWrapper.getContextMenuPosition() != null) {
				MenuItem item = new OperationMenuItem(operationServiceWrapper);
				menuBuilder.addMenuItem(item, operationServiceWrapper.getContextMenuPosition().isEmpty() ? null: operationServiceWrapper.getContextMenuPosition().split("\\|"));
			}
		}

		addNavigateToItems(menuBuilder, targets, operationContext);

		MenuBar menu = menuBuilder.build(targets, operationContext, () -> updateCommandListeners());
		return new TopologyContextMenu(menu);
	}

	public void onBind(Operation operation, Map<String, String> props) {
		OperationServiceWrapper operCommand = new OperationServiceWrapper(operation, props);
		m_operations.add(operCommand);
		updateCommandListeners();
	}

	public void onUnbind(Operation operation, Map<String, String> props) {
		for (OperationServiceWrapper command : m_operations) {
			if (command.getOperation() == operation) {
				m_operations.remove(command);
			}
		}
		updateCommandListeners();
	}

	public void setTopLevelMenuOrder(List<String> menuOrderList) {
		if (m_topLevelMenuOrder == menuOrderList) return;
		m_topLevelMenuOrder.clear();
		m_topLevelMenuOrder.addAll(menuOrderList);

	}

    public void updateMenuConfig(Dictionary<String,?> props) {
        List<String> topLevelOrder = Arrays.asList(props.get("toplevelMenuOrder").toString().split(","));
        setTopLevelMenuOrder(topLevelOrder);

		for (String topLevelItem : topLevelOrder) {
			if (!topLevelItem.equals("Additions")) {
				String key = "submenu." + topLevelItem + ".groups";
				Object value = props.get(key);
				if (value != null) {
					addOrUpdateGroupOrder(topLevelItem, Arrays.asList(value.toString().split(",")));
				}
			}
		}
		addOrUpdateGroupOrder(
				"Default",
				Arrays.asList(props.get("submenu.Default.groups").toString().split(",")));

		updateCommandListeners();

	}

	void addOrUpdateGroupOrder(String key, List<String> orderSet) {
		if (!m_subMenuGroupOrder.containsKey(key)) {
			m_subMenuGroupOrder.put(key, orderSet);
		} else {
			m_subMenuGroupOrder.remove(key);
			m_subMenuGroupOrder.put(key, orderSet);
		}
	}

	Map<String, List<String>> getMenuOrderConfig() {
		return m_subMenuGroupOrder;
	}

	public <T extends CheckedOperation> T findOperationByLabel(Class<T> operationClass, String label) {
		if (label == null) {
			return null; // nothing to do
		}
		for (OperationServiceWrapper eachCommand : m_operations) {
			try {
				String opLabel = MenuBuilder.removeLabelProperties(eachCommand.getCaption());
				if (label.equals(opLabel)) {
					T operation = (T) eachCommand.getOperation();
					return operation;
				}
			} catch (ClassCastException e) {}
		}
		return null;
	}

	// adds menu items for the "navigate to" operation
	private void addNavigateToItems(MenuBuilder menuBuilder, List<VertexRef> targets, OperationContext operationContext) {
		final GraphContainer graphContainer = operationContext.getGraphContainer();
		// Find the vertices in other graphs that this vertex links to
		final Collection<VertexRef> oppositeVertices = graphContainer.getMetaTopologyProvider().getOppositeVertices(targets.get(0));

		// Find all namespaces
		final Set<String> targetNamespaces = oppositeVertices.stream().map(v -> v.getNamespace()).collect(Collectors.toSet());

		// Find provider for namespaces and add menu entry
		for (String eachTargetNamespace : targetNamespaces) {
			// Find the graph provider for the target namespace
			final GraphProvider targetGraphProvider = graphContainer.getMetaTopologyProvider().getGraphProviders().stream()
					.filter(g -> g.getVertexNamespace().equals(eachTargetNamespace))
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
