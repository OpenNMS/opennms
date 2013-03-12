package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import java.util.ArrayList;
import java.util.List;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.types.LatLng;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class MarkerCluster extends JavaScriptObject {
    protected MarkerCluster() {}

    public final native String getChildCount() /*-{
        return this.getChildCount();
    }-*/;

    public final List<NodeMarker> getAllChildMarkers() {
        final List<NodeMarker> markers = new ArrayList<NodeMarker>();
        final JsArray<JSObject> markerObjects = getMarkerObjects();
        if (markerObjects == null) return markers;

        for (int i = 0; i < markerObjects.length(); i++) {
            final JSObject markerObject = markerObjects.get(i);
            final NodeMarker marker = new NodeMarker(markerObject);
            markers.add(marker);
        }

        return markers;
    }

    public final MarkerClusterGroup getGroup() {
        return new MarkerClusterGroup(getNativeGroup());
    }

    private final native JSObject getNativeGroup() /*-{
        return this._group;
    }-*/;

    private final native JsArray<JSObject> getMarkerObjects() /*-{
        return this.getAllChildMarkers();
    }-*/;

    public final LatLng getLatLng() {
        return new LatLng(getNativeLatLng());
    }

    private final native JSObject getNativeLatLng() /*-{
        return this.getLatLng();
    }-*/;

    public final native void closePopup() /*-{
        this.closePopup();
    }-*/;
}
