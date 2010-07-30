package org.opennms.features.poller.remote.gwt.client;


import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEventHandler;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

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
        
        Window.setTitle("OpenNMS - Remote Monitor");
        Window.enableScrolling(false);
        Window.setMargin("0px");
        Window.addResizeHandler(new ResizeHandler() {
			public void onResize(final ResizeEvent event) {
				m_view.getMainPanel().setHeight(m_view.getAppHeight().toString());
			}
        });
        
        final DefaultLocationManager dlm = new DefaultLocationManager(getEventBus(), m_view.getSplitPanel(), m_view.getLocationPanel(), createMapPanel);
        m_locationManager = dlm;

        m_view.getLocationPanel().setEventBus(getEventBus());
        Set<Status> statuses = m_view.getSelectedStatuses();
        
        for (Status s : statuses) {
            dlm.onStatusSelectionChanged(s, true);
        }

        m_locationManager.initialize();
        
        m_view.getSplitPanel().setWidgetMinSize(m_view.getLocationPanel(), 255);
        m_view.getMainPanel().setSize("100%", "100%");
        RootPanel.get("remotePollerMap").add(m_view.getMainPanel());
        
        m_view.updateTimestamp();
        m_view.onLocationClick(null);
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
