package org.opennms.features.poller.remote.gwt.server;

import org.opennms.features.poller.remote.gwt.client.remoteevents.MapRemoteEvent;

public interface LocationEventHandler {

    void sendEvent(final MapRemoteEvent event);

}
