package org.opennms.features.poller.remote.gwt.client;

import com.google.gwt.user.client.ui.Widget;

public interface MapPanel {
    
    public Widget getWidget();

    public void showLocationDetails(String name, String htmlTitle, String htmlContent);

    public GWTBounds getBounds();

    public void setBounds(GWTBounds locationBounds);

    public void placeMarker(GWTMarker marker);
    
}
