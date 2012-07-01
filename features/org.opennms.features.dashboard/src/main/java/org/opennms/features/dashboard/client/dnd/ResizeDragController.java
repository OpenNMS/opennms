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

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;
import com.allen_sauer.gwt.dnd.client.AbstractDragController;
import com.allen_sauer.gwt.dnd.client.drop.BoundaryDropController;
import java.util.HashMap;

import org.opennms.features.dashboard.client.portlet.AbsPopup;
import org.opennms.features.dashboard.client.portlet.AbsPopup.DirectionConstant;

/**
 * author: Tharindu Munasinghe (tharindumunasinghe@gmail.com)
 * org.opennms.features.dashboard
 */
public final class ResizeDragController extends AbstractDragController
{

	private static final int MIN_WIDGET_SIZE = 10;
	private HashMap<Widget, DirectionConstant> directionMap = new HashMap<Widget, DirectionConstant>();
	private AbsPopup windowPanel = null;

	public ResizeDragController( AbsolutePanel boundaryPanel )
	{
		super( boundaryPanel );
	}

	@Override
	public void dragMove()
	{
		int direction = ( ( ResizeDragController ) context.dragController ).getDirection( context.draggable ).directionBits;
		if ( ( direction & AbsPopup.DIRECTION_SOUTH ) != 0 )
		{
			int delta = context.desiredDraggableY - context.draggable.getAbsoluteTop();
			if ( delta != 0 && (windowPanel.getContentHeight() + delta)>0)
			{
				windowPanel.setContentSize( windowPanel.getContentWidth(), windowPanel.getContentHeight() + delta );
			}
		}
		if ( ( direction & AbsPopup.DIRECTION_EAST ) != 0 )
		{
			int delta = context.desiredDraggableX - context.draggable.getAbsoluteLeft();
			if ( delta != 0 && (windowPanel.getContentWidth() + delta)>0)
			{
				windowPanel.setContentSize( windowPanel.getContentWidth() + delta, windowPanel.getContentHeight() );
			}
		}
	}

	@Override
	public void dragStart()
	{
		super.dragStart();
		windowPanel = ( AbsPopup ) context.draggable.getParent().getParent();
	}

	public void makeDraggable( Widget widget, AbsPopup.DirectionConstant direction )
	{
		super.makeDraggable( widget );
		directionMap.put( widget, direction );
	}

	protected BoundaryDropController newBoundaryDropController( AbsolutePanel boundaryPanel, boolean allowDroppingOnBoundaryPanel )
	{
		if ( allowDroppingOnBoundaryPanel )
		{
			throw new IllegalArgumentException();
		}
		return new BoundaryDropController( boundaryPanel, false );
	}

	private DirectionConstant getDirection( Widget draggable )
	{
		return directionMap.get( draggable );
	}
}
