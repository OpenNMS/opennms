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

package org.opennms.features.dashboard.client.portlet;

import org.opennms.features.dashboard.client.dnd.ResizeDragController;

import com.allen_sauer.gwt.dnd.client.DragController;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * author: Tharindu Munasinghe (tharindumunasinghe@gmail.com)
 * org.opennms.features.dashboard
 */
public interface IBasicPortlet {

    void makeDraggable(DragController moveDragController);

    void makeResizable(ResizeDragController resizeDragController);

    void makeNotResizable();

    void makeNotDraggable();

    void setContentSize(int width, int height);

    void setHeight(String height);

    void setWidth(String width);

    void setPixelSize(int width, int height);

    void setSize(String width, String height);
    
    void clearPortletContent();
    
    void addPortletContent(Widget w);
    
    void setTitle(String title);
    
    void setContent(Widget w);
    
    void selectPortlet();
    
    void restoreWidget( );
    
    int getContentHeight();

    int getContentWidth();
    
    Widget getContentWidget();
    
    Widget asWidget() ;
    
    void removeFromParent();
}
