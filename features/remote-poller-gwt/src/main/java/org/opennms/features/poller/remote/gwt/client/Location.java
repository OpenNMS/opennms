package org.opennms.features.poller.remote.gwt.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import de.novanic.eventservice.client.event.Event;

public interface Location extends Event, IsSerializable {

	public abstract String getName();

	public abstract String getPollingPackageName();

	public abstract String getArea();

	public abstract String getGeolocation();

	public abstract GWTLatLng getLatLng();

	public abstract LocationMonitorState getLocationMonitorState();

	public abstract String getImageURL();

}