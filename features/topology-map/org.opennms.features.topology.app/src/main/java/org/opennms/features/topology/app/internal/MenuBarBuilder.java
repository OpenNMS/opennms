package org.opennms.features.topology.app.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;


public class MenuBarBuilder {

    private LinkedHashMap<String, Object> m_menuBar = new LinkedHashMap<String, Object>();
    private List<String> m_menuOrder = new ArrayList<String>();
    private Map<String, List<String>> m_submenuOrderMap = new HashMap<String, List<String>>();
    
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
            }else if(item instanceof Map) {
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

    private Set<Entry<String, Object>> getSortedMenuItems() {
        LinkedHashMap<String, Object> sortedList = new LinkedHashMap<String, Object>();
        
        List<String> keys = new ArrayList<String>(m_menuBar.keySet());
        Collections.sort(keys, new Comparator<String>() {

            @Override
            public int compare(String menuName1, String menuName2) {
                int index1 = -1;
                int index2 = -1;
                
                if(m_menuOrder.contains(menuName1)) {
                    index1 = m_menuOrder.indexOf(menuName1);
                }else {
                    if(m_menuOrder.contains("Additions")) {
                        index1 = m_menuOrder.indexOf("Additions");
                    }else {
                        index1 = m_menuOrder.size();
                    }
                }
                
                if(m_menuOrder.contains(menuName2)) {
                    index2 = m_menuOrder.indexOf(menuName2);
                }else {
                    if(m_menuOrder.contains("Additions")) {
                        index2 = m_menuOrder.indexOf("Additions");
                    }else {
                        index2 = m_menuOrder.size();
                    }
                }
                
                return index1 == index2 ? menuName1.compareTo(menuName2) : index1 - index2;
            }
        });
        
        for(String key : keys) {
            
            sortedList.put(key, m_menuBar.get(key));
        }
        
        return sortedList.entrySet();
    }

    @SuppressWarnings("unchecked")
	private void addMenuItems(MenuItem subMenu, Map<String, Object> value) {
        
        Set<Entry<String, Object>> sortedEntrySet = getSortedSubmenuGroup(subMenu.getText(), value);
        for(Entry<String, Object> entry : sortedEntrySet) {
            if(entry.getValue() instanceof Map) {
                MenuBar.MenuItem subMenuItem = subMenu.addItem(entry.getKey(), null);
                addMenuItems(subMenuItem, (Map<String, Object>) entry.getValue());
            }else {
                if(entry.getKey().equals("separator")) {
                    subMenu.addSeparator();
                }else {
                    subMenu.addItem(entry.getKey(), (Command) entry.getValue());
                }
            }
            
        }
    }

    private Set<Entry<String, Object>> getSortedSubmenuGroup(final String parentMenuName, Map<String, Object> value) {
        
        LinkedHashMap<String, Object> sortedList = new LinkedHashMap<String, Object>();
        
        List<String> keys = new ArrayList<String>(value.keySet());
        Collections.sort(keys, new Comparator<String>() {

            @Override
            public int compare(String menuName1, String menuName2) {
                final List<String> submenuOrder = m_submenuOrderMap.get(parentMenuName) == null && m_submenuOrderMap.containsKey("default") ? m_submenuOrderMap.get("default") : new ArrayList<String>();
                
                int index1 = -1;
                int index2 = -1;
                
                String group1 = getGroupForLabel(menuName1);
                if(submenuOrder.contains(menuName1.toLowerCase()) && group1 == null) {
                    group1 = menuName1.toLowerCase();
                }
                
                String group2 = getGroupForLabel(menuName2);
                if(submenuOrder.contains(menuName2.toLowerCase()) && group2 == null) {
                    group2 = menuName2.toLowerCase();
                }
                
                if(submenuOrder.contains(group1)) {
                    index1 = submenuOrder.indexOf(group1);
                }else {
                    if(submenuOrder.contains("additions".toLowerCase())) {
                        index1 = submenuOrder.indexOf("additions".toLowerCase());
                    }else {
                        index1 = submenuOrder.size();
                    }
                }
                
                if(submenuOrder.contains(group2)) {
                    index2 = submenuOrder.indexOf(group2);
                }else {
                    if(submenuOrder.contains("additions")) {
                        index2 = submenuOrder.indexOf("additions");
                    }else {
                        index2 = submenuOrder.size();
                    }
                }
                
                return index1 == index2 ? menuName1.compareTo(menuName2) : index1 - index2;
            }
        });
        
        String prevGroup = null;
        for(String key : keys) {
            if(prevGroup != null && !prevGroup.equals(getGroupForLabel(key))) {
                sortedList.put("separator", null);
            }
           
            Object command = value.get(key);
            if(key.contains("?")) {
                sortedList.put(key.substring(0, key.indexOf("?")), command);
            }else {
                sortedList.put(key, command);
            }
            
            prevGroup = getGroupForLabel(key);
        }
        
        return sortedList.entrySet();
    }

    private void add(LinkedList<String> menuPath, Command command) {
        add(menuPath, command, m_menuBar);
    }

    public void addMenuCommand(Command command, String menuPosition) {
        if(menuPosition != null) {
            LinkedList<String> menuPath = new LinkedList<String>(Arrays.asList(menuPosition.split("\\|")));
            add(menuPath, command);
        }
    }
    
    public void setTopLevelMenuOrder(List<String> menuOrder) {
        m_menuOrder = menuOrder;
    }
    
    public void setSubMenuGroupOder(Map<String, List<String>> submenOrderMap) {
        m_submenuOrderMap = submenOrderMap;
    }

    private String getGroupForLabel(String label) {
        String group = null;
        String[] groupParams = label.split("\\?");
        
        for(String param : groupParams) {
            if(param.contains("group")) {
                String[] keyValue = param.split("=");
                group = keyValue[1];
            }
        }
        
        return group;
    }
}
