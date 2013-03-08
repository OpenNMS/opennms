package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.controls.alarm.DomEventCallback;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

public class DomEvent {

    public static void stopClickPropagation(final Widget widget) {
        stopClickPropagation(widget.getElement());
    }

    private static native void stopClickPropagation(final Element element) /*-{
        var stop = $wnd.L.DomEvent.stopPropagation;
        $wnd.L.DomEvent
            .on(element, 'click', stop)
            .on(element, 'mousedown', stop)
            .on(element, 'dblclick', stop)
            .on(element, 'touchstart', stop)
            .on(element, 'touchend', stop);
    }-*/;

    public static void addListener(final DomEventCallback callback) {
        final Element element = callback.getWidget().getElement();
        addListener(element, callback.getEventType(), callback, element);
    }

    public static native void addListener(final Element element, final String eventType, final DomEventCallback callback, final Element context) /*-{
        $wnd.L.DomEvent.addListener(element, eventType, callback.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.controls.alarm.DomEventCallback::getJSObject()(), context);
    }-*/;

    public static void removeListener(final DomEventCallback callback) {
        removeListener(callback.getWidget().getElement(), callback.getEventType(), callback);
    }

    private static native void removeListener(final Element element, final String eventType, final DomEventCallback callback) /*-{
        $wnd.L.DomEvent.removeListener(element, eventType, callback.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.controls.alarm.DomEventCallback::getJSObject()());
    }-*/;

}
