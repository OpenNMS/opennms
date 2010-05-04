package org.opennms.features.poller.remote.gwt.client.events;


import org.opennms.features.poller.remote.gwt.client.GWTMarkerState;

import com.google.gwt.event.shared.GwtEvent;

public class GWTMarkerClickedEvent extends GwtEvent<GWTMarkerClickedEventHandler> {
    
    public final static Type<GWTMarkerClickedEventHandler> TYPE = new Type<GWTMarkerClickedEventHandler>();
    
    private GWTMarkerState m_marker;
    
    public GWTMarkerClickedEvent(GWTMarkerState marker) {
        setMarker(marker);
    }

    @Override
    protected void dispatch(GWTMarkerClickedEventHandler handler) {
        handler.onGWTMarkerClicked(this);
        
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<GWTMarkerClickedEventHandler> getAssociatedType() {
        return TYPE;
    }

    private void setMarker(GWTMarkerState marker) {
        m_marker = marker;
    }

    public GWTMarkerState getMarkerState() {
        return m_marker;
    }

}
