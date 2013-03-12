package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;


import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

public class DomEvent {

    public static void stopEventPropagation(final Widget widget) {
        stopEventPropagation(widget.getElement());
    }

    private static native void stopEventPropagation(final Element element) /*-{
        var stop = $wnd.L.DomEvent.stopPropagation;
        $wnd.L.DomEvent
            .on(element, 'keydown', stop)
            .on(element, 'input', stop)
            .on(element, 'paste', stop)
            .on(element, 'click', stop)
            .on(element, 'mousedown', stop)
            .on(element, 'dblclick', stop)
            .on(element, 'touchstart', stop)
            .on(element, 'touchend', stop);
    }-*/;

    public static void addListener(final DomEventCallback callback) {
        final Element element = callback.getWidget().getElement();
        addListener(callback, element);
    }

    public static void addListener(final DomEventCallback callback, final Element element) {
        for (final String eventType : callback.getEventTypes()) {
            addListener(element, eventType, callback, element);
        }
    }

    public static native void addListener(final Element element, final String eventType, final DomEventCallback callback, final Element context) /*-{
        $wnd.L.DomEvent.addListener(element, eventType, callback.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.DomEventCallback::getJSObject()(), context);
    }-*/;

    public static void removeListener(final DomEventCallback callback) {
        final Element element = callback.getWidget().getElement();
        removeListener(callback, element);
    }

    public static void removeListener(final DomEventCallback callback, final Element element) {
        for (final String eventType : callback.getEventTypes()) {
            removeListener(element, eventType, callback);
        }
    }

    private static native void removeListener(final Element element, final String eventType, final DomEventCallback callback) /*-{
        $wnd.L.DomEvent.removeListener(element, eventType, callback.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.DomEventCallback::getJSObject()());
    }-*/;

}
