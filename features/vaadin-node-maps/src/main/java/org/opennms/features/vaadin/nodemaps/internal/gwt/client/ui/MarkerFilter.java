package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;

public interface MarkerFilter {
    public abstract boolean matches(final NodeMarker marker);
}
