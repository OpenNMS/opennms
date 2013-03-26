package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.layers.ILayerImpl;

public class GoogleLayerImpl extends ILayerImpl {
    public static native JSObject create(String type, JSObject options)/*-{                          
        return new $wnd.L.Google(type, options);
    }-*/;
}
