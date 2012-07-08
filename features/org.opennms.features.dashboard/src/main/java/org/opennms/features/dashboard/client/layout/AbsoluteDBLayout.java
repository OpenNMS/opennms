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

import org.opennms.features.dashboard.client.dnd.MoveDragController;
import org.opennms.features.dashboard.client.dnd.ResizeDragController;
import org.opennms.features.dashboard.client.portlet.IBasicPortlet;

import com.allen_sauer.gwt.dnd.client.drop.AbsolutePositionDropController;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * author: Tharindu Munasinghe (tharindumunasinghe@gmail.com)
 * org.opennms.features.dashboard
 */
public class AbsoluteDBLayout extends BasicDBLayout {
    public AbsoluteDBLayout() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void init() {
        resizeDragController = new ResizeDragController(this);
        resizeDragController.setBehaviorConstrainedToBoundaryPanel(true);
        resizeDragController.setBehaviorMultipleSelection(false);

        moveDragController = new MoveDragController(this);
        moveDragController.setBehaviorDragStartSensitivity(5);
        moveDragController.setBehaviorConstrainedToBoundaryPanel(true);
        moveDragController.setBehaviorMultipleSelection(false);
        
        AbsolutePositionDropController dropController = new AbsolutePositionDropController(this);
        moveDragController.registerDropController(dropController);
    }

    @Override
    public int addNewPortlet(IBasicPortlet portlet) {
        int index = addNewPortlet(0, 0, portlet);
        return index;
    }

    @Override
    public int addNewPortlet(int x, int y, IBasicPortlet portlet) {
        this.add(portlet.asWidget(), x, y);

        // make the portlet draggable
        portlet.makeResizable(resizeDragController);
        portlet.makeDraggable(moveDragController);

        portletList.add(portlet);
        return portletList.size() - 1;
    }
}
