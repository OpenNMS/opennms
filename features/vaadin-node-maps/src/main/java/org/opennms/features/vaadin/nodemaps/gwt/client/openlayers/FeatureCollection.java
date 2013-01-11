package org.opennms.features.vaadin.nodemaps.gwt.client.openlayers;

import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.vaadin.terminal.gwt.client.VConsole;

public class FeatureCollection extends JavaScriptObject {
    protected FeatureCollection() {}
    
    public static FeatureCollection create(List<GeoJSONFeature> features) {
        final JsArray<GeoJSONFeature> jsFeatures = JsArray.createArray().cast();
        for (final GeoJSONFeature feature: features) {
            jsFeatures.push(feature);
        }
        return FeatureCollection.create(jsFeatures);
    }

    public static native FeatureCollection create(final JsArray<GeoJSONFeature> features) /*-{
        return {
            "type": "FeatureCollection",
            "features": features
        };
    }-*/;

    public final void logFeatures() {
        final JsArray<GeoJSONFeature> features = getFeatures();
        for (int i = 0; i < features.length(); i++) {
            final GeoJSONFeature feature = features.get(i);
            VConsole.log("feature: lon=" + feature.getLongitude() + ", lat=" + feature.getLatitude());
        }
    }

    public native final JsArray<GeoJSONFeature> getFeatures() /*-{
        return this.features;
    }-*/;
}
