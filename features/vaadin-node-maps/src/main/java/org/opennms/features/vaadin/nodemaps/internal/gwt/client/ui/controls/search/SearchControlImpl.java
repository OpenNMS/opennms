package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search;

import org.discotools.gwt.leaflet.client.controls.ControlImpl;
import org.discotools.gwt.leaflet.client.jsobject.JSObject;

import com.google.gwt.user.client.Element;

public class SearchControlImpl extends ControlImpl {

    public static native final JSObject create(final SearchControl context, final JSObject options) /*-{
        var obj = new $wnd.L.Control(options);
        obj.onAdd = function(map) {
            return context.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search.SearchControl::doOnAdd(Lcom/google/gwt/core/client/JavaScriptObject;)(map);
        };
        obj.onRemove = function(map) {
            context.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search.SearchControl::doOnRemove(Lcom/google/gwt/core/client/JavaScriptObject;)(map);
        };
        return obj;
    }-*/;

    public static native Element createElement(final String className) /*-{
        return $wnd.L.DomUtil.create('div', className);
    }-*/;

}
