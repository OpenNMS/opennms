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

package org.opennms.features.dashboard.client;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.dashboard.client.dnd.GridViewDragHandler;
import org.opennms.features.dashboard.client.layout.GridDesktopPanel;
import org.opennms.features.dashboard.client.layout.VerticalPanelWithSpacer;
import org.opennms.features.dashboard.client.portlet.GridWindowPanel;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.drop.HorizontalPanelDropController;
import com.allen_sauer.gwt.dnd.client.drop.VerticalPanelDropController;

/**
 * author: Tharindu Munasinghe (tharindumunasinghe@gmail.com)
 * org.opennms.features.dashboard
 */
public final class GridViewWidget
{
	private static final String CSS_GRID_PANEL = "demo-gridview";
	private static final String CSS_GRID_PANEL_COLUMN_COMPOSITE = "demo-gridview-column-composite";
	private static final String CSS_GRID_PANEL_CONTAINER = "demo-gridview-container";
	private static final String CSS_GRID_PANEL_HEADING = "demo-gridview-heading";
	private static final String CSS_GRID_PANEL_WIDGET = "demo-gridview-widget";
	
	private static final int COLUMNS = 3;
	private static final int SPACING = 1;

	private GridDesktopPanel boundaryPanel;
	private PickupDragController columnDragController;
	private PickupDragController widgetDragController;
	private HorizontalPanel bodyPanel;
	private HorizontalPanelDropController columnDropController;
	private List<VerticalPanel> columnList;
	private List<GridWindowPanel> windowList;

	public GridViewWidget( GridDesktopPanel gridPanel )
	{
		this( new GridViewDragHandler(), gridPanel );
	}
	
	public GridViewWidget( GridViewDragHandler demoDragHandler, GridDesktopPanel gridPanel )
	{		
		// use the boundary panel as this composite's widget
		boundaryPanel = gridPanel;
		boundaryPanel.setSize( "100%", "100%" );
		
		boundaryPanel.setStyleName( CSS_GRID_PANEL );

		// initialize our column drag controller
		columnDragController = new PickupDragController( boundaryPanel, false );
		columnDragController.setBehaviorMultipleSelection( false );
		columnDragController.addDragHandler( demoDragHandler );

		// initialize our widget drag controller
		widgetDragController = new PickupDragController( boundaryPanel, false );
		widgetDragController.setBehaviorMultipleSelection( false );
		widgetDragController.addDragHandler( demoDragHandler );

		// initialize horizontal panel to hold our columns
		bodyPanel = new HorizontalPanel();
		bodyPanel.setStyleName( CSS_GRID_PANEL_CONTAINER );
		bodyPanel.setSpacing( SPACING );
		boundaryPanel.add( bodyPanel );

		// initialize our column drop controller
		columnDropController = new HorizontalPanelDropController( bodyPanel );
		columnDragController.registerDropController( columnDropController );

		columnList=new ArrayList<VerticalPanel>();
		windowList=new ArrayList<GridWindowPanel>();
		init();
	}
	
	public void init()
	{
		for ( int col = 0; col < COLUMNS; col++ )
		{
			addNewColumn();
		}
	}

	public VerticalPanel  addNewColumn()
	{
		// initialize a vertical panel to hold the heading and a second vertical
		// panel
		VerticalPanel columnCompositePanel = new VerticalPanel();
		columnCompositePanel.setStyleName( CSS_GRID_PANEL_COLUMN_COMPOSITE );
		bodyPanel.add( columnCompositePanel );

		VerticalPanel verticalPanel = new VerticalPanelWithSpacer();
		verticalPanel.setStyleName( CSS_GRID_PANEL_CONTAINER );
		verticalPanel.setSpacing( SPACING );
		columnList.add( verticalPanel );

		// initialize a widget drop controller for the current column
		VerticalPanelDropController widgetDropController = new VerticalPanelDropController( verticalPanel );
		widgetDragController.registerDropController( widgetDropController );

		// Put together the column pieces
		Label heading = new Label( );
		heading.setStyleName( CSS_GRID_PANEL_HEADING );
		columnCompositePanel.add( heading );
		columnCompositePanel.add( verticalPanel );

		// make the column draggable by its heading
		columnDragController.makeDraggable( columnCompositePanel, heading );
		
		return verticalPanel;
	}
	
	public int addNewWidget(Widget visItem)
	{
		if ( !columnList.isEmpty() )
		{
			int index=windowList.size()%columnList.size();
			return addNewWidget( index, visItem );
		}else {
			addNewColumn();
			return addNewWidget( columnList.size() - 1, visItem );
		}
	}
	
	public int addNewWidget(int column, Widget visItem)
	{
		if ( columnList.size()>column )
		{
			VerticalPanel verticalPanel = columnList.get( column );
			
			GridWindowPanel widget = new GridWindowPanel(visItem);
			widget.setWidget( );
			widget.setStyleName( CSS_GRID_PANEL_WIDGET );
			verticalPanel.add( widget );
			windowList.add( widget );
			// make the widget draggable
			widgetDragController.makeDraggable( widget, widget.getHeader() );
			return windowList.size()-1;
		}
		return -1;
	}
	
	public void setContent( int index ){
		if ( index>=0 && index<windowList.size() )
		{
			windowList.get( index ).setWidget( );
		}
	}
	
	public void closeWindow(int window)
	{
		if ( window>=0 && window<windowList.size() )
		{
			windowList.get( window ).removeFromParent();
			windowList.remove( window );
		}
	}
	
	public void setHeaderCaption(int window, String text)
	{
		if ( window>=0 && window<windowList.size() )
		{
			windowList.get( window ).setHeaderCaption( text );
		}
	}
	
	public void selectWindow(int window)
	{
		if ( window>=0 && window<windowList.size() )
		{
			windowList.get( window ).selectItem();
		}
	}
	
	public void clearGrid()
	{
		for ( GridWindowPanel window : windowList )
		{
			widgetDragController.makeNotDraggable( window );
			window.removeFromParent();
		}
		windowList.clear();
	}
}