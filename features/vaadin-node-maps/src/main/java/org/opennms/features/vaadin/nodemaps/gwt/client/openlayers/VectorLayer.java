package org.opennms.features.vaadin.nodemaps.gwt.client.openlayers;

import com.google.gwt.core.client.JavaScriptObject;

public class VectorLayer extends JavaScriptObject {
    protected VectorLayer() {}

    private static final GeoJSONFormat m_format = GeoJSONFormat.create(Projection.create("EPSG:900913"), Projection.create("EPSG:4326"));
    
    public final native void replaceFeatureCollection(final FeatureCollection features) /*-{
        var vectorFeatures = @org.opennms.features.vaadin.nodemaps.gwt.client.openlayers.VectorLayer::m_format.read(features);
        this.removeAllFeatures({silent: true});
        this.addFeatures(vectorFeatures);
    }-*/;
}