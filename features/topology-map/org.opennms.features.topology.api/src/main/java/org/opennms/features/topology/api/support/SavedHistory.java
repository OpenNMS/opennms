/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.api.support;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.features.topology.api.BoundingBox;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.HistoryOperation;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.FocusNodeHopCriteria;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;

/**
 * Immutable class that stores a snapshot of the topology layout at a given time.
 */
@XmlRootElement(name="saved-history")
@XmlAccessorType(XmlAccessType.FIELD)
public class SavedHistory {

    @XmlAttribute(name="semantic-zoom-level")
    private int m_szl;

    @XmlElement(name="bounding-box")
    @XmlJavaTypeAdapter(BoundingBoxAdapter.class)
    private BoundingBox m_boundBox;

    @XmlElement(name="locations")
    @XmlJavaTypeAdapter(VertexRefPointMapAdapter.class)
    private Map<VertexRef, Point> m_locations = new HashMap<VertexRef, Point>();

    @XmlElement(name="selection")
    @XmlJavaTypeAdapter(VertexRefSetAdapter.class)
    private Set<VertexRef> m_selectedVertices;

    @XmlElement(name="focus")
    @XmlJavaTypeAdapter(VertexRefSetAdapter.class)
    private Set<VertexRef> m_focusVertices;

    /**
     * A map of key-value settings for the HistoryOperation components that are registered.
     */
    @XmlElement(name="settings")
    @XmlJavaTypeAdapter(StringMapAdapter.class)
    private final Map<String,String> m_settings = new HashMap<String,String>();

    protected SavedHistory() {
        // Here for JAXB support
    }

    private static Set<VertexRef> getUnmodifiableSet(Collection<VertexRef> vertices) {
        HashSet<VertexRef> selectedVertices = new HashSet<VertexRef>();
        selectedVertices.addAll(vertices);
        return Collections.unmodifiableSet(selectedVertices);
    }
    
    private static Map<String,String> getOperationSettings(GraphContainer graphContainer, Collection<HistoryOperation> operations) {
        Map<String,String> retval = new HashMap<String,String>();
        for (HistoryOperation operation : operations) {
            retval.putAll(operation.createHistory(graphContainer));
        }
        return retval;
    }
    
    public SavedHistory(GraphContainer graphContainer, Collection<HistoryOperation> operations) {
        this(
            graphContainer.getSemanticZoomLevel(), 
            graphContainer.getMapViewManager().getCurrentBoundingBox(),
            saveLocations(graphContainer.getGraph()),
            getUnmodifiableSet(graphContainer.getSelectionManager().getSelectedVertexRefs()),
            getFocusVertices(graphContainer),
            getOperationSettings(graphContainer, operations)
        );
    }

    protected static Set<VertexRef> getFocusVertices(GraphContainer graphContainer) {
        Set<VertexRef> retVal = new HashSet<VertexRef>();

        Criteria[] criterias = graphContainer.getCriteria();
        for (Criteria crit : criterias) {
            if (crit instanceof VertexHopGraphProvider.VertexHopCriteria && crit.getNamespace().equals("nodes")) {
                retVal.addAll(((VertexHopGraphProvider.VertexHopCriteria) crit).getVertices());
            }
        }

        FocusNodeHopCriteria criteria = VertexHopGraphProvider.getFocusNodeHopCriteriaForContainer(graphContainer, false);
        if (criteria != null) {
            retVal.addAll(criteria.getVertices());
        }

        return retVal;
    }

    SavedHistory(int szl, BoundingBox box, Map<VertexRef,Point> locations, Set<VertexRef> selectedVertices, Set<VertexRef> focusVertices, Map<String,String> operationSettings) {
        m_szl = szl;
        m_boundBox = box;
        m_locations = locations;
        m_selectedVertices = selectedVertices;
        m_focusVertices = focusVertices;
        m_settings.putAll(operationSettings);
        LoggerFactory.getLogger(this.getClass()).debug("Created " + toString());
    }

    private static Map<VertexRef,Point> saveLocations(Graph graph) {
        Collection<? extends Vertex> vertices = graph.getDisplayVertices();
        Map<VertexRef,Point> locations = new HashMap<VertexRef,Point>();
        for(Vertex vert : vertices) {
            locations.put(vert, graph.getLayout().getLocation(vert));
        }
        return locations;
    }

    public int getSemanticZoomLevel() {
        return m_szl;
    }
    
    public BoundingBox getBoundingBox() {
        return m_boundBox;
    }
    
    public String getFragment() {
        StringBuffer retval = new StringBuffer().append("(").append(m_szl).append("),").append(m_boundBox.fragment()).append(",").append(m_boundBox.getCenter());
        // Add a CRC of all of the key-value pairs in m_settings to make the fragment unique
        CRC32 settingsCrc = new CRC32();
        for (Map.Entry<String,String> entry : m_settings.entrySet()) {
            try {
                settingsCrc.update(entry.getKey().getBytes("UTF-8"));
                settingsCrc.update(entry.getValue().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // Impossible on modern JVMs
            }
        }
        retval.append(String.format(",(%X)", settingsCrc.getValue()));

        CRC32 locationsCrc = new CRC32();
        for(Map.Entry<VertexRef, Point> entry : m_locations.entrySet()) {
            try {
                locationsCrc.update(entry.getKey().getId().getBytes("UTF-8"));
                //TODO cast to int for now
                locationsCrc.update((int)entry.getValue().getX());
                locationsCrc.update((int)entry.getValue().getY());
            } catch(UnsupportedEncodingException e) {
                // Impossible on modern JVMs
            }
        }
        retval.append(String.format(",(%X)", locationsCrc.getValue()));

        CRC32 selectionsCrc = new CRC32();
        for(VertexRef entry : m_selectedVertices) {
            try {
                selectionsCrc.update(entry.getNamespace().getBytes("UTF-8"));
                selectionsCrc.update(entry.getId().getBytes("UTF-8"));
            } catch(UnsupportedEncodingException e) {
                // Impossible on modern JVMs
            }
        }
        retval.append(String.format(",(%X)", selectionsCrc.getValue()));

        CRC32 focusCrc = new CRC32();
        for(VertexRef entry : m_focusVertices) {
            try {
                focusCrc.update(entry.getNamespace().getBytes("UTF-8"));
                focusCrc.update(entry.getId().getBytes("UTF-8"));
            } catch(UnsupportedEncodingException e) {
                // Impossible on modern JVMs
            }
        }
        retval.append(String.format(",(%X)", focusCrc.getValue()));

        return retval.toString();
    }

    public void apply(GraphContainer graphContainer, Collection<HistoryOperation> operations) {
        LoggerFactory.getLogger(this.getClass()).debug("Applying " + toString());

        if (m_focusVertices.size() > 0) {
            FocusNodeHopCriteria criteria = VertexHopGraphProvider.getFocusNodeHopCriteriaForContainer(graphContainer);
            // Clear existing focus nodes
            criteria.clear();
            // Add focus nodes from history
            criteria.addAll(m_focusVertices);
        } else {
            // Remove any existing VertexHopCriteria
            FocusNodeHopCriteria criteria = VertexHopGraphProvider.getFocusNodeHopCriteriaForContainer(graphContainer, false);
            if (criteria != null) {
                graphContainer.removeCriteria(criteria);
            }
        }

        // Apply the history for each registered HistoryOperation
        for (HistoryOperation operation : operations) {
            try {
                operation.applyHistory(graphContainer, m_settings);
            } catch (Throwable e) {
                LoggerFactory.getLogger(this.getClass()).warn("Failed to perform applyHistory() operation", e);
            }
        }
        applySavedLocations(m_locations, graphContainer.getGraph().getLayout());
        graphContainer.setSemanticZoomLevel(getSemanticZoomLevel());

        // Apply the selected vertices
        graphContainer.getSelectionManager().setSelectedVertexRefs(m_selectedVertices);

        graphContainer.getMapViewManager().setBoundingBox(getBoundingBox());
    }

    private static void applySavedLocations(Map<VertexRef, Point> locations, Layout layout) {
        for(VertexRef ref : locations.keySet()) {
            layout.setLocation(ref, locations.get(ref));
        }
    }
    
    @Override
    public String toString() {
        StringBuffer retval = new StringBuffer().append(this.getClass().getSimpleName()).append(": ").append(getFragment()).append(": ");
        boolean first = true;
        for (Map.Entry<String,String> entry : m_settings.entrySet()) {
            if (first) { first = false; } else { retval.append(","); }
            retval.append(entry.getKey()).append("->").append(entry.getValue());
        }
        if (m_selectedVertices.size() > 0) {
            first = true;
            retval.append(",selectedVertices->{");
            for (VertexRef entry : m_selectedVertices) {
                if (first) { first = false; } else { retval.append(","); }
                retval.append("[").append(entry.getNamespace()).append(":").append(entry.getId()).append("]");
            }
            retval.append("}");
        }
        if (m_focusVertices.size() > 0) {
            first = true;
            retval.append(",focusVertices->{");
            for (VertexRef entry : m_focusVertices) {
                if (first) { first = false; } else { retval.append(","); }
                retval.append("[").append(entry.getNamespace()).append(":").append(entry.getId()).append("]");
            }
            retval.append("}");
        }
        return retval.toString();
    }
}
