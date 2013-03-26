package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.MarkerClusterEventCallback;

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

    public static native void on(final JSObject self, final String event, final MarkerClusterEventCallback callback) /*-{
        self.on(event, callback.@org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.MarkerClusterEventCallback::run(Lorg/opennms/features/vaadin/nodemaps/internal/gwt/client/event/MarkerClusterEvent;));
    }-*/;

    public static native void bindPopup(final JSObject self, final String htmlContent, final JSObject options) /*-{
        self.bindPopup(htmlContent, options);
    }-*/;

    public static native JSObject getMapObject(final JSObject self) /*-{
        return self._map;
    }-*/;

    public static native boolean hasLayer(final JSObject self, final JSObject layer) /*-{
        return self.hasLayer(layer);
    }-*/;
}
