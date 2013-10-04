package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search.UpdateEvent;

public class FilterUpdatedEvent extends UpdateEvent {
    public static final String TYPE = "filterUpdated";

    protected FilterUpdatedEvent() {
    }

    public static final FilterUpdatedEvent createEvent() {
        return UpdateEvent.createUpdateEvent(TYPE).cast();
    }
}
