package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.map.MapOptions;

public class Map extends org.discotools.gwt.leaflet.client.map.Map {
    public Map(final JSObject self) {
        super(self);
    }

    public Map(final String divId, final MapOptions options) {
        super(divId, options);
    }

    public int getMaxZoom() {
        return getMaxZoom(getJSObject());
    }

    private native int getMaxZoom(final JSObject self) /*-{
        return self.getMaxZoom();
    }-*/;
}