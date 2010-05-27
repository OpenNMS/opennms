package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface LocationManagerInitializationCompleteEventHander extends EventHandler {
    public void onInitializationComplete(LocationManagerInitializationCompleteEvent event);
}
