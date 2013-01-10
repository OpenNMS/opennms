package org.opennms.features.vaadin.nodemaps.gwt.client.openlayers;

import com.google.gwt.core.client.JavaScriptObject;

public class GeoJSONProperties extends JavaScriptObject {
    protected GeoJSONProperties() {}

    public native final String getAttribute(final String key) /*-{
        return this[key];
    }-*/;

    public native final void setAttribute(final String key, final String value) /*-{
        this[key] = value;
    }-*/;

    public static native GeoJSONProperties create() /*-{
        return {};
    }-*/;

}