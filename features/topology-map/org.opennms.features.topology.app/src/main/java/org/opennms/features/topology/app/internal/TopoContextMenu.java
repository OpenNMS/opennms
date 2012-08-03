package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.List;

import org.vaadin.peter.contextmenu.ContextMenu;

public class TopoContextMenu extends ContextMenu {


	public class TopoContextMenuItem  {
		
		ContextMenuItem m_item;
		List<TopoContextMenuItem> m_children;
		
		public TopoContextMenuItem(ContextMenuItem item) {
			m_item = item;
		}
		
		public ContextMenuItem getItem() {
			return m_item;
		}
		
		public boolean hasChildren() {
			return m_children == null || m_children.size() == 0 ? false : true;
		}
		
		public List<TopoContextMenuItem> getChildren() {
			return m_children;
		}
		
		public TopoContextMenuItem addItem(ContextMenuItem item) {
			TopoContextMenuItem topoContextMenuItem = new TopoContextMenuItem(item);
			m_children.add(topoContextMenuItem);
			return topoContextMenuItem;
		}

		public String getName() {
			return m_item.getName();
		}
		
	}

	private List<ContextMenuItem> m_items = new ArrayList<ContextMenuItem>();
	
	@Override
	public ContextMenuItem addItem(String label){
		ContextMenuItem newItem = super.addItem(label);
		m_items.add(newItem);
		return newItem;
	}
	
	public List<ContextMenuItem> getItems() {
		return m_items;
	}
}
