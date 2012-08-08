package org.opennms.features.topology.app.internal;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.opennms.features.topology.app.internal.TopoContextMenu.TopoContextMenuItem;

@SuppressWarnings("unchecked")
public class ContextMenuBuilder extends MenuBuilder<Command, TopoContextMenuItem> {

	public TopoContextMenu get() {
		TopoContextMenu cMenu = new TopoContextMenu();
        
        Set<Entry<String, Object>> sortedEntrySet = getSortedMenuItems();
        for(Entry<String, Object> entry : sortedEntrySet) {
            if(entry.getValue() instanceof Map) {
                TopoContextMenuItem menuItem = cMenu.addItem(entry.getKey(), null);
                addMenuItems(menuItem, (Map<String, Object>) entry.getValue());
            }else {
                OperationCommand command = (OperationCommand) entry.getValue();
                cMenu.addItem(entry.getKey(), command.getOperation());
            }
            
        }
        return cMenu;
	}
	
	@Override
	protected void addMenuItems(TopoContextMenuItem subMenu, Map<String, Object> value) {

	    Set<Entry<String, Object>> sortedEntrySet = getSortedSubmenuGroup(subMenu.getName(), value);
	    for(Entry<String, Object> entry : sortedEntrySet) {
	        String commandKey = entry.getKey();
	        if(entry.getValue() instanceof Map) {
	            TopoContextMenuItem subMenuItem = subMenu.addItem(commandKey, null);
	            addMenuItems(subMenuItem, (Map<String, Object>) entry.getValue());
	        }else {
	            if(commandKey.equals("separator")) {
	                subMenu.setSeparatorVisible(true);
	            }else {
	                Command cmd = (Command) entry.getValue();
	                subMenu.addItem(removeLabelProperties(commandKey), cmd.getOperation());
	            }
	        }
	        
	    }
	}


	
}
