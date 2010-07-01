package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * <p>ApplicationDeselectedEventHandler interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface ApplicationDeselectedEventHandler extends EventHandler {
    /**
     * <p>onApplicationDeselected</p>
     *
     * @param event a {@link org.opennms.features.poller.remote.gwt.client.events.ApplicationDeselectedEvent} object.
     */
    public void onApplicationDeselected(ApplicationDeselectedEvent event);
}
