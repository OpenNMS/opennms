package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import com.google.gwt.dom.client.Document;

public abstract class MarkersModelUpdatedEventHandler extends DomEventCallback {
    public MarkersModelUpdatedEventHandler() {
        super(FilteredMarkersUpdatedEvent.TYPE, Document.get().getDocumentElement());
    }
}
