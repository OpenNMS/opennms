package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;


public abstract class AlarmSeverityUpdatedEventHandler extends AbstractDomEventCallback {
    public AlarmSeverityUpdatedEventHandler() {
        super(AlarmSeverityUpdatedEvent.TYPE);
    }
}
