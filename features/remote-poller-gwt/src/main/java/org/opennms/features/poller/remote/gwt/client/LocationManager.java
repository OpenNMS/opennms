package org.opennms.features.poller.remote.gwt.client;

import java.util.List;
import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEventHander;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;


/**
 * This interface specifies the model functions that allow data access to the
 * set of known {@link Location} objects that have been transmitted from the
 * server to the GWT client code.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface LocationManager {
	/**
	 * <p>initialize</p>
	 */
	public void initialize();
	/**
	 * <p>createOrUpdateLocation</p>
	 *
	 * @param info a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
	 */
	public void createOrUpdateLocation(final LocationInfo info);
	/**
	 * <p>createOrUpdateApplication</p>
	 *
	 * @param info a {@link org.opennms.features.poller.remote.gwt.client.ApplicationInfo} object.
	 */
	public void createOrUpdateApplication(final ApplicationInfo info);
	/**
	 * <p>removeApplication</p>
	 *
	 * @param applicationName a {@link java.lang.String} object.
	 */
	public void removeApplication(final String applicationName);
	/**
	 * <p>getLocation</p>
	 *
	 * @param locationName a {@link java.lang.String} object.
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
	 */
	public LocationInfo getLocation(String locationName);
	/**
	 * <p>getAllLocationNames</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<String> getAllLocationNames();
	/**
	 * <p>getVisibleLocations</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<LocationInfo> getVisibleLocations();
	/**
	 * <p>getApplicationInfo</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationInfo} object.
	 */
	public ApplicationInfo getApplicationInfo(String name);
	/**
	 * <p>getAllApplicationNames</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<String> getAllApplicationNames();
	/**
	 * <p>getTagsOnVisibleLocations</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<String> getTagsOnVisibleLocations();
	/**
	 * <p>addLocationManagerInitializationCompleteEventHandler</p>
	 *
	 * @param handler a {@link org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEventHander} object.
	 */
	public void addLocationManagerInitializationCompleteEventHandler(LocationManagerInitializationCompleteEventHander handler);
	/**
	 * <p>locationClicked</p>
	 */
	public void locationClicked();
	/**
	 * <p>applicationClicked</p>
	 */
	public void applicationClicked();
}
