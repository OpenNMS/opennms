package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.types.LatLng;

public class SearchResult extends JSObject {
    protected SearchResult() {}

    public static final SearchResult create(final String title, final LatLng latLng) {
        final SearchResult result = JSObject.createJSObject().cast();
        result.setTitle(title);
        result.setLatLng(latLng);
        return result;
    }

    public final String getTitle() {
        return getPropertyAsString("title");
    }
    public final SearchResult setTitle(final String title) {
        setProperty("title", title);
        return this;
    }
    
    public final LatLng getLatLng() {
        return new LatLng(getProperty("latLng"));
    }
    public final SearchResult setLatLng(final LatLng latLng) {
        setProperty("latLng", latLng.getJSObject());
        return this;
    }
}
