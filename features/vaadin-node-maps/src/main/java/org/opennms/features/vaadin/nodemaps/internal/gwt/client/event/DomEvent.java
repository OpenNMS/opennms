package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;


import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search.UpdateEvent;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;

public class DomEvent {

    public static void stopEventPropagation(final Widget widget) {
        for (final String event : new String[] { "keydown", "keyup", "keypress", "input", "cut", "paste", "click", "dblclick", "mousedown", "mouseup", "touchstart", "touchend", "scrollstart", "scrollstop" }) {
            stopEventPropagation(widget.getElement(), event);
        }
    }

    private static void stopEventPropagation(final Widget widget, final String event) {
        stopEventPropagation(widget.getElement(), event);
    }

    private static native void stopEventPropagation(final Element element, final String event) /*-{
        $wnd.L.DomEvent.on(element, event, $wnd.L.DomEvent.stopPropagation);
    }-*/;

    public static void addListener(final DomEventCallback callback) {
        addListener(callback, callback.getWidget());
    }

    protected static void addListener(final DomEventCallback callback, final Widget widget) {
        final Element element;
        if (widget == null || widget.getElement() == null) {
            element = Document.get().getDocumentElement();
        } else {
            element = widget.getElement();
        }
        for (final String eventType : callback.getEventTypes()) {
            addListener(element, eventType, callback, element);
        }
    }

    public static void addListener(final DomEventCallback callback, final boolean defer) {
        Scheduler.get().scheduleDeferred(new Command() {
            @Override public void execute() {
                addListener(callback);
            }
        });
    }

    protected static void addListener(final DomEventCallback callback, final Element element, final boolean defer) {
        if (defer) {
            Scheduler.get().scheduleDeferred(new Command() {
                @Override public void execute() {
                    addListener(callback, element);
                }
            });
        } else {
            addListener(callback, element);
        }
    }

    protected static void addListener(final DomEventCallback callback, final Element element) {
        for (final String eventType : callback.getEventTypes()) {
            addListener(element, eventType, callback, element);
        }
    }

    protected static native void addListener(final Element element, final String eventType, final DomEventCallback callback, final Element context) /*-{
        $wnd.L.DomEvent.addListener(element, eventType, callback.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.DomEventCallback::getJSObject()(), context);
    }-*/;

    public static void removeListener(final DomEventCallback callback) {
        final Element element = callback.getElement();
        removeListener(callback, element);
    }

    protected static void removeListener(final DomEventCallback callback, final Element element) {
        for (final String eventType : callback.getEventTypes()) {
            removeListener(element, eventType, callback);
        }
    }

    private static native void removeListener(final Element element, final String eventType, final DomEventCallback callback) /*-{
        $wnd.L.DomEvent.removeListener(element, eventType, callback.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.DomEventCallback::getJSObject()());
    }-*/;

    
    public static void send(final UpdateEvent event) {
        send(event, Document.get().getDocumentElement());
    }

    public static native void send(final UpdateEvent event, final Element context) /*-{
        console.log("dispatching event(" + event.type + "): ", event);
        context.dispatchEvent(event);
    }-*/;
}
