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

package org.opennms.features.vaadin.app;

import com.vaadin.event.Action;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;

public abstract class Command extends Action implements MenuBar.Command {
	
    public Command(String caption) {
        super(caption);
    }

    private String m_parentMenu;
    private boolean m_action = false;
    private boolean m_menu = false;
    
    public boolean appliesToTarget(Object target) {
    	return true;
    }
    
    public void menuSelected(MenuItem selectedItem) {
        this.doCommand(null);
    }
    
    public abstract void doCommand(Object target);
	
	public void undoCommand() {}
	
    public String getMenuPosition() {
        return !m_menu ? null : m_parentMenu == null ? getCaption() : m_parentMenu + "|" + getCaption();
    }
    
    public Command setParentMenu(String parentMenu) {
        m_menu = true;
        m_parentMenu = parentMenu;
        return this;
    }
    
    public Command setAction() {
        m_action = true;
        return this;
    }

    public boolean isAction() {
        return m_action;
    }
    
    public String toString() {
        return getCaption();
    }

}
