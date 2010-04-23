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
	public void createOrUpdateLocation(final LocationInfo info);
	public void createOrUpdateApplication(final ApplicationInfo info);
	public LocationInfo getLocation(String locationName);
	public Set<String> getAllLocationNames();
	public List<LocationInfo> getVisibleLocations();
	public ApplicationInfo getApplicationInfo(String name);
	public Set<String> getAllApplicationNames();
	public List<ApplicationInfo> getVisibleApplications();
	public List<String> getTagsOnVisibleLocations();
	public void addLocationManagerInitializationCompleteEventHandler(LocationManagerInitializationCompleteEventHander handler);
}
