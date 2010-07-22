package org.opennms.features.poller.remote.gwt.client.remoteevents;

import java.util.Collection;

import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;

public interface MapRemoteEventHandler {

    /** Constant <code>LOCATION_EVENT_DOMAIN</code> */
    public static final Domain LOCATION_EVENT_DOMAIN = DomainFactory
            .getDomain("location_event");

    /**
     * This action is used to respond to server-side events when a location is updated.
     *
     * @param locationInfo a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
     */
    public abstract void updateLocation(final LocationInfo locationInfo);

    public abstract void updateLocations(Collection<LocationInfo> locations);

    /**
     * <p>removeApplication</p>
     *
     * @param applicationName a {@link java.lang.String} object.
     */
    public abstract void removeApplication(final String applicationName);

    /**
     * <p>updateApplication</p>
     *
     * @param applicationInfo a {@link org.opennms.features.poller.remote.gwt.client.ApplicationInfo} object.
     */
    public abstract void updateApplication(final ApplicationInfo applicationInfo);

    /**
     * This action is used to respond to server-side events sent when all initial location updates are complete.
     */
    public abstract void updateComplete();

}