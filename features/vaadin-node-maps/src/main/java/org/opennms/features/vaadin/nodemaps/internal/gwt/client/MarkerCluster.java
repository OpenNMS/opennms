package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import java.util.ArrayList;
import java.util.List;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.marker.Marker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.leaflet.NodeMarker;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class MarkerCluster extends JavaScriptObject {
    protected MarkerCluster() {}

    public final native String getChildCount() /*-{
        return this.getChildCount();
    }-*/;

    public final List<? extends Marker> getAllChildMarkers() {
        final List<Marker> markers = new ArrayList<Marker>();
        final JsArray<JSObject> markerObjects = getMarkerObjects();
        if (markerObjects == null) return markers;

        for (int i = 0; i < markerObjects.length(); i++) {
            final JSObject markerObject = markerObjects.get(i);
            final NodeMarker marker = new NodeMarker(markerObject);
            markers.add(marker);
        }

        return markers;
    }

    private final native JsArray<JSObject> getMarkerObjects() /*-{
        return this.getAllChildMarkers();
    }-*/;
}
