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


public abstract class MenuBuilder<T, K>{

    private static final String TOP_LEVEL_ADDITIONS = "Additions";
	protected Map<String, Object> m_menuBar = new LinkedHashMap<String, Object>();
	private List<String> m_menuOrder = new ArrayList<String>();
	private Map<String, List<String>> m_submenuOrderMap = new HashMap<String, List<String>>();

	private void add(List<String> menuPath, T command, Map<String, Object> menu) {
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

	private void add(List<String> menuPath, T command) {
	    add(menuPath, command, m_menuBar);
	}

	public void addMenuCommand(T t, String menuPosition) {
	    if(menuPosition != null) {
	        LinkedList<String> menuPath = new LinkedList<String>(Arrays.asList(menuPosition.split("\\|")));
	        add(menuPath, t);
	    }
	}
	
	protected abstract void addMenuItems(K subMenu, Map<String, Object> value);

	protected Set<Entry<String, Object>> getSortedMenuItems() {
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
	                if(m_menuOrder.contains(TOP_LEVEL_ADDITIONS)) {
	                    index1 = m_menuOrder.indexOf(TOP_LEVEL_ADDITIONS);
	                }else {
	                    index1 = m_menuOrder.size();
	                }
	            }
	            
	            if(m_menuOrder.contains(menuName2)) {
	                index2 = m_menuOrder.indexOf(menuName2);
	            }else {
	                if(m_menuOrder.contains(TOP_LEVEL_ADDITIONS)) {
	                    index2 = m_menuOrder.indexOf(TOP_LEVEL_ADDITIONS);
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

	protected static String removeLabelProperties(String commandKey) {
	    if(commandKey.contains("?")) {
	        return commandKey.substring(0, commandKey.indexOf('?'));
	    }else {
	        return commandKey;
	    }
	    
	    
	}

	protected static Map<String, String> getLabelProperties(String commandLabel) {
	    Map<String, String> propMap = new HashMap<String, String>();
	    
	    if(commandLabel.contains("?")) {
	        String propStr = commandLabel.substring(commandLabel.indexOf('?') + 1, commandLabel.length());
	        String[] properties = propStr.split(";");
	        
	        for(String property : properties) {
	            String[] propKeyVal = property.split("=");
	            if(propKeyVal.length > 1) {
	                propMap.put(propKeyVal[0],propKeyVal[1]);
	            }
	        }
	    }
	    
	    return propMap;
	}

	protected Set<Entry<String, Object>> getSortedSubmenuGroup(final String parentMenuName, Map<String, Object> value) {
	        
	        LinkedHashMap<String, Object> sortedList = new LinkedHashMap<String, Object>();
	        
	        List<String> keys = new ArrayList<String>(value.keySet());
	        final List<String> submenuOrder = m_submenuOrderMap.get(parentMenuName) != null ? m_submenuOrderMap.get(parentMenuName) :  m_submenuOrderMap.containsKey("default") ? m_submenuOrderMap.get("default") : new ArrayList<String>();
	        Collections.sort(keys, new Comparator<String>() {
	
	            @Override
	            public int compare(String menuName1, String menuName2) {
	                
	                int index1 = -1;
	                int index2 = -1;
	                
	                String group1 = getGroupForLabel(menuName1, submenuOrder);
	                if(submenuOrder.contains(menuName1.toLowerCase()) && group1 == null) {
	                    group1 = menuName1.toLowerCase();
	                }
	                
	                String group2 = getGroupForLabel(menuName2, submenuOrder);
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
	        int separatorCount = 0;
	        for(String key : keys) {
	            if(prevGroup != null && !prevGroup.equals(getGroupForLabel(key, submenuOrder))) {
	                sortedList.put("separator" + separatorCount++, null);
	            }
	           
	            Object command = value.get(key);
	//            if(key.contains("?")) {
	//                sortedList.put(key.substring(0, key.indexOf("?")), command);
	//            }else {
	//                
	//            }
	            sortedList.put(key, command);
	            
	            prevGroup = getGroupForLabel(key, submenuOrder);
	        }
	        
	        return sortedList.entrySet();
	    }

	public void setTopLevelMenuOrder(List<String> menuOrder) {
	    m_menuOrder = menuOrder;
	}

	public void setSubMenuGroupOrder(Map<String, List<String>> submenOrderMap) {
	    m_submenuOrderMap = submenOrderMap;
	}

	protected static String getGroupForLabel(String label, List<String> submenuOrder) {
	    String group = null;
	    String[] groupParams = label.split("\\?");
	    
	    for(String param : groupParams) {
	        if(param.contains("group")) {
	            String[] keyValue = param.split("=");
	            group = keyValue[1];
	            return submenuOrder.contains(group) ? group : null;
	            
	        }
	    }
	    
	    return null;
	}

}
