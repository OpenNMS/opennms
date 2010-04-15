package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class LocationsUpdatedEvent extends GwtEvent<LocationsUpdatedEventHandler> {
    
    public static Type<LocationsUpdatedEventHandler> TYPE = new Type<LocationsUpdatedEventHandler>();
    private String m_eventString = "You have got the event String";
    
    public LocationsUpdatedEvent() {
    }
    
	@Override
	protected void dispatch(LocationsUpdatedEventHandler handler) {
		handler.onLocationsUpdated(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<LocationsUpdatedEventHandler> getAssociatedType() {
		return TYPE;
	}

    public void setEventString(final String eventString) {
        m_eventString = eventString;
    }

    public String getEventString() {
        return m_eventString;
    }
}
