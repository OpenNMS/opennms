package org.opennms.features.poller.remote.gwt.client;

import java.util.List;

public interface LocationManager {
	public void updateLocation(Location location);
	public void removeLocation(Location location);

	public void updateLocations(List<Location> location);
	public void removeLocations(List<Location> location);

	public void updateComplete();

	public Location getLocation(int index);
	public List<Location> getAllLocations();
	public List<Location> getLocations(int startIndex, int maxRows);
	public List<Location> getVisibleLocations();
	public void selectLocation(String locationName);
	public void fitToMap();

	public void reportError(String string, Throwable t);
}
