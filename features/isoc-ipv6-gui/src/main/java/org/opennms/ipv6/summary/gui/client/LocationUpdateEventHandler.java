package org.opennms.ipv6.summary.gui.client;

import com.google.gwt.event.shared.EventHandler;

public interface LocationUpdateEventHandler extends EventHandler {
    void onLocationUpdate(LocationUpdateEvent event);
}
