package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.SearchConsumer;

import com.google.gwt.user.client.ui.Widget;

public abstract class SearchEventCallback extends DomEventCallback {
    private SearchConsumer m_searchConsumer;

    public SearchEventCallback(final String[] eventTypes, final Widget widget, final SearchConsumer searchConsumer) {
        super(eventTypes, widget);
        m_searchConsumer = searchConsumer;
    }

    public SearchEventCallback(final String eventType, final Widget widget, final SearchConsumer searchConsumer) {
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
            self.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.SearchEventCallback::onEvent(Lcom/google/gwt/dom/client/NativeEvent;)(event);
        }
    }-*/;
}
