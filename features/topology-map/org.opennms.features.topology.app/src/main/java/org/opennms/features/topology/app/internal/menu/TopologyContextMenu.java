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

import org.opennms.features.topology.api.topo.VertexRef;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.client.ContextMenuState;

import com.vaadin.ui.MenuBar;

/**
 * Context-Menu implementation for the TopologyUI.
 */
public class TopologyContextMenu extends ContextMenu {

	private static final long serialVersionUID = 4346027848176535291L;

	/**
	 * This constructor is used to convert the {@link MenuBar} to a {@link ContextMenu}
	 *
	 * @param menuBar the {@link MenuBar} to convert
	 */
	public TopologyContextMenu(MenuBar menuBar) {
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
}
