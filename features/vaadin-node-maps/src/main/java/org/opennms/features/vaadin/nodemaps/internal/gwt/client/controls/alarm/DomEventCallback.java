package org.opennms.features.vaadin.nodemaps.internal.gwt.client.controls.alarm;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.GwtCallback;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.ui.Widget;

public abstract class DomEventCallback extends GwtCallback {
    private String m_eventType;
    private Widget m_widget;

    public DomEventCallback(final String eventType, final Widget widget) {
        super();
        m_eventType = eventType;
        m_widget    = widget;
    }

    public String getEventType() {
        return m_eventType;
    }
    public Widget getWidget() {
        return m_widget;
    }

    protected abstract void onEvent(final NativeEvent event);

    @Override
    protected native JSObject getCallbackFunction(final GwtCallback callback) /*-{
        var self = callback;
        return function(event) {
            self.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.controls.alarm.DomEventCallback::onEvent(Lcom/google/gwt/dom/client/NativeEvent;)(event);
        }
    }-*/;

}
