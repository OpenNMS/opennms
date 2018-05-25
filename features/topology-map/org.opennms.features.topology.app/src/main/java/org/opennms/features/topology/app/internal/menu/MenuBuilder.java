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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.vaadin.ui.MenuBar;


/**
 * Builder to create a Menu.
 */
public class MenuBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(MenuBuilder.class);

	private static final String TOP_LEVEL_ADDITIONS = "Additions";

	protected List<MenuItem> m_menuBar = Lists.newArrayList();
	private List<String> m_menuOrder = new ArrayList<>();
	private Map<String, List<String>> m_submenuOrderMap = new HashMap<>();

	/**
	 * Creates the provided menu path (e.g. File, Save As)
	 *
	 * @param path The path to create
	 */
	public void createPath(String... path) {
		if (path == null || path.length == 0) {
			return;
		}
		// find menu item if exists
		List<String> pathList = Arrays.asList(path);
		MenuItem parent = findMenuItem(null, pathList);
		if (parent == null) {
			// there is no parent for the current path, we create the path
			// find deepest parent for the path
			List<String> pathToCreate = new ArrayList<>(pathList);
			int index = 0;
			while (parent == null && pathList.size() > 1) {
				pathList = pathList.subList(0, pathList.size() - 1);
				parent = findMenuItem(null, pathList);
				index++;
			}
			// If we found a parent, we have to cut the path to create
			if (parent != null) {
				pathToCreate = pathToCreate.subList(index, pathToCreate.size());
			}
			createPath(parent, pathToCreate);
		}
	}

	/**
	 * Adds a menu item for path. If path is empty, it is added as a root entry.
	 *
	 * @param menuItem The item to add.
	 * @param parentPath The item to add to
     */
	public void addMenuItem(MenuItem menuItem, String... parentPath) {
		Objects.requireNonNull(menuItem);
		if (parentPath == null || parentPath.length == 0) {
			// root element
			m_menuBar.add(menuItem);
		} else {
			createPath(parentPath); // create path
			MenuItem parent = findMenuItem(null, Arrays.asList(parentPath));
			if (parent == null) { // it should have been created
				throw new IllegalStateException("No parent element was found, but should have been created");
			}
			parent.addChildren(menuItem);
		}
	}

	private MenuItem createPath(MenuItem currentParent, List<String> parentPath) {
		if (currentParent == null) {
			SimpleMenuItem newParent = new SimpleMenuItem(parentPath.get(0));
			m_menuBar.add(newParent);
			currentParent = newParent;
			parentPath = parentPath.subList(1, parentPath.size());
		}
		if (!parentPath.isEmpty()) {
			for (String eachPath : parentPath) {
				SimpleMenuItem childMenuItem = new SimpleMenuItem(eachPath);
				currentParent.addChildren(childMenuItem);
				currentParent = childMenuItem;
			}
		}
		return currentParent;
	}

	private MenuItem findMenuItem(MenuItem parent, List<String> parentPath) {
		if (parentPath.isEmpty()) {
			return parent;
		}
		List<MenuItem> items = getItemsToCheck(parent);
		if (items.isEmpty()) {
			return parent;
		}
		for (MenuItem eachItem : items) {
			if (eachItem.getLabel().equals(parentPath.get(0))) {
				return findMenuItem(eachItem, parentPath.subList(1, parentPath.size()));
			}
		}
		return null;
	}

	private List<MenuItem> getItemsToCheck(MenuItem parent) {
		if (parent == null) {
			return new ArrayList<>(m_menuBar);
		}
		return parent.getChildren();
	}

	public void setTopLevelMenuOrder(List<String> menuOrder) {
	    m_menuOrder = menuOrder;
	}

	public void setSubMenuGroupOrder(Map<String, List<String>> submenOrderMap) {
	    m_submenuOrderMap = submenOrderMap;
	}

	protected void determineAndApplyOrder() {
		for (MenuItem eachMenuItem : m_menuBar) {
			int order = determineOrderOfMenuEntry(eachMenuItem.getLabel(), m_menuOrder);
			eachMenuItem.setOrder(order);
			applyOrderToChildren(eachMenuItem);
		}
	}

	protected void applyOrderToChildren(final MenuItem parent) {
		final List<String> submenuOrder = getSubmenuOrderList(parent);
		for (MenuItem eachChild : parent.getChildren()) {
			String groupLabel = getGroupForLabel(eachChild.getLabel(), submenuOrder);
			int order = determineOrderOfMenuEntry(groupLabel, submenuOrder);
			eachChild.setOrder(order);
		}
	}

	private List<String> getSubmenuOrderList(MenuItem menuItem) {
		final String labelWithoutProperties = removeLabelProperties(menuItem.getLabel());
		return m_submenuOrderMap.get(labelWithoutProperties) != null ? m_submenuOrderMap.get(labelWithoutProperties) :  m_submenuOrderMap.containsKey("default") ? m_submenuOrderMap.get("default") : new ArrayList<>();
	}

	private int determineOrderOfMenuEntry(String label, List<String> menus) {
		if (menus.contains(label)) {
			return menus.indexOf(label);
		} else {
			if (menus.contains(TOP_LEVEL_ADDITIONS)) {
				return menus.indexOf(TOP_LEVEL_ADDITIONS);
			} else if (menus.contains(TOP_LEVEL_ADDITIONS.toLowerCase())) {
				return menus.indexOf(TOP_LEVEL_ADDITIONS.toLowerCase());
			} else {
				return menus.size();
			}
		}
	}

	/**
	 * Converts the current menu configuration to Vaadin's {@link MenuBar} representation.
	 *
	 * @param targets The current targets (e.g. the selection)
	 * @param operationContext The current {@link OperationContext}.
	 * @param hooks Optional hooks to be executed after a menu item's command has been executed.
	 * @return The converted {@link MenuBar}
	 */
	public MenuBar build(List<VertexRef> targets, OperationContext operationContext, Runnable... hooks) {
		MenuBar menuBar = new MenuBar();
		apply(menuBar, targets, operationContext, hooks);
		return menuBar;
	}

	public void apply(MenuBar rootMenu, List<VertexRef> targets, OperationContext operationContext, Runnable... hooks) {
		final List<Runnable> hookList = hooks == null ? Collections.emptyList() : Arrays.asList(hooks);

		// Determine the order of the items in the menu
		determineAndApplyOrder();

		// Start building menubar
		List<MenuItem> rootItems = new ArrayList<>(m_menuBar);
		Collections.sort(rootItems);
		for (MenuItem eachRootElement : rootItems) {
			MenuBar.MenuItem menuItem = addItem(() -> rootMenu.addItem(removeLabelProperties(eachRootElement.getLabel()), null), eachRootElement, targets, operationContext, hookList);

			// Add children
			addItems(menuItem, eachRootElement, targets, operationContext, hookList);
		}
	}

	private void addItems(MenuBar.MenuItem currentMenuItem, MenuItem currentParent, List<VertexRef> targets, OperationContext operationContext, List<Runnable> hooks) {
		if (currentMenuItem != null) {
			// Now add children
			List<MenuItem> childItems = new ArrayList<>(currentParent.getChildren());
			Collections.sort(childItems);
			String prevGroup = null;
			MenuBar.MenuItem prevMenuItem = null;
			for (MenuItem eachChild : childItems) {
				// add Separators between groups if the previous group changed and we added an element
				// (otherwise we may end up having multiple separators)
				String currentGroup = getGroupForLabel(eachChild.getLabel(), getSubmenuOrderList(currentParent));
				if (prevGroup != null && prevMenuItem != null && !prevGroup.equals(currentGroup)) {
					currentMenuItem.addSeparator();
				}
				prevGroup = currentGroup;
				prevMenuItem = addItem(() -> currentMenuItem.addItem(removeLabelProperties(eachChild.getLabel()), null), eachChild, targets, operationContext, hooks);

				// add children
				addItems(prevMenuItem, eachChild, targets, operationContext, hooks);
			}
		}
	}

	protected static String getGroupForLabel(String label, List<String> submenuOrder) {
		String group;
		String[] groupParams = label.split("\\?");

		for(String param : groupParams) {
			if(param.contains("group")) {
				String[] keyValue = param.split("=");
				group = keyValue[1];
				if (submenuOrder.contains(group)) {
					return group;
				}
				if (submenuOrder.contains(group.toLowerCase())) {
					return group.toLowerCase();
				}
			}
		}
		return null;
	}

	protected static String removeLabelProperties(String commandKey) {
		if(commandKey.contains("?")) {
			return commandKey.substring(0, commandKey.indexOf('?'));
		} else {
			return commandKey;
		}
	}

	private static MenuBar.MenuItem addItem(ItemAddBehaviour<MenuBar.MenuItem> behaviour, MenuItem eachChildElement, List<VertexRef> targets, OperationContext operationContext, List<Runnable> hooks) {
		boolean visibility = eachChildElement.isVisible(targets, operationContext);
		if (visibility) { // only add item if it is actually visible
			final MenuBar.MenuItem childMenuItem = behaviour.addItem();
			final boolean enabled = eachChildElement.isEnabled(targets, operationContext);
			final boolean checkable = eachChildElement.isCheckable();
			childMenuItem.setEnabled(enabled);
			childMenuItem.setCheckable(checkable);
			if (checkable) {
				boolean checked = eachChildElement.isChecked(targets, operationContext);
				childMenuItem.setChecked(checked);
			}

			// Add click behaviour if leaf element
			if (!eachChildElement.getChildren().isEmpty() && eachChildElement.getCommand() != null) {
				LOG.warn("The MenuItem {} is not a leaf but defines a command. The command is ignored.", removeLabelProperties(eachChildElement.getLabel()));
			} else {
				if (eachChildElement.getCommand() != null) {
					childMenuItem.setCommand((MenuBar.Command) selectedItem -> {
						eachChildElement.getCommand().execute(targets, operationContext);
						hooks.forEach(Runnable::run);
					});
				}
			}
			return childMenuItem;
		}
		return null;
	}
}
