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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionAware;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.topo.CollapsibleCriteria;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Defaults;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.TopologyProviderInfo;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.api.topo.blablabla.CollapsibleGraph;
import org.opennms.features.topology.api.topo.blablabla.XXXGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * This class will be used to filter a topology so that the semantic zoom level is
 * interpreted as a hop distance away from a set of selected vertices. The vertex 
 * selection is specified using sets of {@link VertexHopCriteria} filters.
 * 
 * @author Seth
 */
public class VertexHopGraphProvider implements GraphProvider, SelectionAware {
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(VertexHopGraphProvider.class);

    // TODO MVR move these somewhere else
    public static WrappedVertexHopCriteria getWrappedVertexHopCriteria(GraphContainer graphContainer) {
        final Set<VertexHopCriteria> vertexHopCriterias = Criteria.getCriteriaForGraphContainer(graphContainer, VertexHopCriteria.class);
        return new WrappedVertexHopCriteria(vertexHopCriterias);
    }

    // TODO MVR move these somewhere else
    public static CollapsibleCriteria[] getCollapsedCriteriaForContainer(GraphContainer graphContainer) {
        return getCollapsedCriteria(graphContainer.getCriteria());
    }

    // TODO MVR move these somewhere else
    public static CollapsibleCriteria[] getCollapsedCriteria(Criteria[] criteria) {
        return getCollapsibleCriteria(criteria, true);
    }

    // TODO MVR move these somewhere else
    public static CollapsibleCriteria[] getCollapsibleCriteriaForContainer(GraphContainer graphContainer) {
        return getCollapsibleCriteria(graphContainer.getCriteria());
    }

    // TODO MVR move these somewhere else
    public static CollapsibleCriteria[] getCollapsibleCriteria(Criteria[] criteria) {
        return getCollapsibleCriteria(criteria, false);
    }

    // TODO MVR move these somewhere else
    public static CollapsibleCriteria[] getCollapsibleCriteria(Criteria[] criteria, boolean onlyCollapsed) {
        List<CollapsibleCriteria> retval = new ArrayList<CollapsibleCriteria>();
        if (criteria != null) {
            for (Criteria criterium : criteria) {
                try {
                    CollapsibleCriteria hopCriteria = (CollapsibleCriteria)criterium;
                    if (onlyCollapsed) {
                        if (hopCriteria.isCollapsed()) {
                            retval.add(hopCriteria);
                        }
                    } else {
                        retval.add(hopCriteria);
                    }
                } catch (ClassCastException e) {}
            }
        }
        return retval.toArray(new CollapsibleCriteria[0]);
    }

    public abstract static class VertexHopCriteria extends Criteria {
        private String m_label = "";
        private String m_id = "";
        
        @Override
        public String toString() {
            return "Namespace:"+getNamespace()+", ID:"+getId()+", Label:"+getLabel();
        }

        //Adding explicit constructor because I found that this label must be set
        //for the focus list to have meaningful information in the focus list.
        public VertexHopCriteria(String label) {
        	m_label = label;
        }
        
        public VertexHopCriteria(String id, String label) {
        	m_id = id;
        	m_label = label;
        }

        @Override
        public ElementType getType() {
            return ElementType.VERTEX;
        }

        public abstract Set<VertexRef> getVertices();

        public String getLabel() {
            return m_label;
        }

        public void setLabel(String label) {
            m_label = label;
        }

        public void setId(String id){
            m_id = id;
        }

        public String getId(){
            return m_id;
        }

        public boolean isEmpty() {
            Set<VertexRef> vertices = getVertices();
            if (vertices == null) {
                return false;
            }
            return vertices.isEmpty();
        }
    }

    /**
     * Wrapper class to wrap a bunch of {@link VertexHopCriteria}.
     * There may be multiple {@link VertexHopCriteria} objects available.
     * However in the end it is easier to use this criteria object to wrap all available {@link VertexHopCriteria}
     * instead of iterating over all all the time and determine all vertices.
     */
    public static class WrappedVertexHopCriteria extends VertexHopCriteria {

        private final Set<VertexHopCriteria> criteriaList;

        public WrappedVertexHopCriteria(Set<VertexHopCriteria> vertexHopCriterias) {
            super("Wrapped Vertex Hop Criteria for all VertexHopCriteria in the currently selected GraphProvider");
            criteriaList = Objects.requireNonNull(vertexHopCriterias);
        }

        public void addCriteria(VertexHopCriteria criteria) {
            this.criteriaList.add(criteria);
        }

        @Override
        public Set<VertexRef> getVertices() {
            Set<VertexRef> vertices = criteriaList.stream()
                    .flatMap(criteria -> criteria.getVertices().stream())
                    .collect(Collectors.toSet());
            return vertices;
        }

        @Override
        public String getNamespace() {
            return "$wrapped$";
        }

        @Override
        public int hashCode() {
            return Objects.hash(criteriaList);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj instanceof WrappedVertexHopCriteria) {
                WrappedVertexHopCriteria other = (WrappedVertexHopCriteria) obj;
                return Objects.equals(criteriaList, other.criteriaList);
            }
            return false;
        }

        public boolean contains(VertexRef vertexRef) {
            return getVertices().contains(vertexRef);
        }
    }

    /**
     * Helper criteria class to reference to existing VertexRefs.
     * This should be used anytime you want to add a vertex to the current focus (e.g. from the mouse context menu).
     */
    public static class DefaultVertexHopCriteria extends VertexHopCriteria {

        private final VertexRef vertexRef;

        public DefaultVertexHopCriteria(VertexRef vertexRef) {
            super(vertexRef.getId(), vertexRef.getLabel());
            this.vertexRef = vertexRef;
        }

        @Override
        public Set<VertexRef> getVertices() {
            return Sets.newHashSet(vertexRef);
        }

        @Override
        public String getNamespace() {
            return vertexRef.getNamespace();
        }

        @Override
        public int hashCode() {
            return vertexRef.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj instanceof DefaultVertexHopCriteria) {
                return Objects.equals(vertexRef, ((DefaultVertexHopCriteria) obj).vertexRef);
            }
            return false;
        }
    }

    private final GraphProvider m_delegate;
    private XXXGraph graph;

    public VertexHopGraphProvider(GraphProvider delegate) {
        m_delegate = delegate;
    }

    @Override
    public boolean contributesTo(ContentType type) {
        return m_delegate.contributesTo(type);
    }

    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType type) {
        return m_delegate.getSelection(selectedVertices, type);
    }

    @Override
    public XXXGraph getCurrentGraph() {
        return this.graph;
    }

    @Override
    public void refresh() {
        m_delegate.refresh();
        this.graph = new CollapsibleGraph(m_delegate.getCurrentGraph());
    }

    @Override
    public String getNamespace() {
        return m_delegate.getNamespace();
    }

    @Override
    public Defaults getDefaults() {
        return m_delegate.getDefaults();
    }

    @Override
    public TopologyProviderInfo getTopologyProviderInfo() {
        return m_delegate.getTopologyProviderInfo();
    }
}
