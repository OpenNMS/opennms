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
import org.opennms.features.poller.remote.gwt.client.remoteevents.MapRemoteEventHandler;



/**
 * This interface represents the controller methods that control the user interface.
 * It extends several event handlers that the controller logic is expected to respond to.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
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
		ApplicationSelectedEventHandler, MapRemoteEventHandler
{
    
    
}
