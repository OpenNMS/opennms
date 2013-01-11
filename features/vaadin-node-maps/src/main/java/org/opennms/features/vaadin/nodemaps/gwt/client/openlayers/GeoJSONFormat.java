package org.opennms.features.vaadin.nodemaps.gwt.client.openlayers;

import com.google.gwt.core.client.JavaScriptObject;

public class GeoJSONFormat extends JavaScriptObject {
    protected GeoJSONFormat() {}

    public static final native GeoJSONFormat create(final Projection internal, final Projection external) /*-{
        var options = {};
        if (internal) {
            options.internalProjection = internal;
        }
        if (external) {
            options.externalProjection = external;
        }
        $wnd.console.log('internal:');
        $wnd.console.log(internal);
        $wnd.console.log('external:');
        $wnd.console.log(external);
        return new $wnd.OpenLayers.Format.GeoJSON(options);
    }-*/;
}