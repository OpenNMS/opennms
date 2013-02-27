package org.opennms.features.vaadin.nodemaps.internal.gwt.client.leaflet;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;

import com.google.gwt.core.client.JsArray;


public class MarkerClusterGroupImpl {
    public static native JSObject create(final JSObject options)/*-{
        return new $wnd.L.MarkerClusterGroup(options);
    }-*/;

    public static native void clearLayers(final JSObject self) /*-{
        self.clearLayers();
    }-*/;

    public static native void addLayer(final JSObject self, final JSObject marker) /*-{
        self.addLayer(marker);
    }-*/;

    public static native void addLayers(final JSObject self, final JsArray<JSObject> markers) /*-{
        self.addLayers(markers);
    }-*/;
}
