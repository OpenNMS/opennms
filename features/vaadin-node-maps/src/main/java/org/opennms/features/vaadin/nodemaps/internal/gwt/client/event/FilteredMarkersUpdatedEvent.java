package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search.UpdateEvent;

public class FilteredMarkersUpdatedEvent extends UpdateEvent {
    public static final String TYPE = "filteredMarkersUpdated";

    protected FilteredMarkersUpdatedEvent() {
    }

    public static final FilteredMarkersUpdatedEvent createEvent() {
        return UpdateEvent.createUpdateEvent(TYPE).cast();
    }
}
