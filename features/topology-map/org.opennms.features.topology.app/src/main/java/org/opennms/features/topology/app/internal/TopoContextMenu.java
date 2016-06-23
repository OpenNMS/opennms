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

package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;
import org.vaadin.peter.contextmenu.ContextMenu;

import com.google.common.collect.Lists;

public class TopoContextMenu extends ContextMenu {

	/**
	 * This listener will fire the operation that we associate with the context menu item.
	 */
	public static class ContextMenuListener implements ContextMenu.ContextMenuItemClickListener {

		private final OperationContext m_opContext;
		private final TopoContextMenu m_topoContextMenu;
		private final ContextMenuItem m_item;
		private final Operation m_operation;

		public ContextMenuListener(OperationContext opContext, TopoContextMenu topoContextMenu, ContextMenuItem item, Operation operation) {
			m_opContext = opContext;
			m_topoContextMenu = topoContextMenu;
			m_item = item;
			m_operation = operation;
		}

		@Override
		public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
			if (event.getSource() == m_item) {
				try {
					Collection<VertexRef> selectedVertexRefs = m_opContext.getGraphContainer().getSelectionManager().getSelectedVertexRefs();

					List<VertexRef> targets;
					// If the user right-clicks on a vertex that is already selected...
					if(selectedVertexRefs.contains(m_topoContextMenu.getTarget())) {
						// ... then use the entire selection as the target of the operation
						targets = Lists.newArrayList(selectedVertexRefs);
					} else{
						// Otherwise, just use the single vertex that was right-clicked on as the target
						targets = asVertexList(m_topoContextMenu.getTarget());
					}

					m_operation.execute(targets, m_opContext);
				} catch (final Throwable e) {
					LoggerFactory.getLogger(this.getClass()).warn("contextMenuItemClicked: operation failed", e);
				}
			}
		}
	}

	private static final long serialVersionUID = -4506151279227147283L;

	public static class TopoContextMenuItem {

		private final String m_name;
		private final Operation m_operation;
		//private boolean m_visible = true;
		//private boolean m_enabled = true;
		private boolean m_separatorVisible = false;
		private final List<TopoContextMenuItem> m_children = new ArrayList<TopoContextMenuItem>();

		public TopoContextMenuItem(String name, Operation operation) {
			m_name = name;
			m_operation = operation;
		}

		/**
		 * This function is used to convert the {@link TopoContextMenuItem} into a {@link ContextMenuItem}
		 * and add it to the {@link TopoContextMenu}.
		 * 
		 * @param menu
		 * @param operationContext
		 */
		public void addItemToMenu(TopoContextMenu menu, OperationContext operationContext) {
			// Construct a new ContextMenuItem in the surrounding ContextMenu
            List<VertexRef> targets = asVertexList(menu.getTarget());
            if (m_operation == null || m_operation.display(targets, operationContext)) {
				ContextMenuItem item = menu.addItem(m_name);
				item.setEnabled(m_operation == null || m_operation.enabled(targets, operationContext));
				item.setSeparatorVisible(m_separatorVisible);
				if (m_operation != null) {
					item.addItemClickListener(new ContextMenuListener(operationContext, menu, item, m_operation));
				}

				// Add all children to the menu as well
				for (TopoContextMenuItem child : getChildren()) {
                    child.addSubItemToMenu(menu, item, operationContext);
                    //child.addItemToMenu(menu, operationContext);
                }
            }
		}

        private void addSubItemToMenu(TopoContextMenu menu, ContextMenuItem parentMenuItem, OperationContext operationContext) {
            List<VertexRef> targets = asVertexList(menu.getTarget());
            if (m_operation == null || m_operation.display(targets, operationContext)) {
                ContextMenuItem item = parentMenuItem.addItem(getName());
                item.setEnabled(m_operation == null || m_operation.enabled(targets, operationContext));
                item.setSeparatorVisible(m_separatorVisible);

                if (m_operation != null) {
                    item.addItemClickListener(new ContextMenuListener(operationContext, menu, item, m_operation));
                }

                for(TopoContextMenuItem child : getChildren()){
                    child.addSubItemToMenu(menu, item, operationContext);
                }
            }
        }

		public boolean hasChildren() {
			return m_children == null || m_children.size() == 0 ? false : true;
		}

		public boolean hasOperation() {
			return m_operation == null ? false : true;
		}

		public Operation getOperation() {
			return m_operation;
		}

		public List<TopoContextMenuItem> getChildren() {
			return m_children;
		}

		public TopoContextMenuItem addChildMenuItem(String label, Operation operation) {
			TopoContextMenuItem topoContextMenuItem = new TopoContextMenuItem(label, operation);
			m_children.add(topoContextMenuItem);
			return topoContextMenuItem;
		}

		public String getName() {
			return m_name;
		}

		/*
		public void setEnabled(boolean enabled) {
			m_enabled = enabled;
		}
		*/

		public void setSeparatorVisible(boolean visible) {
			// TODO: Figure out how to support this with the new ContextMenu API
			//m_item.setSeparatorVisible(b);
			m_separatorVisible = visible;
		}

		/*
		public boolean isVisible() {
			return m_visible;
		}

		public void setVisible(boolean visible) {
			this.m_visible = visible;
		}
		*/
	}

	private List<TopoContextMenuItem> m_items = new ArrayList<TopoContextMenuItem>();

	private Object m_target = null;

	public Object getTarget() {
		return m_target;
	}

	public void setTarget(Object target) {
		this.m_target = target;
	}

	public TopoContextMenuItem addItem(String label, Operation operation) {
		TopoContextMenuItem item = new TopoContextMenuItem(label, operation);
		m_items.add(item);
		return item;
	}

	public List<TopoContextMenuItem> getItems() {
		return m_items;
	}

	private static List<VertexRef> asVertexList(Object target) {
		if (target != null && target instanceof Collection) {
			return new ArrayList<VertexRef>((Collection<VertexRef>)target);
		} else if (target != null && target instanceof VertexRef) {
			return Collections.singletonList((VertexRef)target);
		} else {
			return Collections.<VertexRef>emptyList();
		}
	}

	public void updateOperationContext(OperationContext operationContext) {
		removeAllItems();
		for (TopoContextMenuItem item : m_items) {
			item.addItemToMenu(this, operationContext);
		}
	}
}
