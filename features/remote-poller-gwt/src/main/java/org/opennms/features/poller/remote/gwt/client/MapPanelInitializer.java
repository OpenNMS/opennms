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
        getApplication().updateTimestamp();
        getApplication().splitPanel.setWidgetMinSize(getApplication().locationPanel, 255);
        getApplication().mainPanel.setSize("100%", "100%");
        RootPanel.get("remotePollerMap").add(getApplication().mainPanel);
        getApplication().mainPanel.setSize("100%", getApplication().getAppHeight().toString());
        getApplication().mainPanel.forceLayout();
        getApplication().onLocationClick(null);
    }

    public Application getApplication() {
        return m_application;
    }
}