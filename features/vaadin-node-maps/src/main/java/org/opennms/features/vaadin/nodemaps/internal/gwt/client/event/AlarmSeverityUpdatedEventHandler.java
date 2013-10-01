package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import com.google.gwt.dom.client.Document;

public abstract class AlarmSeverityUpdatedEventHandler extends DomEventCallback {
    public AlarmSeverityUpdatedEventHandler() {
        super(AlarmSeverityUpdatedEvent.TYPE, Document.get().getDocumentElement());
    }
}
