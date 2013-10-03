package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;


public abstract class FilteredMarkersUpdatedEventHandler extends AbstractDomEventCallback {
    public FilteredMarkersUpdatedEventHandler() {
        super(FilteredMarkersUpdatedEvent.TYPE);
    }
}
