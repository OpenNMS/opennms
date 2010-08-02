package org.opennms.features.poller.remote.gwt.client;


import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEventHandler;

import com.google.gwt.event.shared.HandlerManager;

import de.novanic.eventservice.client.event.RemoteEventService;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class Application implements LocationsUpdatedEventHandler {

    private LocationManager m_locationManager;
    private final HandlerManager m_eventBus;

    DefaultApplicationView m_view;

    

    public Application(HandlerManager eventBus) {
        m_eventBus = eventBus;
    }

    public void initialize(DefaultApplicationView view, LocationStatusServiceAsync remoteService, RemoteEventService remoteEventService) {
        // Register for all relevant events thrown by the UI components
        getEventBus().addHandler(LocationsUpdatedEvent.TYPE, this);
        
        // Log.setUncaughtExceptionHandler();
        m_view = view;
        
        m_locationManager = new DefaultLocationManager(getEventBus(), m_view, remoteService, remoteEventService);
        
        m_view.initialize();
    }

    public void onApplicationViewSelected() {
        m_locationManager.applicationClicked();
    }
    
    public void onLocationViewSelected() {
        m_locationManager.locationClicked();
    }

    /** {@inheritDoc} */
    public void onLocationsUpdated(LocationsUpdatedEvent e) {
        m_view.updateTimestamp();
    }
    
    private HandlerManager getEventBus() {
        return m_eventBus;
    }
}
