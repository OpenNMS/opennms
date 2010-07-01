package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * <p>GWTMarkerClickedEventHandler interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface GWTMarkerClickedEventHandler extends EventHandler {
    
    /**
     * <p>onGWTMarkerClicked</p>
     *
     * @param event a {@link org.opennms.features.poller.remote.gwt.client.events.GWTMarkerClickedEvent} object.
     */
    public void onGWTMarkerClicked(GWTMarkerClickedEvent event);
}
