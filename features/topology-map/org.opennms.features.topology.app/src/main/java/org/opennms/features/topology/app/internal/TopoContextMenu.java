package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.vaadin.peter.contextmenu.ContextMenu;

public class TopoContextMenu extends ContextMenu {


	public class TopoContextMenuItem {
		
		ContextMenuItem m_item = null;
		Operation m_operation = null;
		List<TopoContextMenuItem> m_children = new ArrayList<TopoContextMenuItem>();
		
		public TopoContextMenuItem(ContextMenuItem item, Operation operation) {
			m_item = item;
			m_operation = operation;
		}
		
		public ContextMenuItem getItem() {
			return m_item;
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
		
		public TopoContextMenuItem addItem(String label, Operation operation) {
			TopoContextMenuItem topoContextMenuItem = new TopoContextMenuItem(m_item.addItem(label), operation);
			m_children.add(topoContextMenuItem);
			return topoContextMenuItem;
		}

		public String getName() {
			return m_item.getName();
		}

        public void setSeparatorVisible(boolean b) {
            m_item.setSeparatorVisible(b);
        }
		
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
	    TopoContextMenuItem item = new TopoContextMenuItem(addItem(label), operation);
	    m_items.add(item);
	    return item;
	}
	
	public List<TopoContextMenuItem> getItems() {
		return m_items;
	}
}
