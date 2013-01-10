package org.opennms.features.vaadin.nodemaps.gwt.client.openlayers;

import com.google.gwt.core.client.JavaScriptObject;

public class Projection extends JavaScriptObject {
    protected Projection() {}

    public static final native Projection create(final String projection) /*-{
        return new $wnd.OpenLayers.Projection(projection);
    }-*/;
}