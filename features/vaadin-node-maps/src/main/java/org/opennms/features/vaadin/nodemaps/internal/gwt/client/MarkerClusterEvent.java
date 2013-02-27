package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import com.google.gwt.dom.client.NativeEvent;

public class MarkerClusterEvent extends NativeEvent {
    protected MarkerClusterEvent() {}

    public final native MarkerCluster getMarkerCluster() /*-{
        return this.layer;
    }-*/;
}
