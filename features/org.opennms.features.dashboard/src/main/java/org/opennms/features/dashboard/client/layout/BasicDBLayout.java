/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.features.dashboard.client.layout;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.dashboard.client.dnd.MoveDragController;
import org.opennms.features.dashboard.client.dnd.ResizeDragController;
import org.opennms.features.dashboard.client.portlet.IBasicPortlet;

import com.allen_sauer.gwt.dnd.client.DragController;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * author: Tharindu Munasinghe (tharindumunasinghe@gmail.com)
 * org.opennms.features.dashboard
 */
public abstract class BasicDBLayout extends AbsolutePanel implements
        IBasicDBLayout {
    private static final String CSS_GRID_PANEL = "demo-gridview";
    
    protected List<IBasicPortlet> portletList;
    protected ResizeDragController resizeDragController;
    protected PickupDragController moveDragController;

    public BasicDBLayout() {
        this.setSize( "100%", "100%" );        
        this.setStyleName( CSS_GRID_PANEL );
        portletList = new ArrayList<IBasicPortlet>();
        
        init();
    }
    
    public abstract void init();
        
    public void setContent(int index) {
        if (index >= 0 && index < portletList.size()) {
            portletList.get(index).restoreWidget();
        }
    }
    
    public void closePortlet(int portlet) {
        if (portlet >= 0 && portlet < portletList.size()) {
            portletList.get(portlet).removeFromParent();
            portletList.remove(portlet);
        }
    }

    public void setHeaderCaption(int portlet, String text) {
        if (portlet >= 0 && portlet < portletList.size()) {
            portletList.get(portlet).setTitle(text);
        }
    }    

    public void selectPortlet(int portlet) {
        if (portlet >= 0 && portlet < portletList.size()) {
            portletList.get(portlet).selectPortlet();
        }
    }
    
    public void clearDashboard() {
        for (IBasicPortlet portlet : portletList) {
            portlet.makeNotDraggable();
            portlet.makeNotResizable();
            portlet.removeFromParent();
        }
        portletList.clear();
    }
}
