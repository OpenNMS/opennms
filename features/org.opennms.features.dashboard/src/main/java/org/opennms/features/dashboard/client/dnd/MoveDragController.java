package org.opennms.features.dashboard.client.dnd;

import com.allen_sauer.gwt.dnd.client.AbstractDragController;
import com.allen_sauer.gwt.dnd.client.util.DOMUtil;
import com.allen_sauer.gwt.dnd.client.util.Location;
import com.allen_sauer.gwt.dnd.client.util.WidgetLocation;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;

public class MoveDragController extends AbstractDragController
{
	private int boundaryOffsetX;
	private int boundaryOffsetY;
	private int dropTargetClientHeight;
	private int dropTargetClientWidth;

	public MoveDragController( AbsolutePanel boundaryPanel )
	{
		super( boundaryPanel );
	}

	@Override
	public void dragEnd()
	{
		super.dragEnd();
	}

	public void dragMove()
	{
		Location widgetLocation = new WidgetLocation( context.boundaryPanel, null );
		int desiredLeft = context.desiredDraggableX - widgetLocation.getLeft();
		int desiredTop = context.desiredDraggableY - widgetLocation.getTop();
		boundaryOffsetX = 2;
		boundaryOffsetY = 2;
		if ( getBehaviorConstrainedToBoundaryPanel() )
		{
			desiredLeft = Math.max( boundaryOffsetX, Math.min( desiredLeft, dropTargetClientWidth ) );
			desiredTop = Math.max( boundaryOffsetY, Math.min( desiredTop, dropTargetClientHeight ) );
		}
		DOMUtil.fastSetElementPosition( context.draggable.getElement(), desiredLeft, desiredTop );
	}

	@Override
	public void dragStart()
	{
		super.dragStart();

		// one time calculation of boundary panel location for efficiency during
		// dragging
		Location widgetLocation = new WidgetLocation( context.boundaryPanel, null );
		boundaryOffsetX = widgetLocation.getLeft() + DOMUtil.getBorderLeft( context.boundaryPanel.getElement() );
		boundaryOffsetY = widgetLocation.getTop() + DOMUtil.getBorderTop( context.boundaryPanel.getElement() );

		boundaryOffsetX = 2;
		boundaryOffsetY = 2;

		dropTargetClientWidth = boundaryOffsetX + DOMUtil.getClientWidth( context.boundaryPanel.getElement() ) - context.draggable.getOffsetWidth() - DOMUtil.getBorderLeft( context.draggable.getElement() );
		dropTargetClientHeight = boundaryOffsetY + DOMUtil.getClientHeight( context.boundaryPanel.getElement() ) - context.draggable.getOffsetHeight() - DOMUtil.getBorderTop( context.draggable.getElement() );
	}

	@Override
	public void makeDraggable( Widget draggable )
	{
		try
		{
			super.makeDraggable( draggable );
		}
		catch ( Exception ex )
		{
			ex.printStackTrace();
		}
	}

	@Override
	public void makeNotDraggable( Widget draggable )
	{
		try
		{
			super.makeNotDraggable( draggable );
		}
		catch ( Exception ex )
		{
			ex.printStackTrace();
		}
	}

}


