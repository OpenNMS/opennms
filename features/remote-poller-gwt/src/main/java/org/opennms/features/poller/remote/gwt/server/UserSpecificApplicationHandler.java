package org.opennms.features.poller.remote.gwt.server;

import java.util.Collection;

import org.opennms.features.poller.remote.gwt.client.remoteevents.MapRemoteEvent;

import de.novanic.eventservice.service.EventExecutorService;

public class UserSpecificApplicationHandler extends DefaultApplicationHandler {

    public UserSpecificApplicationHandler() {super();}

    public UserSpecificApplicationHandler(final LocationDataService locationDataService, final EventExecutorService eventService, final boolean includeStatus) {
        super(locationDataService, eventService, includeStatus);
    }

    public UserSpecificApplicationHandler(final LocationDataService locationDataService, final EventExecutorService eventService, final boolean includeStatus, final Collection<String> currentApplications) {
        super(locationDataService, eventService, includeStatus, currentApplications);
    }

    protected void sendEvent(final MapRemoteEvent event) {
        getEventService().addEventUserSpecific(event);
    }

}
