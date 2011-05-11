package org.opennms.isoc.ipv6.gui.client;

import com.google.gwt.event.shared.EventHandler;

public interface HostUpdateEventHandler extends EventHandler {
    void onHostUpdate(HostUpdateEvent event);
}
