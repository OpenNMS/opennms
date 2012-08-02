package org.opennms.features.topology.app.internal;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;


public class MenuBarBuilder extends MenuBuilder<MenuBar.Command, MenuItem> {

    public MenuBarBuilder() {
        
    }
    
    @SuppressWarnings("unchecked")
	public MenuBar get() {
        MenuBar menuBar = new MenuBar();
        
        Set<Entry<String, Object>> sortedEntrySet = getSortedMenuItems();
        for(Entry<String, Object> entry : sortedEntrySet) {
            if(entry.getValue() instanceof Map) {
                MenuBar.MenuItem menuItem = menuBar.addItem(entry.getKey(), null);
                addMenuItems(menuItem, (Map<String, Object>) entry.getValue());
            }else {
                menuBar.addItem(entry.getKey(), (Command) entry.getValue());
            }
            
        }
        return menuBar;
    }

	@SuppressWarnings("unchecked")
	protected void addMenuItems(MenuItem subMenu, Map<String, Object> value) {
	    
	    Set<Entry<String, Object>> sortedEntrySet = getSortedSubmenuGroup(subMenu.getText(), value);
	    for(Entry<String, Object> entry : sortedEntrySet) {
	        String commandKey = entry.getKey();
	        if(entry.getValue() instanceof Map) {
	            MenuBar.MenuItem subMenuItem = subMenu.addItem(commandKey, null);
	            addMenuItems(subMenuItem, (Map<String, Object>) entry.getValue());
	        }else {
	            if(commandKey.equals("separator")) {
	                subMenu.addSeparator();
	            }else {
	                subMenu.addItem(removeLabelProperties(commandKey), (Command) entry.getValue());
	            }
	        }
	        
	    }
	}

}
