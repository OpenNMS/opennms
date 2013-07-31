/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import java.util.Set;
import java.util.Map.Entry;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.app.internal.TopoContextMenu.TopoContextMenuItem;

public class ContextMenuBuilder extends MenuBuilder<Command, TopoContextMenuItem> {

	public TopoContextMenu get() {
		TopoContextMenu cMenu = new TopoContextMenu();
        
        Set<Entry<String, Object>> sortedEntrySet = getSortedMenuItems();
        for(Entry<String, Object> entry : sortedEntrySet) {
            if(entry.getValue() instanceof Map<?,?>) {
                TopoContextMenuItem menuItem = cMenu.addItem(entry.getKey(), (Operation)null);
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
	        if(entry.getValue() instanceof Map<?,?>) {
	            TopoContextMenuItem subMenuItem = subMenu.addItem(commandKey, null);
	            addMenuItems(subMenuItem, (Map<String, Object>) entry.getValue());
	        }else {
	            if(commandKey.startsWith("separator")) {
	                subMenu.setSeparatorVisible(true);
	            }else {
	                Command cmd = (Command) entry.getValue();
	                subMenu.addItem(removeLabelProperties(commandKey), cmd.getOperation());
	            }
	        }
	        
	    }
	}


	
}
