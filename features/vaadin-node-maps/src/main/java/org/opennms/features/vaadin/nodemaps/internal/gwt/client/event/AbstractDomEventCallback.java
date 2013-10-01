package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.jsobject.JSObjectWrapper;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractDomEventCallback extends JSObjectWrapper implements DomEventCallback {
    private String[] m_eventTypes;
    private Element m_element;
    private Widget m_widget;

    public AbstractDomEventCallback() {
        super(JSObject.createJSFunction());
        setJSObject(getCallbackFunction(this));
    }

    public AbstractDomEventCallback(final String[] eventTypes) {
        this();
        m_eventTypes = eventTypes;
    }

    public AbstractDomEventCallback(final String eventType) {
        this(new String[]{eventType});
    }

    public AbstractDomEventCallback(final String[] eventTypes, final Widget widget) {
        this(eventTypes);
        m_widget = widget;
    }

    public AbstractDomEventCallback(final String eventType, final Widget widget) {
        this(new String[] { eventType });
        m_widget = widget;
    }

    public AbstractDomEventCallback(final String[] eventTypes, final Element element) {
        this(eventTypes);
        m_element = element;
    }

    public AbstractDomEventCallback(final String eventType, final Element element) {
        this(new String[] { eventType });
        m_element = element;
    }

    public String[] getEventTypes() {
        return m_eventTypes;
    }
    
    public Widget getWidget() {
        return m_widget;
    }

    public Element getElement() {
        if (m_widget == null) {
            if (m_element == null) {
                return Document.get().getDocumentElement();
            } else {
                return m_element;
            }
        } else {
            return m_widget.getElement();
        }
    }

    protected native JSObject getCallbackFunction(final DomEventCallback callback) /*-{
        var self = callback;
        return function(event) {
            self.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.DomEventCallback::onEvent(Lcom/google/gwt/dom/client/NativeEvent;)(event);
        }
    }-*/;

}
