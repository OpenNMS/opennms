package org.opennms.features.vaadin.nodemaps.gwt.client.openlayers;

import com.google.gwt.core.client.JavaScriptObject;

public class VectorLayer extends JavaScriptObject {
    protected VectorLayer() {}

    public final native void replaceFeatureCollection(final FeatureCollection features) /*-{
        var geojson_format = new $wnd.OpenLayers.Format.GeoJSON({
            internalProjection: new $wnd.OpenLayers.Projection("EPSG:900913"),
            externalProjection: new $wnd.OpenLayers.Projection("EPSG:4326")
        });
        var vectorFeatures = geojson_format.read(features);
        this.removeAllFeatures({silent: true});
        this.addFeatures(vectorFeatures);
    }-*/;
}