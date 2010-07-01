package org.opennms.features.poller.remote.gwt.client.events;

import org.opennms.features.poller.remote.gwt.client.GWTMarkerState;

import com.google.gwt.event.shared.GwtEvent;

public class GWTMarkerInfoWindowRefreshEvent extends GwtEvent<GWTMarkerInfoWindowRefreshHandler> {
    
public final static Type<GWTMarkerInfoWindowRefreshHandler> TYPE = new Type<GWTMarkerInfoWindowRefreshHandler>();
    
    private GWTMarkerState m_marker;
    
    public GWTMarkerInfoWindowRefreshEvent(GWTMarkerState markerState) {
        setMarkerState(markerState);
    }
    
    @Override
    protected void dispatch(GWTMarkerInfoWindowRefreshHandler handler) {
        handler.onGWTMarkerInfoWindowRefresh(this);
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<GWTMarkerInfoWindowRefreshHandler> getAssociatedType() {
        return TYPE;
    }

    public void setMarkerState(GWTMarkerState m_marker) {
        this.m_marker = m_marker;
    }

    public GWTMarkerState getMarkerState() {
        return m_marker;
    }

}
