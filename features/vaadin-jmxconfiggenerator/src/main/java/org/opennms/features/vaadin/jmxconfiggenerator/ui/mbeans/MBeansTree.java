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
package org.opennms.features.vaadin.jmxconfiggenerator.ui.mbeans;

import com.vaadin.event.Action;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.v7.event.ItemClickEvent.ItemClickListener;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.Tree;

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
