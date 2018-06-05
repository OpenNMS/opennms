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

package org.opennms.features.topology.plugins.ncs;

import java.util.*;

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeListener;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.plugins.ncs.NCSEdgeProvider.NCSVertex;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;

public class NCSPathEdgeProvider implements EdgeProvider {

    public static final String PATH_NAMESPACE = "ncsPath";
    private static final String HTML_TOOLTIP_TAG_OPEN = "<p>";
    private static final String HTML_TOOLTIP_TAG_END  = "</p>";
    
    public static class NCSServicePathCriteria extends Criteria implements Iterable<Edge> {
        private static final long serialVersionUID = 5833760704861282509L;
        private List<Edge> m_edgeList;
        public NCSServicePathCriteria(List<Edge> edges) {
            m_edgeList = edges;
        }
        
        @Override
        public ElementType getType() {
            return ElementType.EDGE;
        }

        @Override
        public String getNamespace() {
            return PATH_NAMESPACE;
        }

        @Override
        public int hashCode() {
            return m_edgeList.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof NCSServicePathCriteria){
                NCSServicePathCriteria c = (NCSServicePathCriteria) obj;
                return c.m_edgeList.equals(m_edgeList);
            }
            return false;
        }

        public List<Edge> getEdges(){
            return m_edgeList;
        }

        @Override
        public Iterator<Edge> iterator() {
            return m_edgeList.iterator();
        }
    }
    
    public static class NCSPathEdge extends AbstractEdge {
        private final String m_serviceName;
        private final String m_deviceA;
        private final String m_deviceZ;

        public NCSPathEdge (String serviceName, String deviceA, String deviceZ, NCSVertex source, NCSVertex target) {
            super("ncsPath", source.getId() + ":::" + target.getId(), source, target);
            m_serviceName = serviceName;
            m_deviceA = deviceA;
            m_deviceZ = deviceZ;
            setStyleName("ncs edge");
        }

        @Override
        public String getTooltipText() {
            final StringBuilder toolTip = new StringBuilder();

            toolTip.append(HTML_TOOLTIP_TAG_OPEN);
            toolTip.append("Service: " + m_serviceName);
            toolTip.append(HTML_TOOLTIP_TAG_END);

            toolTip.append(HTML_TOOLTIP_TAG_OPEN);
            toolTip.append("Source: " + m_deviceA);
            toolTip.append(HTML_TOOLTIP_TAG_END);

            toolTip.append(HTML_TOOLTIP_TAG_OPEN);
            toolTip.append("Target: " + m_deviceZ);
            toolTip.append(HTML_TOOLTIP_TAG_END);

            return toolTip.toString();
        }

        @Override
        public Item getItem() {
            return new BeanItem<NCSPathEdge>(this);
        }

    }

    @Override
    public String getNamespace() {
        return "ncsPath";
    }

    @Override
    public boolean contributesTo(String namespace) {
        return "nodes".equals(namespace);
    }

    @Override
    public Edge getEdge(String namespace, String id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Edge getEdge(EdgeRef reference) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<Edge> getEdges(Criteria... criteria) {
        for (Criteria criterium : criteria) {
            try {
                return ((NCSServicePathCriteria)criterium).getEdges();
            } catch (ClassCastException e) {}
        }
        return Collections.<Edge>emptyList();
    }

    @Override
    public List<Edge> getEdges(Collection<? extends EdgeRef> references) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void addEdgeListener(EdgeListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeEdgeListener(EdgeListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void clearEdges() {
        // TODO Auto-generated method stub

    }

    @Override
    public int getEdgeTotalCount() {
        return getEdges().size();
    }

}
