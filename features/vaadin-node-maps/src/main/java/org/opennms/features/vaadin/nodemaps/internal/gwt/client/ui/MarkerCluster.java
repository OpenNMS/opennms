package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import java.util.ArrayList;
import java.util.List;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.types.LatLng;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.JSNodeMarker;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class MarkerCluster extends JavaScriptObject {
    protected MarkerCluster() {}

    public final native String getChildCount() /*-{
        return this.getChildCount();
    }-*/;

    public final List<JSNodeMarker> getAllChildMarkers() {
        final List<JSNodeMarker> markers = new ArrayList<JSNodeMarker>();
        final JsArray<JSObject> markerObjects = getMarkerObjects();
        if (markerObjects == null) return markers;

        for (int i = 0; i < markerObjects.length(); i++) {
            final JSObject markerObject = markerObjects.get(i);
            final JSNodeMarker marker = new JSNodeMarker(markerObject);
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
