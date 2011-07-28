package org.opennms.features.node.list.gwt.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface PhysicalInterfaceSelectionHandler extends EventHandler {
    void onPhysicalInterfaceSelected(PhysicalInterfaceSelectionEvent event);
}
