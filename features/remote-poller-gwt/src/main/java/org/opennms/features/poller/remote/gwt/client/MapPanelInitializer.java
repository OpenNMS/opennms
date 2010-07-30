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
        getApplication().m_view.getSplitPanel().setWidgetMinSize(getApplication().m_view.getLocationPanel(), 255);
        getApplication().m_view.getMainPanel().setSize("100%", "100%");
        RootPanel.get("remotePollerMap").add(getApplication().m_view.getMainPanel());
        getApplication().m_view.getMainPanel().setSize("100%", getApplication().getAppHeight().toString());
        getApplication().m_view.getMainPanel().forceLayout();
        getApplication().m_view.onLocationClick(null);
    }

    public Application getApplication() {
        return m_application;
    }
}