package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.MarkerProvider;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;

public class MarkerContainer implements MarkerProvider {
    final List<NodeMarker> m_markers         = new ArrayList<NodeMarker>();
    final List<NodeMarker> m_filteredMarkers = new ArrayList<NodeMarker>();

    private MarkerFilter m_filter;

    public MarkerContainer(final MarkerFilter filter) {
        m_filter = filter;
    }

    public int size() {
        return getMarkers().size();
    }

    public ListIterator<NodeMarker> listIterator() {
        return getMarkers().listIterator();
    }

    public List<NodeMarker> getDisabledMarkers() {
        final ArrayList<NodeMarker> markers = new ArrayList<NodeMarker>();
        for (final NodeMarker marker : m_markers) {
            if (!m_filteredMarkers.contains(marker)) {
                markers.add(marker);
            }
        }
        return Collections.unmodifiableList(markers);
    }

    public List<NodeMarker> getMarkers() {
        return Collections.unmodifiableList(m_filteredMarkers);
    }

    public void setMarkers(final List<NodeMarker> markers) {
        m_markers.clear();
        m_markers.addAll(markers);
        refresh();
    }

    public void refresh() {
        m_filteredMarkers.clear();
        for (final NodeMarker marker : m_markers) {
            if (m_filter.matches(marker)) {
                m_filteredMarkers.add(marker);
            }
        }
    }
}
