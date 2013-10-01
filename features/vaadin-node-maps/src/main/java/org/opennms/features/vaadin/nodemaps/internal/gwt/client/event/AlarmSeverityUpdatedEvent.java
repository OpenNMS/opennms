package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search.UpdateEvent;

public class AlarmSeverityUpdatedEvent extends UpdateEvent {
    public static final String TYPE = "alarmSeverityUpdated";

    protected AlarmSeverityUpdatedEvent() {
        super();
    }

    public static final AlarmSeverityUpdatedEvent createEvent(final int minimumSeverity) {
        final AlarmSeverityUpdatedEvent event = UpdateEvent.createUpdateEvent(TYPE).cast();
        event.setMinimumSeverity(minimumSeverity);
        return event;
    }

    protected final native void setMinimumSeverity(final int minimumSeverity) /*-{
        this.minimumSeverity = minimumSeverity;
    }-*/;

    public final native int getMinimumSeverity() /*-{
        return this.minimumSeverity;
    }-*/;
}
