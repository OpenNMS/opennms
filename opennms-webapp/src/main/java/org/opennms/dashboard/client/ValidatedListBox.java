/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.dashboard.client;


import com.google.gwt.user.client.ui.ListBox;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class ValidatedListBox extends ListBox {
    /**
     * 
     */
    private Dashlet m_dashlet;
    private boolean m_allowWrapAround = true;
    private ValidatedListBox m_parent = null;
    private DirectionalChangeListener m_directionalChangeListener = null;

    public ValidatedListBox(Dashlet dashlet) {
        super();
        m_dashlet = dashlet;
    }
    
    public void setParent(ValidatedListBox parent) {
        m_parent = parent;
    }
    
    public void setDirectionalChangeListener(DirectionalChangeListener listener) {
        m_directionalChangeListener = listener;
    }
    
    public String getSelectedValue() {
        int index = getSelectedIndex();
        if (index < 0 || index >= getItemCount()) {
            m_dashlet.error("Error: list box index " + index + " is out of range");
            return null;
        }
        
        return getValue(index);
    }

    public String getRelativeSelectedValue(int offset) {
        int relativeIndex = getSelectedIndex() + offset;
        if (relativeIndex < 0 || relativeIndex >= getItemCount()) {
            return null;
        }
        
        return getValue(relativeIndex);
    }
    
    public void adjustSelectedValue(int direction) {
        int newPrefabIndex = getSelectedIndex() + direction;
        if (newPrefabIndex < 0) {
            if (m_parent != null) {
                m_parent.adjustSelectedValue(-1);
                return;
            } else {
                if (m_allowWrapAround) {
                    newPrefabIndex = getItemCount() -1;
                } else {
                    return;
                }
            }
        } else if (newPrefabIndex >= getItemCount()) {
            if (m_parent != null) {
                m_parent.adjustSelectedValue(1);
                return;
            } else {
                if (m_allowWrapAround) {
                    newPrefabIndex = 0;
                } else {
                    return;
                }
            }
        }
        
        setSelectedIndex(newPrefabIndex);
        
        if (m_directionalChangeListener != null) {
            m_directionalChangeListener.onChange(this, direction);
        }
    }
}