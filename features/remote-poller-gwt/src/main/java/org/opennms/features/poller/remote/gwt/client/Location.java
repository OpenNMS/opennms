package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.user.client.rpc.IsSerializable;

import de.novanic.eventservice.client.event.Event;

/**
 * <p>Location interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface Location extends Event, IsSerializable, Comparable<Location> {

	/**
	 * <p>getImageURL</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public abstract String getImageURL();

	/**
	 * <p>getLocationInfo</p>
	 *
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
	 */
	public abstract LocationInfo getLocationInfo();

	/**
	 * <p>getLocationDetails</p>
	 *
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationDetails} object.
	 */
	public abstract LocationDetails getLocationDetails();
}
