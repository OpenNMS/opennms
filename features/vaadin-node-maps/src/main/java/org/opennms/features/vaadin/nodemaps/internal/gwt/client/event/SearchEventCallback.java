package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;

import com.google.gwt.user.client.ui.Widget;

public abstract class SearchEventCallback extends AbstractDomEventCallback {
    public SearchEventCallback(final String[] eventTypes, final Widget widget) {
        super(eventTypes, widget);
    }

    public SearchEventCallback(final String eventType, final Widget widget) {
        super(eventType, widget);
    }

    @Override
    protected native JSObject getCallbackFunction(final DomEventCallback callback) /*-{
        var self = callback;
        return function(event) {
            self.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.DomEventCallback::onEvent(Lcom/google/gwt/dom/client/NativeEvent;)(event);
        }
    }-*/;
}
