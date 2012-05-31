package org.opennms.features.topology.app.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;


public class MenuBarBuilder {

    private LinkedHashMap<String, Object> m_menuBar = new LinkedHashMap<String, Object>();
    public MenuBarBuilder() {
        
    }
    
    private void add(List<String> menuPath, Command command, Map<String, Object> menu) {
        if(menuPath.isEmpty()) {
            return;
        }
        String first = menuPath.get(0);
        
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
            }else if(item instanceof Map) {
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

    public MenuBar get() {
        MenuBar menuBar = new MenuBar();
        
        for(Entry<String, Object> entry : m_menuBar.entrySet()) {
            if(entry.getValue() instanceof Map) {
                MenuBar.MenuItem menuItem = menuBar.addItem(entry.getKey(), null);
                addMenuItems(menuItem, (Map<String, Object>) entry.getValue());
            }else {
                menuBar.addItem(entry.getKey(), (Command) entry.getValue());
            }
            
        }
        return menuBar;
    }

    private void addMenuItems(MenuItem subMenu, Map<String, Object> value) {
        for(Entry<String, Object> entry : value.entrySet()) {
            if(entry.getValue() instanceof Map) {
                MenuBar.MenuItem subMenuItem = subMenu.addItem(entry.getKey(), null);
                addMenuItems(subMenuItem, (Map<String, Object>) entry.getValue());
            }else {
                subMenu.addItem(entry.getKey(), (Command) entry.getValue());
            }
            
        }
    }

    public void add(LinkedList<String> menuPath, Command command) {
        add(menuPath, command, m_menuBar);
    }

    public void addMenuCommand(Command command, String menuPosition) {
        if(menuPosition != null) {
            LinkedList<String> menuPath = new LinkedList(Arrays.asList(menuPosition.split("\\|")));
            add(menuPath, command);
        }
    }
}
