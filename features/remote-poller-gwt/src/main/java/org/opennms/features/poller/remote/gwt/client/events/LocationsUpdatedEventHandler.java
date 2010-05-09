package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.EventHandler;


public interface LocationsUpdatedEventHandler extends EventHandler {
		public void onLocationsUpdated(LocationsUpdatedEvent e);
}
