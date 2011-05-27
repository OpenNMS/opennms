package org.opennms.ipv6.summary.gui.client;

import com.google.gwt.event.shared.EventHandler;

public interface HostUpdateEventHandler extends EventHandler {
    void onHostUpdate(HostUpdateEvent event);
}
