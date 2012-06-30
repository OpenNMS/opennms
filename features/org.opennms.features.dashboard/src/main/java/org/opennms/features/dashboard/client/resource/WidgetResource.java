package org.opennms.features.dashboard.client.resource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.resources.client.ImageResource;

public interface WidgetResource extends ClientBundle, ClientBundleWithLookup
{
	WidgetResource WIDGETRESOURCE = GWT.create( WidgetResource.class);
	
	@Source("style.css")
	@NotStrict
	WidgetCssResource widgetsCssResource();

	@Source("images/resize_corner.png")
	ImageResource resizeCorner();
	
	@Source("images/drop_small.png")
	ImageResource dropSmall();


}
