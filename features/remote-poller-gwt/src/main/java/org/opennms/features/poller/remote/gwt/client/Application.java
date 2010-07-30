package org.opennms.features.poller.remote.gwt.client;


import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEventHandler;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

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
        // Log.setUncaughtExceptionHandler();
        m_view = view;
        

        Window.setTitle("OpenNMS - Remote Monitor");
        Window.enableScrolling(false);
        Window.setMargin("0px");
        Window.addResizeHandler(new ResizeHandler() {
			public void onResize(final ResizeEvent event) {
				m_view.getMainPanel().setHeight(getAppHeight().toString());
			}
        });

        // Register for all relevant events thrown by the UI components
        getEventBus().addHandler(LocationsUpdatedEvent.TYPE, this);

        
        final DefaultLocationManager dlm = new DefaultLocationManager(getEventBus(), m_view.getSplitPanel(), m_view.getLocationPanel(), createMapPanel);
        m_locationManager = dlm;

        m_view.getLocationPanel().setEventBus(getEventBus());

        for (final Widget w : m_view.getStatusesPanel()) {
            if (w instanceof CheckBox) {
                final CheckBox cb = (CheckBox)w;
                final Status s = Status.valueOf(cb.getFormValue());
                dlm.onStatusSelectionChanged(s, cb.getValue());
            }
        }

        m_locationManager.initialize();
        
        m_view.getSplitPanel().setWidgetMinSize(m_view.getLocationPanel(), 255);
        m_view.getMainPanel().setSize("100%", "100%");
        RootPanel.get("remotePollerMap").add(m_view.getMainPanel());
        
        m_view.updateTimestamp();
        m_view.onLocationClick(null);
    }

    Integer getAppHeight() {
    	final com.google.gwt.user.client.Element e = m_view.getMainPanel().getElement();
		int extraHeight = e.getAbsoluteTop();
		return Window.getClientHeight() - extraHeight;
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
