package org.opennms.features.poller.remote.gwt.client;

import org.junit.Test;

import com.google.gwt.event.shared.HandlerManager;


public class LocationAddedToMapTest {
    
    
    @Test
    public void testAddLocation() {
        
        HandlerManager eventBus = new HandlerManager(null);
        Application application = new Application(eventBus);
        application.initialize(new ApplicationView(application, eventBus));
        
    }
}
