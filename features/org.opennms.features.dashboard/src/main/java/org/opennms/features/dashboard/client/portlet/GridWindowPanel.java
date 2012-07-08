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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * author: Tharindu Munasinghe (tharindumunasinghe@gmail.com)
 * org.opennms.features.dashboard
 */
@Deprecated
public class GridWindowPanel extends FlowPanel
{
	private static GridWindowPanel focusItem;
	
	private Widget visItem;
	private FocusPanel header;
	private Label captionLabel;
	private SimplePanel body;
	private Widget widget;

	public GridWindowPanel(final Widget visItem)
	{
		this.visItem=visItem;
		header = new FocusPanel();
		header.setHeight( "20px" );
		header.setStyleName( "demo-gridview-window-heading" );
		add( header );
		header.addClickHandler( new ClickHandler() {
			
			public void onClick(ClickEvent arg0) {
//				visItem.getPortlet().toFront();
				selectItem();
			}
		});
		
		header.addDoubleClickHandler( new  DoubleClickHandler() {
			
			public void onDoubleClick(DoubleClickEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		captionLabel=new Label();
		captionLabel.setStyleName( "demo-gridview-windowHeading-text" );
		header.add( captionLabel );
		
		body = new SimplePanel();
		setSize( "auto", "auto" );
		add( body );
	}

	public void setWidget( )
	{
//		this.widget=visItem.getPortlet().getContentWidget();
		body.clear();
		body.add( widget );
//		if ( visItem.getChart()!=null )
//		{
//			body.setPixelSize( visItem.getChart().getWidth(), visItem.getChart().getHeight() );
//		}else {
//			body.setPixelSize( VisualizationItemPresenter.DEFAULT_CHART_WIDTH, VisualizationItemPresenter.DEFAULT_CHART_HEIGHT );
//		}
	}
	
	public void selectItem()
	{
		if ( focusItem!=this )
		{
			if ( focusItem != null )
			{
				focusItem.removeStyleName( "demo-gridview-widget-focus" );
				focusItem.setStyleName( "demo-gridview-widget" );
			}
			focusItem = this;
			focusItem.removeStyleName( "demo-gridview-widget" );
			focusItem.setStyleName( "demo-gridview-widget-focus" );
		}
	}

	public FocusPanel getHeader()
	{
		return header;
	}

	public Widget getWidget()
	{
		return widget;
	}	
	
	public void setHeaderCaption(String text)
	{
		captionLabel.setText( text );
	}
}
