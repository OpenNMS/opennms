/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.dashboard.client;


import com.google.gwt.user.client.ui.ListBox;

/**
 * <p>ValidatedListBox class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class ValidatedListBox extends ListBox {
    /**
     * 
     */
    private Dashlet m_dashlet;
    private boolean m_allowWrapAround = true;
    private ValidatedListBox m_parent = null;
    private DirectionalChangeHandler m_directionalChangeHandler = null;

    /**
     * <p>Constructor for ValidatedListBox.</p>
     *
     * @param dashlet a {@link org.opennms.dashboard.client.Dashlet} object.
     */
    public ValidatedListBox(Dashlet dashlet) {
        super();
        setStyleName("form-control");
        m_dashlet = dashlet;
    }
    
    /**
     * <p>setParent</p>
     *
     * @param parent a {@link org.opennms.dashboard.client.ValidatedListBox} object.
     */
    public void setParent(ValidatedListBox parent) {
        m_parent = parent;
    }
    
    /**
     * <p>setDirectionalChangeHandler</p>
     *
     * @param handler a {@link org.opennms.dashboard.client.DirectionalChangeHandler} object.
     */
    public void setDirectionalChangeHandler(DirectionalChangeHandler handler) {
        m_directionalChangeHandler = handler;
    }
    
    /**
     * <p>getSelectedValue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSelectedValue() {
        int index = getSelectedIndex();
        if (index < 0 || index >= getItemCount()) {
            m_dashlet.error("Error: list box index " + index + " is out of range");
            return null;
        }
        
        return getValue(index);
    }

    /**
     * <p>getRelativeSelectedValue</p>
     *
     * @param offset a int.
     * @return a {@link java.lang.String} object.
     */
    public String getRelativeSelectedValue(int offset) {
        int relativeIndex = getSelectedIndex() + offset;
        if (relativeIndex < 0 || relativeIndex >= getItemCount()) {
            return null;
        }
        
        return getValue(relativeIndex);
    }
    
    /**
     * <p>adjustSelectedValue</p>
     *
     * @param direction a int.
     */
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
        
        if (m_directionalChangeHandler != null) {
            m_directionalChangeHandler.onChange(null, direction);
        }
    }
}
