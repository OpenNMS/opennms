package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;

public class SearchImpl {
    public static native JSObject create(final JSObject options)/*-{
        return new $wnd.L.Control.Search(options);
    }-*/;
}
