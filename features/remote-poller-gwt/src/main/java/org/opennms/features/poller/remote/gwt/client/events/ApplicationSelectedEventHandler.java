package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * <p>ApplicationSelectedEventHandler interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface ApplicationSelectedEventHandler extends EventHandler {
    /**
     * <p>onApplicationSelected</p>
     *
     * @param event a {@link org.opennms.features.poller.remote.gwt.client.events.ApplicationSelectedEvent} object.
     */
    public void onApplicationSelected(ApplicationSelectedEvent event);
}
