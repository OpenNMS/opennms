package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * <p>LocationManagerInitializationCompleteEventHander interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface LocationManagerInitializationCompleteEventHander extends EventHandler {
    /**
     * <p>onInitializationComplete</p>
     *
     * @param event a {@link org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEvent} object.
     */
    public void onInitializationComplete(LocationManagerInitializationCompleteEvent event);
}
