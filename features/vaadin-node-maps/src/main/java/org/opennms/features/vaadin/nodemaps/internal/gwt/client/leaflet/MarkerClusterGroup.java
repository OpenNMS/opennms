package org.opennms.features.vaadin.nodemaps.internal.gwt.client.leaflet;

import java.util.List;

import org.discotools.gwt.leaflet.client.Options;
import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.layers.ILayer;
import org.discotools.gwt.leaflet.client.marker.Marker;

import com.google.gwt.core.client.JsArray;

public class MarkerClusterGroup extends ILayer {
    protected MarkerClusterGroup(final JSObject element) {
        super(element);
    }

    public MarkerClusterGroup() {
        this(MarkerClusterGroupImpl.create(JSObject.createJSObject()));
    }

    public MarkerClusterGroup(final Options options) {
        this(MarkerClusterGroupImpl.create(options.getJSObject()));
    }

    public void clearLayers() {
        MarkerClusterGroupImpl.clearLayers(getJSObject());
    }

    public void addLayer(final Marker marker) {
        MarkerClusterGroupImpl.addLayer(getJSObject(), marker.getJSObject());
    }

    public void addLayers(final List<? extends Marker> markers) {
        final JsArray<JSObject> markerArray = JsArray.createArray().cast();
        for (final Marker marker : markers) {
            markerArray.push(marker.getJSObject());
        }
        MarkerClusterGroupImpl.addLayers(getJSObject(), markerArray);
    }
}
