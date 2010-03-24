package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractLocationManager implements LocationManager {

	public enum State {
		UNINITIALIZED,
		APIKEY_LOADING,
		APIKEY_LOADED,
		MAP_API_LOADING,
		MAP_API_LOADED,
		MAP_API_CONFIGURED,
		MARKER_API_LOADING,
		MARKER_API_LOADED,
		ICON_API_LOADING,
		ICON_API_LOADED,
		EVENT_BACKEND_LOADING,
		EVENT_BACKEND_FAILED,
		EVENT_BACKEND_LOADED,
		FINISHED
	}

	public abstract void initialize();

	public abstract void removeLocations(Collection<Location> location);
	public abstract void updateLocations(Collection<Location> location);

	public abstract void updateComplete();

	public abstract Location getLocation(int index);
	public abstract List<Location> getAllLocations();
	public abstract List<Location> getLocations(int startIndex, int maxRows);
	public abstract List<Location> getVisibleLocations();
	public abstract void selectLocation(String locationName);
	public abstract void fitToMap();

	public void removeLocation(final Location location) {
		final List<Location> locationList = new ArrayList<Location>(1);
		locationList.add(location);
		removeLocations(locationList);
	}
	
	public void updateLocation(final Location location) {
		final List<Location> locationList = new ArrayList<Location>(1);
		locationList.add(location);
		updateLocations(locationList);
	}
	
	public abstract void reportError(String message, Throwable throwable);
}
