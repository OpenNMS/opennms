package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * <p>ApplicationDetailsRetrievedEventHandler interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface ApplicationDetailsRetrievedEventHandler extends EventHandler {
    /**
     * <p>onApplicationDetailsRetrieved</p>
     *
     * @param event a {@link org.opennms.features.poller.remote.gwt.client.events.ApplicationDetailsRetrievedEvent} object.
     */
    public void onApplicationDetailsRetrieved(ApplicationDetailsRetrievedEvent event);
}
