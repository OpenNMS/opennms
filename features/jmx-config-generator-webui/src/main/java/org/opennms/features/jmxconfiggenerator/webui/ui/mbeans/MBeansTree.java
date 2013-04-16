/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.jmxconfiggenerator.webui.ui.mbeans;

import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Component;
import com.vaadin.ui.Tree;
import org.opennms.features.jmxconfiggenerator.webui.data.UiModel;
import org.opennms.features.jmxconfiggenerator.webui.data.MetaMBeanItem;
import org.opennms.features.jmxconfiggenerator.webui.data.ModelChangeListener;

/**
 *
 * @author Markus von Rüden
 */
class MBeansTree extends Tree implements ModelChangeListener<UiModel>, ViewStateChangedListener, Action.Handler {

	private final MBeansController controller;
	private final MbeansHierarchicalContainer container;
	private final Action SELECT = new Action("select");
	private final Action DESELECT = new Action("deselect");
	private final Action[] ACTIONS = new Action[]{SELECT, DESELECT};

	protected MBeansTree(final MBeansController controller) {
		this.container = controller.getMBeansHierarchicalContainer();
		this.controller = controller;
		setSizeFull();
		setCaption("MBeans");
		setContainerDataSource(container);
		setItemCaptionPropertyId(MetaMBeanItem.CAPTION);
		setItemIconPropertyId(MetaMBeanItem.ICON);
		setItemDescriptionGenerator(new ItemDescriptionGenerator() {
			@Override
			public String generateDescription(Component source, Object itemId, Object propertyId) {
				return getItem(itemId).getItemProperty(MetaMBeanItem.TOOLTIP).getValue().toString();
			}
		});
		setSelectable(true);
		setMultiSelect(false);
		setNullSelectionAllowed(true);
		setMultiselectMode(AbstractSelect.MultiSelectMode.SIMPLE);
		addListener(new ItemClickEvent.ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				controller.updateView(event);
			}
		});
		setImmediate(true);
		addActionHandler(this);
	}

	/**
	 * Expands all items in the tree, so the complete tree is expanded per default.
	 * If there are any items the first itemId is returned.
	 * @return The first itemId in the container if there is any, otherwise false.
	 */
	private Object expandTree() {
		Object firstItemId = null;
		for (Object itemId : getItemIds()) {
			if (firstItemId == null) firstItemId = itemId;
			expandItem(itemId);
		}
		return firstItemId;
	}

	@Override
	public void modelChanged(UiModel internalModel) {
		container.updateDataSource(internalModel);
		Object selectItemId = expandTree();
		
		// select anything in the tree
		if (selectItemId != null) {
			select(selectItemId); // first item
		} else {
			select(getNullSelectionItemId()); // no selection at all (there are no elements in the tree)
		}
	}

	@Override
	public void viewStateChanged(ViewStateChangedEvent event) {
		switch (event.getNewState()) {
			case Edit:
				setEnabled(false);
				break;
			default:
				setEnabled(true);
				break;
		}
	}

	@Override
	public Action[] getActions(Object target, Object sender) {
		return ACTIONS;
	}
	
	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if (action == SELECT) controller.handleSelect(container, target);
		if (action == DESELECT) controller.handleDeselect(container, target);
		fireValueChange(false);
	}
}
