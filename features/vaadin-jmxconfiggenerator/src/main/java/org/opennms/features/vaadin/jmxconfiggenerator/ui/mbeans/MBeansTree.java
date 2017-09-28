/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans;

import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tree;

/**
 *
 * @author Markus von Rüden
 */
class MBeansTree extends Tree implements Action.Handler {

	public interface MetaMBeansTreeItem {
		String TOOLTIP = "caption";
		String ICON = "icon";
		String CAPTION = "label";
		String SELECTED = "selected";
		String VALID = "valid";
	}

	private final MBeansController controller;
	private final MbeansHierarchicalContainer container;
	private final Action SELECT = new Action("select");
	private final Action DESELECT = new Action("deselect");
	private final Action[] ACTIONS = new Action[]{ SELECT, DESELECT };

	protected MBeansTree(final MBeansController controller) {
		this.container = controller.getMBeansHierarchicalContainer();
		this.controller = controller;
		setSizeFull();
		setContainerDataSource(container);
		setItemCaptionPropertyId(MetaMBeansTreeItem.CAPTION);
		setItemIconPropertyId(MetaMBeansTreeItem.ICON);
		setItemDescriptionGenerator(new ItemDescriptionGenerator() {
			@Override
			public String generateDescription(Component source, Object itemId, Object propertyId) {
				return getItem(itemId).getItemProperty(MetaMBeansTreeItem.TOOLTIP).getValue().toString();
			}
		});
		setItemStyleGenerator(new ItemStyleGenerator() {
			@Override
			public String getStyle(Tree source, Object itemId) {
				if ((Boolean) source.getItem(itemId).getItemProperty(MBeansTree.MetaMBeansTreeItem.VALID).getValue())
					return "";
				return "invalid";
			}
		});
		setSelectable(true);
		setMultiSelect(false);
		setNullSelectionAllowed(true);
		setMultiselectMode(MultiSelectMode.SIMPLE);
		addItemClickListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				final Object itemId = event.getItemId();
				if (itemId instanceof String) {
					controller.selectItemInTree((String) itemId);
				}
			}
		});
		setImmediate(true);
		addActionHandler(this);
		setValidationVisible(false);
	}

	/**
	 * Expands all items in the tree, so the complete tree is expanded per default.
	 * If there are any items the first itemId is returned.
	 * @return The first itemId in the container if there is any, otherwise false.
	 */
	public void expandAllItems() {
		for (Object itemId : rootItemIds()) {
			expandItemsRecursively(itemId);
		}
	}

	/**
	 * Expands all items in the tree upwards beginning at the startItemid. This is useful if you want to
	 * show a path to a node in the tree and ensure it is really visible (e.g. an element is not valid).
	 *
	 * @param startItemId The itemId to start expanding the tree from upwards to the root node.
	 */
	public void expandItemsUpToParent(String startItemId) {
		if (startItemId != null) {
			expandItem(startItemId);
			String parentItemId = (String) getParent(startItemId);
			expandItemsUpToParent(parentItemId);
		}
	}

	@Override
	public Action[] getActions(Object target, Object sender) {
		return ACTIONS;
	}
	
	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if (action == SELECT) {
			controller.handleSelectDeselect(container.getItem(target), target, true);
		}
		if (action == DESELECT) {
			controller.handleSelectDeselect(container.getItem(target), target, false);
		}
		fireValueChange(false);
	}
}
