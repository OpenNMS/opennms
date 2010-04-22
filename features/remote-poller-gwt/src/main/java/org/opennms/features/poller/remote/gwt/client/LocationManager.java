package org.opennms.features.poller.remote.gwt.client;

import java.util.List;
import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEventHander;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;


/**
 * This interface specifies the model functions that allow data access to the
 * set of known {@link Location} objects that have been transmitted from the
 * server to the GWT client code.
 */
public interface LocationManager {
	public void initialize();
	public Location createOrUpdateLocation(final LocationInfo info);
	public void createOrUpdateApplication(final ApplicationInfo info);
	public Location getLocation(String locationName);
	// public List<Location> getAllLocations();
	public Set<String> getAllLocationNames();
	public List<Location> getVisibleLocations();
	public Set<String> getAllApplicationNames();
	public List<ApplicationInfo> getVisibleApplications();
	public void addLocationManagerInitializationCompleteEventHandler(LocationManagerInitializationCompleteEventHander handler);
}
