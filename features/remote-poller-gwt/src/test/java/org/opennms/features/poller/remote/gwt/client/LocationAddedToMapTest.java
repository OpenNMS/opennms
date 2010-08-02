package org.opennms.features.poller.remote.gwt.client;

import org.junit.Test;

import com.google.gwt.event.shared.HandlerManager;

import de.novanic.eventservice.client.event.RemoteEventService;


public class LocationAddedToMapTest {
    
    
    @Test
    public void testAddLocation() {
        
        HandlerManager eventBus = new HandlerManager(null);
        Application application = new Application(eventBus);
        MapPanel mapPanel = createMockMapPanel();
        application.initialize(createMockApplicationView(eventBus, application, mapPanel), createMockRemoteService(), createMockRemoteEventService());
        
    }

    private DefaultApplicationView createMockApplicationView( HandlerManager eventBus, Application application, MapPanel mapPanel) {
        return new DefaultApplicationView(application, eventBus, mapPanel);
    }

    private MapPanel createMockMapPanel() {
        return null;
    }

    private RemoteEventService createMockRemoteEventService() {
        return null;
    }

    private LocationStatusServiceAsync createMockRemoteService() {
        return null;
    }
}
