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

package org.opennms.features.dashboard.client.dnd;

import com.allen_sauer.gwt.dnd.client.AbstractDragController;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.util.DOMUtil;
import com.allen_sauer.gwt.dnd.client.util.Location;
import com.allen_sauer.gwt.dnd.client.util.WidgetLocation;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * author: Tharindu Munasinghe (tharindumunasinghe@gmail.com)
 * org.opennms.features.dashboard
 */
public class MoveDragController extends PickupDragController
{
//	private int boundaryOffsetX;
//	private int boundaryOffsetY;
//	private int dropTargetClientHeight;
//	private int dropTargetClientWidth;

	public MoveDragController( AbsolutePanel boundaryPanel )
	{
	    super( boundaryPanel, false );
	}

//	@Override
//	public void dragEnd()
//	{
//		super.dragEnd();
//	}

//    public void dragMove() {
//        Location widgetLocation = new WidgetLocation(context.boundaryPanel,
//                                                     null);
//        int desiredLeft = context.desiredDraggableX
//                - widgetLocation.getLeft();
//        int desiredTop = context.desiredDraggableY - widgetLocation.getTop();
//        boundaryOffsetX = 2;
//        boundaryOffsetY = 2;
//        if (getBehaviorConstrainedToBoundaryPanel()) {
//            desiredLeft = Math.max(boundaryOffsetX,
//                                   Math.min(desiredLeft,
//                                            dropTargetClientWidth));
//            desiredTop = Math.max(boundaryOffsetY,
//                                  Math.min(desiredTop, dropTargetClientHeight));
//        }
//        DOMUtil.fastSetElementPosition(context.draggable.getElement(),
//                                       desiredLeft, desiredTop);
//    }
//
//	@Override
//	public void dragStart()
//	{
//		super.dragStart();
//
//		// one time calculation of boundary panel location for efficiency during
//		// dragging
//		Location widgetLocation = new WidgetLocation( context.boundaryPanel, null );
//		boundaryOffsetX = widgetLocation.getLeft() + DOMUtil.getBorderLeft( context.boundaryPanel.getElement() );
//		boundaryOffsetY = widgetLocation.getTop() + DOMUtil.getBorderTop( context.boundaryPanel.getElement() );
//
//		boundaryOffsetX = 2;
//		boundaryOffsetY = 2;
//
//		dropTargetClientWidth = boundaryOffsetX + DOMUtil.getClientWidth( context.boundaryPanel.getElement() ) - context.draggable.getOffsetWidth() - DOMUtil.getBorderLeft( context.draggable.getElement() );
//		dropTargetClientHeight = boundaryOffsetY + DOMUtil.getClientHeight( context.boundaryPanel.getElement() ) - context.draggable.getOffsetHeight() - DOMUtil.getBorderTop( context.draggable.getElement() );
//	}

    @Override
    public void makeDraggable(Widget draggable) {
        try {
            super.makeDraggable(draggable);
        } catch (Exception ex) {
            // ex.printStackTrace();
        }
    }

    @Override
    public void makeNotDraggable(Widget draggable) {
        try {
            super.makeNotDraggable(draggable);
        } catch (Exception ex) {
            // ex.printStackTrace();
        }
    }
}
