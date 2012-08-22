package org.opennms.features.topology.app.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;


public class MenuBarBuilder extends MenuBuilder<MenuBar.Command, MenuItem> {

    public MenuBarBuilder() {
        
    }
    
    private void add(List<String> menuPath, Command command, Map<String, Object> menu) {
        if(menuPath.isEmpty()) {
            return;
        }
        
        String first = menuPath.get(0).contains(".") ? menuPath.get(0).substring(0, menuPath.get(0).indexOf('.')) : menuPath.get(0);
        
        if(menuPath.size() == 1) {
            if(menu.containsKey(first)) {
                add(Collections.singletonList(first + "_dup"), command, menu );
            }else {
                menu.put(first, command);
            }
            
        }else {
            Object item = menu.get(first);
            if(item == null) {
                Map<String, Object> subMenu = new LinkedHashMap<String, Object>();
                menu.put(first, subMenu);
                add(menuPath.subList(1, menuPath.size()), command, subMenu);
            }else if(item instanceof Map<?,?>) {
                @SuppressWarnings("unchecked")
                Map<String, Object> subMenu = (Map<String, Object>) item;
                add(menuPath.subList(1, menuPath.size()), command, subMenu);
            }else {
                List<String> newMenuPath = new LinkedList<String>();
                newMenuPath.add(first + "_dup");
                newMenuPath.addAll(menuPath.subList(1, menuPath.size()));
                add(newMenuPath, command, menu);
            }
            
        }
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
