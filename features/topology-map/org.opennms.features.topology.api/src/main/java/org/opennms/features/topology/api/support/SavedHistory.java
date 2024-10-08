/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.topology.api.support;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.CRC32;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.features.topology.api.BoundingBox;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.HistoryOperation;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.support.hops.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.support.hops.VertexHopCriteria;
import org.opennms.features.topology.api.topo.CollapsibleCriteria;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.SearchCriteria;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * Immutable class that stores a snapshot of the topology state (layout, selected/focused vertices, etc) at a given time.
 */
@XmlRootElement(name="saved-history")
@XmlAccessorType(XmlAccessType.FIELD)
public class SavedHistory {

    private static final Logger LOG = LoggerFactory.getLogger(SavedHistory.class);

    @XmlAttribute(name="semantic-zoom-level")
    private int m_szl;

    @XmlElement(name="bounding-box")
    @XmlJavaTypeAdapter(BoundingBoxAdapter.class)
    private BoundingBox m_boundBox;

    @XmlElement(name="locations")
    @XmlJavaTypeAdapter(VertexRefPointMapAdapter.class)
    private Map<VertexRef, Point> m_locations = new HashMap<>();

    @XmlElementWrapper(name="selection")
    @XmlElement(name="vertex")
    @XmlJavaTypeAdapter(VertexRefAdapter.class)
    private Set<VertexRef> m_selectedVertices = Sets.newHashSet();

    @XmlElementWrapper(name="focus")
    @XmlElement(name="vertex")
    @XmlJavaTypeAdapter(VertexRefAdapter.class)
    private Set<VertexRef> m_focusVertices = Sets.newHashSet();

    /**
     * A map of key-value settings for the HistoryOperation components that are registered.
     */
    @XmlElement(name="settings")
    @XmlJavaTypeAdapter(StringMapAdapter.class)
    private final Map<String,String> m_settings = new HashMap<>();

    @XmlElementWrapper(name="searches")
    @XmlElement(name="search-criteria")
    private final List<SearchResult> m_searchQueries = new ArrayList<>();

    protected SavedHistory() {
        // Here for JAXB support
    }

    public SavedHistory(GraphContainer graphContainer, Collection<HistoryOperation> operations) {
        this(
            graphContainer.getSemanticZoomLevel(),
            graphContainer.getMapViewManager().getCurrentBoundingBox(),
            saveLocations(graphContainer.getGraph()),
            getUnmodifiableSet(graphContainer.getSelectionManager().getSelectedVertexRefs()),
            getFocusVertices(graphContainer),
            getOperationSettings(graphContainer, operations),
            getSearchQueries(graphContainer.getCriteria()));
    }

    SavedHistory(
            int szl,
            BoundingBox box,
            Map<VertexRef,Point> locations,
            Set<VertexRef> selectedVertices,
            Set<VertexRef> focusVertices,
            Map<String,String> operationSettings,
            Collection<SearchResult> searchQueries) {
        m_szl = szl;
        m_boundBox = box;
        m_locations = locations;
        m_selectedVertices = selectedVertices;
        m_focusVertices = focusVertices;
        m_settings.putAll(operationSettings);
        m_searchQueries.addAll(Objects.requireNonNull(searchQueries));
        LOG.debug("Created " + toString());
    }

    public int getSemanticZoomLevel() {
        return m_szl;
    }

    public BoundingBox getBoundingBox() {
        return m_boundBox;
    }

    public String getFragment() {
        final StringBuilder retval = new StringBuilder().append("(").append(m_szl).append("),").append(m_boundBox.fragment()).append(",").append(m_boundBox.getCenter());
        // Add a CRC of all of the key-value pairs in m_settings to make the fragment unique
        CRC32 settingsCrc = new CRC32();
        for (Map.Entry<String,String> entry : m_settings.entrySet()) {
            settingsCrc.update(entry.getKey().getBytes(StandardCharsets.UTF_8));
            settingsCrc.update(entry.getValue().getBytes(StandardCharsets.UTF_8));
        }
        retval.append(String.format(",(%X)", settingsCrc.getValue()));

        CRC32 locationsCrc = new CRC32();
        for(Map.Entry<VertexRef, Point> entry : m_locations.entrySet()) {
            locationsCrc.update(entry.getKey().getId().getBytes(StandardCharsets.UTF_8));
            //TODO cast to int for now
            locationsCrc.update((int)entry.getValue().getX());
            locationsCrc.update((int)entry.getValue().getY());
        }
        retval.append(String.format(",(%X)", locationsCrc.getValue()));

        CRC32 selectionsCrc = new CRC32();
        for(VertexRef entry : m_selectedVertices) {
            selectionsCrc.update(entry.getNamespace().getBytes(StandardCharsets.UTF_8));
            selectionsCrc.update(entry.getId().getBytes(StandardCharsets.UTF_8));
        }
        retval.append(String.format(",(%X)", selectionsCrc.getValue()));

        CRC32 focusCrc = new CRC32();
        for(VertexRef entry : m_focusVertices) {
            focusCrc.update(entry.getNamespace().getBytes(StandardCharsets.UTF_8));
            focusCrc.update(entry.getId().getBytes(StandardCharsets.UTF_8));
        }
        retval.append(String.format(",(%X)", focusCrc.getValue()));

        CRC32 historyCrc = new CRC32();
        for (SearchResult query : m_searchQueries) {
            historyCrc.update(query.toString().getBytes(StandardCharsets.UTF_8));
        }
        retval.append(String.format(",(%X)", historyCrc.getValue()));

        return retval.toString();
    }

    public void apply(GraphContainer graphContainer, Collection<HistoryOperation> operations, ServiceLocator serviceLocator) {
        LOG.debug("Applying " + toString());

        graphContainer.clearCriteria();

        // Apply the history for each registered HistoryOperation
        for (HistoryOperation operation : operations) {
            try {
                operation.applyHistory(graphContainer, m_settings);
            } catch (Throwable e) {
                LOG.warn("Failed to perform applyHistory() operation", e);
            }
        }

        // Browse through all available search providers that have a "history" functionality
        List<SearchProvider> searchProviders = serviceLocator.findServices(SearchProvider.class,null);
        for (SearchProvider searchProvider : searchProviders) {
            if (searchProvider instanceof HistoryAwareSearchProvider) {
                // For each of these search providers generate Criteria for all search queries from history
                // Add resulting Criteria to the graph container
                for (SearchResult searchQuery : m_searchQueries) {
                    if (searchProvider.getSearchProviderNamespace().equals(searchQuery.getNamespace()) || searchProvider.contributesTo(searchQuery.getNamespace())) {
                        Criteria searchCriteria = ((HistoryAwareSearchProvider)searchProvider).buildCriteriaFromQuery(searchQuery, graphContainer);
                        graphContainer.addCriteria(searchCriteria);
                    }
                }
            }
        }

        // Set Vertices in Focus after all other operations are applied, otherwise the topology provider may have changed
        // which results in a graphContainer.clearCriteria()
        applyVerticesInFocus(m_focusVertices, graphContainer);
        applySavedLocations(m_locations, graphContainer.getGraph().getLayout());
        graphContainer.setSemanticZoomLevel(getSemanticZoomLevel());
        graphContainer.getSelectionManager().setSelectedVertexRefs(m_selectedVertices); // Apply the selected vertices
        graphContainer.getMapViewManager().setBoundingBox(getBoundingBox());
    }

    @Override
    public String toString() {
        final StringBuilder retval = new StringBuilder().append(this.getClass().getSimpleName()).append(": ").append(getFragment()).append(": ");
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

    private static Set<String> getSavedHistory(GraphContainer graphContainer, Collection<HistoryOperation> operations) {
        Set<String> retval = new HashSet<>();

        Map<String, String> settings = getOperationSettings(graphContainer, operations);
        return retval;
    }

    private static Set<VertexRef> getUnmodifiableSet(Collection<VertexRef> vertices) {
        HashSet<VertexRef> selectedVertices = new HashSet<>();
        selectedVertices.addAll(vertices);
        return Collections.unmodifiableSet(selectedVertices);
    }

    private static Map<String,String> getOperationSettings(GraphContainer graphContainer, Collection<HistoryOperation> operations) {
        Map<String,String> retval = new HashMap<>();
        for (HistoryOperation operation : operations) {
            retval.putAll(operation.createHistory(graphContainer));
        }
        return retval;
    }

    private static Set<VertexRef> getFocusVertices(GraphContainer graphContainer) {
        final Set<VertexRef> retVal = new HashSet<>();
        Criteria[] criterias = graphContainer.getCriteria();
        for (Criteria crit : criterias) {
            if (crit instanceof VertexHopCriteria
                    && !(crit instanceof CollapsibleCriteria)) {
                retVal.addAll(((VertexHopCriteria) crit).getVertices());
            }
        }
        return retVal;
    }

    private static Map<VertexRef,Point> saveLocations(Graph graph) {
        Collection<? extends Vertex> vertices = graph.getDisplayVertices();
        Map<VertexRef,Point> locations = new HashMap<>();
        for(Vertex vert : vertices) {
            locations.put(vert, graph.getLayout().getLocation(vert));
        }
        return locations;
    }

    private static void applyVerticesInFocus(Set<VertexRef> focusVertices, GraphContainer graphContainer) {
        focusVertices.forEach(vertexRef -> graphContainer.addCriteria(new DefaultVertexHopCriteria(vertexRef)));
    }

    private static void applySavedLocations(Map<VertexRef, Point> locations, Layout layout) {
        for(VertexRef ref : locations.keySet()) {
            layout.setLocation(ref, locations.get(ref));
        }
    }

    private static Collection<SearchResult> getSearchQueries(Criteria[] criteria) {
        Collection<SearchResult> queryHistories = new ArrayList<>();
        for (Criteria c : criteria) {
            if (c instanceof SearchCriteria) {
                queryHistories.add(new SearchResult((SearchCriteria)c));
            }
        }
        return queryHistories;
    }
}