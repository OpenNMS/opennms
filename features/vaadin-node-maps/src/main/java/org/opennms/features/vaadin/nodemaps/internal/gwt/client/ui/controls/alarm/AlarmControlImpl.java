package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.alarm;

import org.discotools.gwt.leaflet.client.controls.ControlImpl;
import org.discotools.gwt.leaflet.client.jsobject.JSObject;

import com.google.gwt.user.client.Element;

public class AlarmControlImpl extends ControlImpl {
    public static native JSObject create(final AlarmControl context, final JSObject options) /*-{
        var obj = new $wnd.L.Control(options);
        obj.onAdd = function(map) {
            return context.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.alarm.AlarmControl::doOnAdd(Lcom/google/gwt/core/client/JavaScriptObject;)(map);
        };
        obj.onRemove = function(map) {
            context.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.alarm.AlarmControl::doOnRemove(Lcom/google/gwt/core/client/JavaScriptObject;)(map);
        };
        return obj;
    }-*/;

    public static native Element createElement(final String className) /*-{
        return $wnd.L.DomUtil.create('div', className);
    }-*/;
}
