package org.opennms.features.poller.remote.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.shared.HandlerManager;

public class Main implements EntryPoint {
    
    
    public void onModuleLoad() {
        HandlerManager eventBus = new HandlerManager(null);
        Application application = new Application(eventBus);
        application.initialize(new ApplicationView(eventBus));

    }

}
