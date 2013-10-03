package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.JSNodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.DomEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.FilterUpdatedEventHandler;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.FilteredMarkersUpdatedEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.MarkersModelUpdatedEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.MarkersModelUpdatedEventHandler;

import com.google.gwt.dom.client.NativeEvent;

public class MarkerContainer implements MarkerProvider {
    List<JSNodeMarker> m_markers         = new ArrayList<JSNodeMarker>();
    List<JSNodeMarker> m_filteredMarkers = new ArrayList<JSNodeMarker>();

    private Logger logger = Logger.getLogger(getClass().getName());
    private MarkerFilter m_filter;
    private FilterUpdatedEventHandler m_filterHandler;
    private MarkersModelUpdatedEventHandler m_markersModelUpdatedHandler;

    public MarkerContainer(final MarkerFilter filter) {
        m_filter = filter;
    }

    public void onLoad() {
        m_filterHandler = new FilterUpdatedEventHandler() {
            @Override public void onEvent(final NativeEvent nativeEvent) {
                logger.log(Level.INFO, "MarkerContainer.onFilterUpdated()");
                refresh();
            }
        };
        DomEvent.addListener(m_filterHandler);

        m_markersModelUpdatedHandler = new MarkersModelUpdatedEventHandler() {
            @Override public void onEvent(final NativeEvent event) {
                logger.log(Level.INFO, "MarkerContainer.onMarkersModelUpdated()");
                refresh();
            }
        };

        refresh();
    }

    public void onUnload() {
        if (m_filterHandler != null) DomEvent.removeListener(m_filterHandler);
        if (m_markersModelUpdatedHandler != null) DomEvent.removeListener(m_markersModelUpdatedHandler);
    }

    public int size() {
        return getMarkers().size();
    }

    public ListIterator<JSNodeMarker> listIterator() {
        return getMarkers().listIterator();
    }

    public List<JSNodeMarker> getDisabledMarkers() {
        final ArrayList<JSNodeMarker> markers = new ArrayList<JSNodeMarker>();
        final List<JSNodeMarker> existingMarkers = getMarkers();
        if (existingMarkers != null) {
            for (final NodeMarker marker : existingMarkers) {
                if (marker instanceof JSNodeMarker) {
                    final JSNodeMarker m = (JSNodeMarker)marker;
                    if (!m_filteredMarkers.contains(m)) {
                        markers.add(m);
                    }
                }
            }
        }
        return Collections.unmodifiableList(markers);
    }

    public List<JSNodeMarker> getAllMarkers() {
        return Collections.unmodifiableList(m_markers);
    }

    @Override
    public List<JSNodeMarker> getMarkers() {
        return Collections.unmodifiableList(m_filteredMarkers);
    }

    public void setMarkers(final List<JSNodeMarker> markers) {
        if (markers == null) {
            logger.log(Level.INFO, "MarkerContainer.setMarkers(): clearing master marker list in the marker container.");
            if (m_markers != null) m_markers.clear();
        } else {
            logger.log(Level.INFO, "MarkerContainer.setMarkers(): saving " + markers.size() + " markers to the master marker list in the marker container.");
            m_markers = markers;
        }
        DomEvent.send(MarkersModelUpdatedEvent.createEvent());
        refresh();
    }

    public void refresh() {
        logger.log(Level.INFO, "MarkerContainer.refresh()");

        final List<JSNodeMarker> markers = new ArrayList<JSNodeMarker>();
        final List<JSNodeMarker> existingMarkers = getAllMarkers();
        if (existingMarkers != null) {
            for (final JSNodeMarker marker : existingMarkers) {
                if (m_filter.matches(marker)) {
                    markers.add(marker);
                }
            }
        }
        m_filteredMarkers = markers;
        logger.log(Level.INFO, "MarkerContainer.refresh(): out of " + getMarkers().size() + " markers, " + markers.size() + " matched the search filter.");
        DomEvent.send(FilteredMarkersUpdatedEvent.createEvent());
    }
}
