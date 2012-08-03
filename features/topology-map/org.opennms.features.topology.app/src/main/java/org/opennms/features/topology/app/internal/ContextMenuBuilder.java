package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;

@SuppressWarnings("unchecked")
public class ContextMenuBuilder extends MenuBuilder<Command, ContextMenuItem> {

	private List<ContextMenuItem> m_itemList = new ArrayList<ContextMenuItem>();
	
	public TopoContextMenu get() {
		TopoContextMenu cMenu = new TopoContextMenu();
        
        Set<Entry<String, Object>> sortedEntrySet = getSortedMenuItems();
        for(Entry<String, Object> entry : sortedEntrySet) {
            if(entry.getValue() instanceof Map) {
                ContextMenuItem menuItem = cMenu.addItem(entry.getKey());
                m_itemList.add(menuItem);
                addMenuItems(menuItem, (Map<String, Object>) entry.getValue());
            }else {
                cMenu.addItem(entry.getKey());
            }
            
        }
        return cMenu;
	}
	
	public List<ContextMenuItem> getContextMenuItems() {
		return m_itemList;
	}

	@Override
	protected void addMenuItems(ContextMenuItem subMenu, Map<String, Object> value) {

	    Set<Entry<String, Object>> sortedEntrySet = getSortedSubmenuGroup(subMenu.getName(), value);
	    for(Entry<String, Object> entry : sortedEntrySet) {
	        String commandKey = entry.getKey();
	        if(entry.getValue() instanceof Map) {
	            ContextMenuItem subMenuItem = subMenu.addItem(commandKey);
	            addMenuItems(subMenuItem, (Map<String, Object>) entry.getValue());
	        }else {
	            if(commandKey.equals("separator")) {
	                subMenu.setSeparatorVisible(true);
	            }else {
	                subMenu.addItem(removeLabelProperties(commandKey));
	            }
	        }
	        
	    }
	}


	
}
