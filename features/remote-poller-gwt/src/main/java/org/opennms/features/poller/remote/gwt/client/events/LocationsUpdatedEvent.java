package org.opennms.features.poller.remote.gwt.client.events;

import org.opennms.features.poller.remote.gwt.client.LocationManager;

import com.google.gwt.event.shared.GwtEvent;

public class LocationsUpdatedEvent extends GwtEvent<LocationsUpdatedEventHandler> {
    
    public static Type<LocationsUpdatedEventHandler> TYPE = new Type<LocationsUpdatedEventHandler>();
    private LocationManager m_locationManager;
    
    public LocationsUpdatedEvent(final LocationManager locationManager) {
        setLocationManager(locationManager);
    }
    
	@Override
	protected void dispatch(LocationsUpdatedEventHandler handler) {
		handler.onLocationsUpdated(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<LocationsUpdatedEventHandler> getAssociatedType() {
		return TYPE;
	}

    public static com.google.gwt.event.shared.GwtEvent.Type<LocationsUpdatedEventHandler> getType(){
        return TYPE;
    }

    public void setLocationManager(final LocationManager locationManager) {
        m_locationManager = locationManager;
    }

    public LocationManager getLocationManager() {
        return m_locationManager;
    }

}
