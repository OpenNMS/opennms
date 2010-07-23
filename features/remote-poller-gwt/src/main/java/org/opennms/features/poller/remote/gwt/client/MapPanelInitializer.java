package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationManagerInitializationCompleteEventHander;

import com.google.gwt.user.client.ui.RootPanel;

final class MapPanelInitializer implements
        LocationManagerInitializationCompleteEventHander {
    /**
     * 
     */
    private final Application m_application;

    /**
     * @param application
     */
    MapPanelInitializer(Application application) {
        m_application = application;
    }

    public void onInitializationComplete(LocationManagerInitializationCompleteEvent event) {
        m_application.updateTimestamp();
        m_application.splitPanel.setWidgetMinSize(m_application.locationPanel, 255);
        m_application.mainPanel.setSize("100%", "100%");
        RootPanel.get("remotePollerMap").add(m_application.mainPanel);
        m_application.mainPanel.setSize("100%", m_application.getAppHeight().toString());
        m_application.mainPanel.forceLayout();
        m_application.onLocationClick(null);
    }
}