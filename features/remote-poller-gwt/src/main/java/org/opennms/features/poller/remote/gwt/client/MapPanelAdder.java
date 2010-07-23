package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.InitializationCommand.DataLoader;

final class MapPanelAdder extends InitializationCommand.DataLoader {
    /**
     * 
     */
    private final DefaultLocationManager m_defaultLocationManager;

    /**
     * @param defaultLocationManager
     */
    MapPanelAdder(DefaultLocationManager defaultLocationManager) {
        m_defaultLocationManager = defaultLocationManager;
    }

    @Override
    public void onLoaded() {
        // Append the map panel to the main SplitPanel
        m_defaultLocationManager.getPanel().add(m_defaultLocationManager.m_mapPanel.getWidget());
    }
}