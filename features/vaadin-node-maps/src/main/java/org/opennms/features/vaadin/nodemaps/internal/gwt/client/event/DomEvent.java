package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;


import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

public class DomEvent {

    public static void stopEventPropagation(final Widget widget) {
        for (final String event : new String[] { "keydown", "keyup", "keypress", "input", "cut", "paste", "click", "dblclick", "mousedown", "mouseup", "touchstart", "touchend", "scrollstart", "scrollstop" }) {
            stopEventPropagation(widget, event);
        }
    }

    public static void stopEventPropagation(final Widget widget, final String event) {
        stopEventPropagation(widget.getElement(), event);
    }

    private static native void stopEventPropagation(final Element element, final String event) /*-{
        $wnd.L.DomEvent.on(element, event, $wnd.L.DomEvent.stopPropagation);
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
