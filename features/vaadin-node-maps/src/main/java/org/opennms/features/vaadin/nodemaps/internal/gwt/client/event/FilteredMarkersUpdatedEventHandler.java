package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import com.google.gwt.dom.client.Document;

public abstract class FilteredMarkersUpdatedEventHandler extends DomEventCallback {
    public FilteredMarkersUpdatedEventHandler() {
        super(FilteredMarkersUpdatedEvent.TYPE, Document.get().getDocumentElement());
    }
}
