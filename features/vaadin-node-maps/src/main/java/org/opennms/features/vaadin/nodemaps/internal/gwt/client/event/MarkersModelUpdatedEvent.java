package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search.UpdateEvent;

public class MarkersModelUpdatedEvent extends UpdateEvent {
    public static final String TYPE = "markersModelUpdated";

    protected MarkersModelUpdatedEvent() {
    }

    public static final MarkersModelUpdatedEvent createEvent() {
        return UpdateEvent.createUpdateEvent(TYPE).cast();
    }
}
