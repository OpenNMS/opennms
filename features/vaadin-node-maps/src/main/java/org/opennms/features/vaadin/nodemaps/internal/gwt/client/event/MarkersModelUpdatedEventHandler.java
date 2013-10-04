package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;


public abstract class MarkersModelUpdatedEventHandler extends AbstractDomEventCallback {
    public MarkersModelUpdatedEventHandler() {
        super(FilteredMarkersUpdatedEvent.TYPE);
    }
}
