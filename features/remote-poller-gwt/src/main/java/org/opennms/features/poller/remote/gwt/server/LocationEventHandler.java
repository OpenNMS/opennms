package org.opennms.features.poller.remote.gwt.server;

import org.opennms.features.poller.remote.gwt.client.remoteevents.MapRemoteEvent;

/**
 * <p>LocationEventHandler interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface LocationEventHandler {

    /**
     * <p>sendEvent</p>
     *
     * @param event a {@link org.opennms.features.poller.remote.gwt.client.remoteevents.MapRemoteEvent} object.
     */
    void sendEvent(final MapRemoteEvent event);

}
