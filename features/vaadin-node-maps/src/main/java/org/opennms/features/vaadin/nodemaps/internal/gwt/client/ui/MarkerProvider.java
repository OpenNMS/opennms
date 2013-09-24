package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import java.util.List;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.JSNodeMarker;

public interface MarkerProvider {
    public List<JSNodeMarker> getMarkers();
}
