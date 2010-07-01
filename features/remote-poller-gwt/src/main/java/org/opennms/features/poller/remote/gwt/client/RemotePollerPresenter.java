package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.FilterPanel.FiltersChangedEventHandler;
import org.opennms.features.poller.remote.gwt.client.FilterPanel.StatusSelectionChangedEventHandler;
import org.opennms.features.poller.remote.gwt.client.TagPanel.TagClearedEventHandler;
import org.opennms.features.poller.remote.gwt.client.TagPanel.TagSelectedEventHandler;
import org.opennms.features.poller.remote.gwt.client.events.ApplicationDeselectedEventHandler;
import org.opennms.features.poller.remote.gwt.client.events.ApplicationSelectedEventHandler;
import org.opennms.features.poller.remote.gwt.client.events.GWTMarkerClickedEventHandler;
import org.opennms.features.poller.remote.gwt.client.events.GWTMarkerInfoWindowRefreshHandler;
import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEventHandler;
import org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEventHandler;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;


/** 
 * This interface represents the controller methods that control the user interface. 
 * It extends several event handlers that the controller logic is expected to respond to.
 */
public interface RemotePollerPresenter extends MapPanelBoundsChangedEventHandler,
		LocationPanelSelectEventHandler,
		FiltersChangedEventHandler,
		TagSelectedEventHandler,
		TagClearedEventHandler,
		StatusSelectionChangedEventHandler,
		GWTMarkerClickedEventHandler,
		GWTMarkerInfoWindowRefreshHandler,
		ApplicationDeselectedEventHandler,
		ApplicationSelectedEventHandler
{
    public static final Domain LOCATION_EVENT_DOMAIN = DomainFactory.getDomain("location_event");

    public void fitMapToLocations();

    public void reportError(final String string, final Throwable error);

    /**
     * This action is used to respond to server-side events when a location is updated.
     */
    public void updateLocation(final LocationInfo locationInfo);
    public void removeApplication(final String applicationName);

    public void updateApplication(final ApplicationInfo applicationInfo);

    /**
     * This action is used to respond to server-side events sent when all initial location updates are complete.
     */
    public void updateComplete();
}
