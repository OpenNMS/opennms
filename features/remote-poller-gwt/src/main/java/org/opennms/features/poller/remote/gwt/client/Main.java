package org.opennms.features.poller.remote.gwt.client;

import com.google.gwt.core.client.EntryPoint;

public class Main implements EntryPoint {
    
    
    public void onModuleLoad() {
        Application application = new Application();
        application.initialize(new ApplicationView());

    }

}
