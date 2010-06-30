package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * <p>LocationStatusService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
@RemoteServiceRelativePath("locationStatus")
public interface LocationStatusService extends RemoteService {
	/**
	 * <p>start</p>
	 */
	void start();
	/**
	 * <p>getLocationInfo</p>
	 *
	 * @param locationName a {@link java.lang.String} object.
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
	 */
	public LocationInfo getLocationInfo(final String locationName);
	/**
	 * <p>getLocationDetails</p>
	 *
	 * @param locationName a {@link java.lang.String} object.
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationDetails} object.
	 */
	public LocationDetails getLocationDetails(final String locationName);
	/**
	 * <p>getApplicationInfo</p>
	 *
	 * @param applicationName a {@link java.lang.String} object.
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationInfo} object.
	 */
	public ApplicationInfo getApplicationInfo(final String applicationName);
	/**
	 * <p>getApplicationDetails</p>
	 *
	 * @param applicationName a {@link java.lang.String} object.
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationDetails} object.
	 */
	public ApplicationDetails getApplicationDetails(final String applicationName);
}
