package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import org.discotools.gwt.leaflet.client.controls.Control;
import org.discotools.gwt.leaflet.client.jsobject.JSObject;

public class Search extends Control {
    public Search(final JSObject element) {
        super(element);
    }

    public Search() {
        this(SearchImpl.create(JSObject.createJSObject()));
    }

    public Search(final SearchOptions options) {
        this(SearchImpl.create(options.getJSObject()));
    }
}
