package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import com.google.gwt.dom.client.Document;

public abstract class FilterUpdatedEventHandler extends DomEventCallback {
    public FilterUpdatedEventHandler() {
        super(FilterUpdatedEvent.TYPE, Document.get().getDocumentElement());
    }
}
