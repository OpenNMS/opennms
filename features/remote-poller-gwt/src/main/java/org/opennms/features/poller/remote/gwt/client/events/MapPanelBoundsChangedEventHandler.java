package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * <p>MapPanelBoundsChangedEventHandler interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface MapPanelBoundsChangedEventHandler extends EventHandler {
    /**
     * <p>onBoundsChanged</p>
     *
     * @param event a {@link org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEvent} object.
     */
    public void onBoundsChanged(MapPanelBoundsChangedEvent event);
}
