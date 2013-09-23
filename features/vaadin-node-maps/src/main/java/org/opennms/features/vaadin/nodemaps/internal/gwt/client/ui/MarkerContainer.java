package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.MarkerProvider;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.JSNodeMarker;

public class MarkerContainer implements MarkerProvider {
    final List<JSNodeMarker> m_markers         = new ArrayList<JSNodeMarker>();
    final List<JSNodeMarker> m_filteredMarkers = new ArrayList<JSNodeMarker>();

    private MarkerFilter m_filter;

    public MarkerContainer(final MarkerFilter filter) {
        m_filter = filter;
    }

    public int size() {
        return getMarkers().size();
    }

    public ListIterator<JSNodeMarker> listIterator() {
        return getMarkers().listIterator();
    }

    public List<JSNodeMarker> getDisabledMarkers() {
        final ArrayList<JSNodeMarker> markers = new ArrayList<JSNodeMarker>();
        for (final JSNodeMarker marker : m_markers) {
            if (!m_filteredMarkers.contains(marker)) {
                markers.add(marker);
            }
        }
        return Collections.unmodifiableList(markers);
    }

    @Override
    public List<JSNodeMarker> getMarkers() {
        return Collections.unmodifiableList(m_filteredMarkers);
    }

    public void setMarkers(final List<JSNodeMarker> markers) {
        if (m_markers != markers) {
            m_markers.clear();
            m_markers.addAll(markers);
        }
        refresh();
    }

    public void refresh() {
        m_filteredMarkers.clear();
        for (final JSNodeMarker marker : m_markers) {
            if (m_filter.matches(marker)) {
                m_filteredMarkers.add(marker);
            }
        }
    }
}
