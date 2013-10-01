package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import com.google.gwt.dom.client.Document;

public abstract class SearchStringUpdatedEventHandler extends DomEventCallback {
    public SearchStringUpdatedEventHandler() {
        super(SearchStringUpdatedEvent.TYPE, Document.get().getDocumentElement());
    }
}
