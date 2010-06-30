package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.EventHandler;


/**
 * <p>LocationsUpdatedEventHandler interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface LocationsUpdatedEventHandler extends EventHandler {
		/**
		 * <p>onLocationsUpdated</p>
		 *
		 * @param e a {@link org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent} object.
		 */
		public void onLocationsUpdated(LocationsUpdatedEvent e);
}
