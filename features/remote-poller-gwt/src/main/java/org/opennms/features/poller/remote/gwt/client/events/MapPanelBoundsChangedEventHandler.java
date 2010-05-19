package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface MapPanelBoundsChangedEventHandler extends EventHandler {
    public void onBoundsChanged(MapPanelBoundsChangedEvent event);
}
