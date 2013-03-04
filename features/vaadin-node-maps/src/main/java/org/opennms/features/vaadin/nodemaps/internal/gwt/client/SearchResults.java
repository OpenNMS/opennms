package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;

public class SearchResults extends JSObject {
    protected SearchResults() {}

    public static SearchResults create() {
        return JSObject.createJSObject().cast();
    }
}
