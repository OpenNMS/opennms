package org.opennms.features.topology.plugins.ncs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    
    private static final String HTML_TOOLTIP_TAG_OPEN = "<p>";
    private static final String HTML_TOOLTIP_TAG_END  = "</p>";
    
    public static class NCSServicePathCriteria extends ArrayList<Edge> implements Criteria {
        private static final long serialVersionUID = 5833760704861282509L;

        public NCSServicePathCriteria(List<Edge> edges) {
            super(edges);
        }
        
        @Override
        public ElementType getType() {
            return ElementType.EDGE;
        }

        @Override
        public String getNamespace() {
            return "ncsPath";
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
            StringBuffer toolTip = new StringBuffer();

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
    public String getEdgeNamespace() {
        // TODO Auto-generated method stub
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
    public boolean matches(EdgeRef edgeRef, Criteria criteria) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<Edge> getEdges(Criteria criteria) {
        NCSServicePathCriteria crit = (NCSServicePathCriteria) criteria;
        return crit;
    }

    @Override
    public List<Edge> getEdges() {
        throw new UnsupportedOperationException("Not implemented");
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

}
