package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface ApplicationDetailsRetrievedEventHandler extends EventHandler {
    public void onApplicationDetailsRetrieved(ApplicationDetailsRetrievedEvent event);
}
