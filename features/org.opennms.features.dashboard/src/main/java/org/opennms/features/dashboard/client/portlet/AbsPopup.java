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
import org.opennms.features.dashboard.client.dnd.MoveDragController;
import org.opennms.features.dashboard.client.resource.WidgetResource;

import com.allen_sauer.gwt.dnd.client.AbstractDragController;
import com.allen_sauer.gwt.dnd.client.util.Location;
import com.allen_sauer.gwt.dnd.client.util.WidgetLocation;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * author: Tharindu Munasinghe (tharindumunasinghe@gmail.com)
 * org.opennms.features.dashboard
 */
public abstract class AbsPopup extends FocusPanel
{
	protected WidgetResource imageResource = WidgetResource.WIDGETRESOURCE;
//	protected WidgetCssResource cssResource = imageResource.widgetsCssResource();

	public static class DirectionConstant
	{

		public final int directionBits;

		public final String directionLetters;

		private DirectionConstant( int directionBits, String directionLetters )
		{
			this.directionBits = directionBits;
			this.directionLetters = directionLetters;
		}
	}

	public static final int DIRECTION_EAST = 0x0001;
	public static final int DIRECTION_NORTH = 0x0002;
	public static final int DIRECTION_SOUTH = 0x0004;
	public static final int DIRECTION_WEST = 0x0008;
	public static final DirectionConstant EAST = new DirectionConstant( DIRECTION_EAST, "e" );
	public static final DirectionConstant NORTH = new DirectionConstant( DIRECTION_NORTH, "n" );
	public static final DirectionConstant NORTH_EAST = new DirectionConstant( DIRECTION_NORTH | DIRECTION_EAST, "ne" );
	public static final DirectionConstant NORTH_WEST = new DirectionConstant( DIRECTION_NORTH | DIRECTION_WEST, "nw" );
	public static final DirectionConstant SOUTH = new DirectionConstant( DIRECTION_SOUTH, "s" );
	public static final DirectionConstant SOUTH_EAST = new DirectionConstant( DIRECTION_SOUTH | DIRECTION_EAST, "se" );
	public static final DirectionConstant SOUTH_WEST = new DirectionConstant( DIRECTION_SOUTH | DIRECTION_WEST, "sw" );
	public static final DirectionConstant WEST = new DirectionConstant( DIRECTION_WEST, "w" );

	private static final int BORDER_THICKNESS = 5;
	protected static final int TITLE_HEIGHT = 22;
	protected static final int BUTTON_PANEL_HEIGHT=23;
	private int contentHeight;
	private int contentWidth;
	private Widget eastWidget;
	private boolean initialLoad = false;
	private Widget southWidget;
	private Panel cornerWidget;
	
	protected Label title;
	protected PushButton closeBtn;
	
	protected Grid mainPanel;
	
	protected SimplePanel messageBody;
	protected SimplePanel imageBody;
	
	protected Button okBtn;
	protected Button cancelBtn;
	
	protected ClickHandler closeHandler;
	private AbstractDragController moveDragController;
	private ResizeDragController resizeDragController;
	protected FlexTable bodyPanel;
	protected FlexTable titlePanel;
	
	public AbsPopup()
	{		
		mainPanel = new Grid( 3, 3 );
		mainPanel.getCellFormatter().setWidth( 0, 0, BORDER_THICKNESS+"px" );
		mainPanel.getCellFormatter().setHeight( 0, 0, BORDER_THICKNESS+"px" );
		mainPanel.getCellFormatter().setWidth( 2, 2, BORDER_THICKNESS+"px" );
		mainPanel.getCellFormatter().setHeight( 2, 2, BORDER_THICKNESS+"px" );
		add( mainPanel );
		addStyleName( "popup-WindowPanel" );
		mainPanel.setCellSpacing( 0 );
		mainPanel.setCellPadding( 0 );
		mainPanel.addStyleName( "borderPanel" );
		
		DOM.setStyleAttribute( this.getElement(), "zIndex", "20000" );
		DOM.setStyleAttribute( this.getElement(), "borderColor","#C3D9FF");
		DOM.setStyleAttribute( this.getElement(), "border", "none" );
		DOM.setStyleAttribute( this.getElement(), "background", "none" );
		DOM.setStyleAttribute( this.getElement(), "padding","0px");
		
		bodyPanel=new FlexTable();
		bodyPanel.setSize( "100%", "100%" );
		bodyPanel.setCellSpacing( 0 );
		bodyPanel.setCellPadding( 0 );
		mainPanel.setWidget( 1, 1, bodyPanel );
		mainPanel.getCellFormatter().setVerticalAlignment( 1, 1, HasVerticalAlignment.ALIGN_TOP );
		
		closeHandler = new ClickHandler() {
			
			public void onClick(ClickEvent arg0) {
				AbsPopup.this.setVisible( false );
				AbsPopup.this.removeFromParent();
			}
		};
			
		titlePanel=new FlexTable();
		titlePanel.setCellSpacing( 0 );
		titlePanel.setCellPadding( 0 );
		titlePanel.setWidth( "100%" );
				
		title=new Label( );
		title.setStyleName( "popup-WindowPanel-title" );
		title.setSize( "100%", TITLE_HEIGHT+"px" );
		titlePanel.setWidget( 0, 0, title );		
				
		closeBtn=new PushButton(new Image( imageResource.dropSmall() ));
		closeBtn.setSize( "auto", "auto" );
		closeBtn.setStyleName( "popup-WindowPanel-close" );
		closeBtn.addClickHandler( closeHandler );
		titlePanel.setWidget( 0, 1, closeBtn );
		titlePanel.getCellFormatter().setWidth( 0, 1, "30px" );
							
		HorizontalPanel contentpanel=new HorizontalPanel();
		contentpanel.setWidth( "100%" );
		bodyPanel.setWidget( 1, 0, contentpanel );
		bodyPanel.getCellFormatter().setVerticalAlignment( 1, 0, HasVerticalAlignment.ALIGN_TOP );
		
		imageBody=new SimplePanel();
		imageBody.setStyleName( "popup-WindowPanel-image" );
		contentpanel.add( imageBody );
		
		messageBody=new SimplePanel();
		messageBody.setStyleName( "popup-WindowPanel-body" );
		contentpanel.add( messageBody );				
		
		moveDragController=new MoveDragController( RootPanel.get() );
		moveDragController.setBehaviorDragStartSensitivity( 5 );
		moveDragController.setBehaviorConstrainedToBoundaryPanel(true);
		moveDragController.setBehaviorMultipleSelection(false);
		moveDragController.makeDraggable( this, title );	
		
		resizeDragController = new ResizeDragController( RootPanel.get() );
		resizeDragController.setBehaviorConstrainedToBoundaryPanel( true );
		resizeDragController.setBehaviorMultipleSelection( false );
				
		setupCell( 0, 0, NORTH_WEST, false );
		setupCell( 0, 1, NORTH, false );
		setupCell( 0, 2, NORTH_EAST, false );

		setupCell( 1, 0, WEST, false );
		eastWidget = setupCell( 1, 2, EAST, true );
		eastWidget.setSize( BORDER_THICKNESS+"px", "100%" );

		setupCell( 2, 0, SOUTH_WEST, false );
		southWidget = setupCell( 2, 1, SOUTH, true );
		southWidget.setSize( "100%", BORDER_THICKNESS + "px" );
		
		cornerWidget = setupCell( 2, 2, SOUTH_EAST, true );
		Image corner=new Image( imageResource.resizeCorner() );
		cornerWidget.add( corner );
		DOM.setStyleAttribute( corner.getElement(), "position", "absolute" );
		DOM.setStyleAttribute( corner.getElement(), "right","3px");
		DOM.setStyleAttribute( corner.getElement(), "bottom", "3px" );
		
		okBtn = new Button( " Ok " );
		okBtn.setPixelSize( 75, BUTTON_PANEL_HEIGHT );
		okBtn.setStyleName( "popup-WindowPanel-btn" );
		
		cancelBtn = new Button( "Cancel" );
		cancelBtn.setPixelSize( 75, BUTTON_PANEL_HEIGHT );
		cancelBtn.setStyleName( "popup-WindowPanel-btn" );			
	}
	
	public void clear()
	{
		messageBody.clear();
	}
	
	public void showpopUp()
	{
		center();
//		show();	
	}
	
	public void showPopup( int left, int top )
	{
//		setPopupPosition( left, top );
//		show();		
		RootPanel.get().add( this, left, top );
		if ( !isVisible() )
		{
	    	setVisible(true);			
		}
	}
	
	public int getContentHeight()
	{
		return contentHeight;
	}

	public int getContentWidth()
	{
		return contentWidth;
	}

	public void moveBy( int right, int down )
	{
		AbsolutePanel parent = ( AbsolutePanel ) getParent();
		Location location = new WidgetLocation( this, parent );
		int left = location.getLeft() + right;
		int top = location.getTop() + down;
		parent.setWidgetPosition( this, left, top );
	}
		
	private Panel setupCell( int row, int col, DirectionConstant direction, boolean resize )
	{
		final FocusPanel widget = new FocusPanel();
		widget.setPixelSize( BORDER_THICKNESS, BORDER_THICKNESS );
		mainPanel.setWidget( row, col, widget );
		if ( resize )
		{
			resizeDragController.makeDraggable( widget, direction );
			widget.addStyleName( "Resize-" + direction.directionLetters );
		}
		return widget;
	}
	
	private void removeResizeHandle( Widget w, DirectionConstant direction )
	{
		resizeDragController.makeNotDraggable( w);
		w.removeStyleName( "Resize-" + direction.directionLetters );
	}
		
	public void setContentSize( int width, int height )
	{
		if ( width != contentWidth )
		{
			contentWidth = width;			
		}
		if ( height != contentHeight )
		{
			contentHeight = height;
//			int headerHeight = title.getOffsetHeight();
//			eastWidget.setPixelSize( BORDER_THICKNESS, contentHeight /*+ headerHeight*/ );
		}
		mainPanel.setPixelSize( contentWidth, contentHeight );
	}

	@Override
	protected void onLoad()
	{
		super.onLoad();
		if ( !initialLoad && mainPanel.getOffsetHeight() != 0 )
		{
			initialLoad = true;
			setContentSize( mainPanel.getOffsetWidth(), mainPanel.getOffsetHeight() );
		}
	}
	
	public void makeNotResizable()
	{
		removeResizeHandle( eastWidget, EAST );
		removeResizeHandle( southWidget, SOUTH );
		removeResizeHandle( cornerWidget, SOUTH_EAST );
	}
	
	public void center() {

	    int left = (Window.getClientWidth() - getOffsetWidth()) >> 1;
	    int top = (Window.getClientHeight() - getOffsetHeight()) >> 1;
	    RootPanel.get().add( this, Math.max(Window.getScrollLeft() + left, 0), Math.max(Window.getScrollTop() + top, 0));
	    if ( !isVisible() )
		{
	    	setVisible(true);			
		}
	  }
	
	@Override
	public void setHeight( String height )
	{
		mainPanel.setHeight( height );
	}

	@Override
	public void setWidth( String width )
	{
		mainPanel.setWidth( width );
	}

	@Override
	public void setPixelSize( int width, int height )
	{
		mainPanel.setPixelSize( width, height );
	}

	@Override
	public void setSize( String width, String height )
	{
		mainPanel.setSize( width, height );
	}

	public void makeNotDraggable()
	{
		moveDragController.makeNotDraggable( this );
	}
	
	
}


