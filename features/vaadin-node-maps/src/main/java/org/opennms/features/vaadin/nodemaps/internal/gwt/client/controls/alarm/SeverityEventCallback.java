package org.opennms.features.vaadin.nodemaps.internal.gwt.client.controls.alarm;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.GwtCallback;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.SearchConsumer;

import com.google.gwt.user.client.ui.Widget;

public abstract class SeverityEventCallback extends DomEventCallback {
    private SearchConsumer m_searchConsumer;

    public SeverityEventCallback(final String eventType, final Widget widget, final SearchConsumer searchConsumer) {
        super(eventType, widget);
        m_searchConsumer = searchConsumer;
    }

    protected SearchConsumer getSearchConsumer() {
        return m_searchConsumer;
    }

    @Override
    protected native JSObject getCallbackFunction(final GwtCallback callback) /*-{
        var self = callback;
        return function(event) {
            self.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.controls.alarm.SeverityEventCallback::onEvent(Lcom/google/gwt/dom/client/NativeEvent;)(event);
        }
    }-*/;
}
