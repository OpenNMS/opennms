package org.opennms.features.poller.remote.gwt.client;


import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEventHandler;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class Application implements LocationsUpdatedEventHandler {
    

    static final DateTimeFormat UPDATE_TIMESTAMP_FORMAT = DateTimeFormat.getMediumDateTimeFormat();

    

    private LocationManager m_locationManager;
    private final HandlerManager m_eventBus;

    ApplicationView m_view;

    

    public Application(HandlerManager eventBus) {
        m_eventBus = eventBus;
    }

    public void initialize(ApplicationView view, MapPanel createMapPanel) {
        // Register for all relevant events thrown by the UI components
        getEventBus().addHandler(LocationsUpdatedEvent.TYPE, this);
        
        // Log.setUncaughtExceptionHandler();
        m_view = view;
        
        m_locationManager = new DefaultLocationManager(getEventBus(), m_view.getSplitPanel(), m_view.getLocationPanel(), createMapPanel);

        m_locationManager.initialize(m_view.getSelectedStatuses());
        
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
