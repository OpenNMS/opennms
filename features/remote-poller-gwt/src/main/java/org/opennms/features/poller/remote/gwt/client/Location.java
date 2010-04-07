package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.user.client.rpc.IsSerializable;

import de.novanic.eventservice.client.event.Event;

public interface Location extends Event, IsSerializable, Comparable<Location> {

	@Deprecated
	public abstract String getName();

	@Deprecated
	public abstract String getPollingPackageName();

	@Deprecated
	public abstract String getArea();

	@Deprecated
	public abstract String getGeolocation();

	@Deprecated
	public abstract GWTLatLng getLatLng();

	@Deprecated
	public abstract LocationMonitorState getLocationMonitorState();

	@Deprecated
	public abstract String getImageURL();
	
	public abstract LocationInfo getLocationInfo();

	public abstract LocationDetails getLocationDetails();
}