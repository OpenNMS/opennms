package org.opennms.isoc.ipv6.gui.client;

import com.google.gwt.event.shared.EventHandler;

public interface LocationUpdateEventHandler extends EventHandler {
    void onLocationUpdate(LocationUpdateEvent event);
}
