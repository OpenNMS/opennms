package org.opennms.features.vaadin.nodemaps.internal.gwt.client.leaflet;

import java.util.List;

import org.discotools.gwt.leaflet.client.Options;
import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.layers.others.FeatureGroup;
import org.discotools.gwt.leaflet.client.marker.Marker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.MarkerClusterEventCallback;

import com.google.gwt.core.client.JsArray;

public class MarkerClusterGroup extends FeatureGroup {
    public MarkerClusterGroup(final JSObject element) {
        super(element);
    }

    public MarkerClusterGroup() {
        this(MarkerClusterGroupImpl.create(JSObject.createJSObject()));
    }

    public MarkerClusterGroup(final Options options) {
        this(MarkerClusterGroupImpl.create(options.getJSObject()));
    }

    public MarkerClusterGroup addLayer(final Marker marker) {
        MarkerClusterGroupImpl.addLayer(getJSObject(), marker.getJSObject());
        return this;
    }

    public MarkerClusterGroup addLayers(final List<? extends Marker> markers) {
        final JsArray<JSObject> markerArray = JsArray.createArray().cast();
        for (final Marker marker : markers) {
            markerArray.push(marker.getJSObject());
        }
        MarkerClusterGroupImpl.addLayers(getJSObject(), markerArray);
        return this;
    }

    public MarkerClusterGroup bindPopup(final String htmlContent, final Options options) {
        MarkerClusterGroupImpl.bindPopup(getJSObject(), htmlContent, options.getJSObject());   
        return this;
    }

    public MarkerClusterGroup on(final String event, final MarkerClusterEventCallback callback) {
        MarkerClusterGroupImpl.on(getJSObject(), event, callback);
        return this;
    }

    public final JSObject getMapObject() {
        return MarkerClusterGroupImpl.getMapObject(getJSObject());
    }

}
