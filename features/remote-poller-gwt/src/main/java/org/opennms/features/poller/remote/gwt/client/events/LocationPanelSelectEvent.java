package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class LocationPanelSelectEvent extends GwtEvent<LocationPanelSelectEventHandler> {
    
    public static Type<LocationPanelSelectEventHandler> TYPE = new Type<LocationPanelSelectEventHandler>();
    private String m_locationName;
    
    public LocationPanelSelectEvent(String locationName) {
        m_locationName = locationName;
    }
    
    @Override
    protected void dispatch(LocationPanelSelectEventHandler handler) {
        handler.onLocationSelected(this);
        
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<LocationPanelSelectEventHandler> getAssociatedType() {
        return TYPE;
    }


    public String getLocationName() {
        return m_locationName;
    }
}
