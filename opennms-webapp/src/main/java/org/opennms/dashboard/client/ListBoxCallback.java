/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.dashboard.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;

/**
 * <p>ListBoxCallback class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class ListBoxCallback implements AsyncCallback<String[][]> {
    private DashletLoader m_dashletLoader;
    private int m_direction = 1;
    private ListBox m_listBox;
    private String m_emptyListItemName = null;
    private String m_emptyListItemValue;
    private String m_nullListItemName;
    private String m_nullListItemValue;
    
    /**
     * <p>Constructor for ListBoxCallback.</p>
     *
     * @param dashlet a {@link org.opennms.dashboard.client.DashletLoader} object.
     * @param listBox a {@link com.google.gwt.user.client.ui.ListBox} object.
     */
    public ListBoxCallback(DashletLoader dashlet, ListBox listBox) {
        m_dashletLoader = dashlet;
        m_listBox = listBox;
    }
    
    /**
     * <p>getListBox</p>
     *
     * @return a {@link com.google.gwt.user.client.ui.ListBox} object.
     */
    public ListBox getListBox() {
        return m_listBox;
    }

    /**
     * <p>setDirection</p>
     *
     * @param direction a int.
     */
    public void setDirection(int direction) {
        m_direction = direction;
    }
    
    /**
     * <p>getDirection</p>
     *
     * @return a int.
     */
    public int getDirection() {
        return m_direction;
    }
    
    /**
     * <p>setNullListItem</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public void setNullListItem(String name, String value) {
        m_nullListItemName = name;
        m_nullListItemValue = value;
    }
    
    /**
     * <p>setEmptyListItem</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public void setEmptyListItem(String name, String value) {
        m_emptyListItemName = name;
        m_emptyListItemValue = value;
    }

    /**
     * <p>onDataLoaded</p>
     *
     * @param resources an array of {@link java.lang.String} objects.
     */
    public void onDataLoaded(String[][] resources) {
        m_listBox.clear();
        
        if (resources == null) {
            if (m_nullListItemName != null) {
                m_listBox.addItem(m_nullListItemName, m_nullListItemValue);
            }
        } else  if (resources.length == 0) {
            if (m_emptyListItemName != null) {
                m_listBox.addItem(m_emptyListItemName, m_emptyListItemValue);
            }
        } else {
            for (int i = 0; i < resources.length; i++) {
                m_listBox.addItem(resources[i][1], resources[i][0]);
            }
        }
        
        int itemCount = m_listBox.getItemCount();
        if (m_direction < 0 && itemCount > 0) {
            m_listBox.setSelectedIndex(itemCount - 1);
        } else if (m_direction > 0 && itemCount > 0) {
            m_listBox.setSelectedIndex(0);
        }

        m_dashletLoader.complete();
    }
    
    /** {@inheritDoc} */
    @Override
    public void onFailure(Throwable caught) {
        m_dashletLoader.loadError(caught);
    }

    /** {@inheritDoc} */
    @Override
    public void onSuccess(String[][] result) {
        onDataLoaded(result);
    }

}
