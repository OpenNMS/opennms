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

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.topology.api.IViewContribution;

import com.vaadin.ui.TabSheet;

public class WidgetManager {

    private List<IViewContribution> m_viewContributors = new ArrayList<IViewContribution>();
    private List<WidgetUpdateListener> m_listeners = new ArrayList<WidgetUpdateListener>();
    
    public WidgetManager() {
        
    }
    
    public void addUpdateListener(WidgetUpdateListener listener) {
        m_listeners.add(listener);
        updateWidgetListeners();
    }
    
    public int widgetCount() {
        return m_viewContributors.size();
    }
    
    public TabSheet getTabSheet() {
        TabSheet tabSheet = new TabSheet();
        
        for(IViewContribution viewContrib : m_viewContributors) {
            
            if(viewContrib.getIcon() != null) {
                tabSheet.addTab(viewContrib.getView(), viewContrib.getTitle(), viewContrib.getIcon());
            } else {
                tabSheet.addTab(viewContrib.getView(), viewContrib.getTitle());
            }
            
        }
        
        return tabSheet;
    }
    
    public void onBind(IViewContribution viewContribution) {
        m_viewContributors.add(viewContribution);
        updateWidgetListeners();
    }

    private void updateWidgetListeners() {
        for(WidgetUpdateListener listener : m_listeners) {
            listener.widgetListUpdated(this);
        }
    }
    
    public void onUnbind(IViewContribution viewContribution) {
        m_viewContributors.remove(viewContribution);
        updateWidgetListeners();
    }

    public void removeUpdateListener(WidgetUpdateListener listener) {
        m_listeners.remove(listener);
    }
}
