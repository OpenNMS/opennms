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
