package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import com.google.gwt.event.shared.GwtEvent;

public abstract class OpenNMSEvent<H extends OpenNMSEventHandler> extends GwtEvent<H> {
    public static class Type<H> extends GwtEvent.Type<H> {
    }

    public abstract void dispatch(H handler);
}
