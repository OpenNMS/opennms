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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;

public class MenuBarBuilder extends MenuBuilder<MenuBar.Command, MenuItem> {

    @SuppressWarnings("unchecked")
	public MenuBar get() {
        MenuBar menuBar = new MenuBar();
        
        Set<Entry<String, Object>> sortedEntrySet = getSortedMenuItems();
        for(Entry<String, Object> entry : sortedEntrySet) {
            if(entry.getValue() instanceof Map<?,?>) {
                MenuBar.MenuItem menuItem = menuBar.addItem(entry.getKey(), null);
                addMenuItems(menuItem, (Map<String, Object>) entry.getValue());
            }else {
                menuBar.addItem(entry.getKey(), (Command) entry.getValue());
            }
            
        }
        return menuBar;
    }

	@SuppressWarnings("unchecked")
    @Override
	protected void addMenuItems(MenuItem subMenu, Map<String, Object> value) {
	    
	    Set<Entry<String, Object>> sortedEntrySet = getSortedSubmenuGroup(subMenu.getText(), value);
	    for(Entry<String, Object> entry : sortedEntrySet) {
	        String commandKey = entry.getKey();
	        if(entry.getValue() instanceof Map<?,?>) {
	            MenuBar.MenuItem subMenuItem = subMenu.addItem(commandKey, null);
	            addMenuItems(subMenuItem, (Map<String, Object>) entry.getValue());
	        }else {
	            if(commandKey.startsWith("separator")) {
	                subMenu.addSeparator();
	            }else {
	                subMenu.addItem(removeLabelProperties(commandKey), (Command) entry.getValue());
	            }
	        }
	        
	    }
	}

}
