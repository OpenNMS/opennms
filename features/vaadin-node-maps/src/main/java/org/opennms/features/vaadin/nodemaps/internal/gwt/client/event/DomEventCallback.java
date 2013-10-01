package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.ui.Widget;

public abstract class DomEventCallback extends GwtCallback {
    private String[] m_eventTypes;
    private Element m_element;
    private Widget m_widget;

    public DomEventCallback(final String[] eventTypes, final Widget widget) {
        super();
        m_eventTypes = eventTypes;
        m_widget     = widget;
    }

    public DomEventCallback(final String eventType, final Widget widget) {
        this(new String[] { eventType }, widget);
    }

    public DomEventCallback(final String[] eventTypes, final Element element) {
        super();
        m_eventTypes = eventTypes;
        m_element    = element;
    }

    public DomEventCallback(final String eventType, final Element element) {
        this(new String[] { eventType }, element);
    }

    public String[] getEventTypes() {
        return m_eventTypes;
    }
    
    public Widget getWidget() {
        return m_widget;
    }

    public Element getElement() {
        return m_widget == null? m_element : m_widget.getElement();
    }

    protected abstract void onEvent(final NativeEvent event);

    @Override
    protected native JSObject getCallbackFunction(final GwtCallback callback) /*-{
        var self = callback;
        return function(event) {
            self.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.DomEventCallback::onEvent(Lcom/google/gwt/dom/client/NativeEvent;)(event);
        }
    }-*/;

}
