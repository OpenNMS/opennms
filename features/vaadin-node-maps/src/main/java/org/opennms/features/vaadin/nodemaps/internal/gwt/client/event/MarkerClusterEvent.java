package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.MarkerCluster;

import com.google.gwt.dom.client.NativeEvent;

public class MarkerClusterEvent extends NativeEvent {
    protected MarkerClusterEvent() {}

    public final native MarkerCluster getMarkerCluster() /*-{
        return this.layer;
    }-*/;
}
