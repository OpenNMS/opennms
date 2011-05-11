package org.opennms.isoc.ipv6.gui.client;

import com.google.gwt.event.shared.GwtEvent;

public class LocationUpdateEvent extends GwtEvent<LocationUpdateEventHandler> {
    
    public static Type<LocationUpdateEventHandler> TYPE = new Type<LocationUpdateEventHandler>();
    
    private final String m_location;
    
    public LocationUpdateEvent(String location) {
        m_location = location;
    }
    
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<LocationUpdateEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(LocationUpdateEventHandler handler) {
        handler.onLocationUpdate(this);
    }
    
    public String getLocation() {
        return m_location;
    }

}
