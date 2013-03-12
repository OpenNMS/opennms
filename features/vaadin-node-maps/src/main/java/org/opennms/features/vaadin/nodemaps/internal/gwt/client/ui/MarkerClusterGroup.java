package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import java.util.List;

import org.discotools.gwt.leaflet.client.Options;
import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.layers.ILayer;
import org.discotools.gwt.leaflet.client.layers.others.FeatureGroup;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.MarkerClusterEventCallback;

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

    public MarkerClusterGroup addLayer(final ILayer layer) {
        MarkerClusterGroupImpl.addLayer(getJSObject(), layer.getJSObject());
        return this;
    }

    public MarkerClusterGroup addLayers(final List<ILayer> layers) {
        final JsArray<JSObject> layerArray = JsArray.createArray().cast();
        for (final ILayer marker : layers) {
            layerArray.push(marker.getJSObject());
        }
        MarkerClusterGroupImpl.addLayers(getJSObject(), layerArray);
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

    public boolean hasLayer(final ILayer layer) {
        return MarkerClusterGroupImpl.hasLayer(getJSObject(), layer.getJSObject());
    }
}
