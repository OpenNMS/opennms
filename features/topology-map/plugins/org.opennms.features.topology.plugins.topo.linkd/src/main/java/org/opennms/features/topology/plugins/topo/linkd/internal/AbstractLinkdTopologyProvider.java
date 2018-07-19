/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.text.DecimalFormat;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionAware;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractSearchProvider;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.SimpleConnector;
import org.opennms.features.topology.api.topo.SimpleLeafVertex;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.topology.BridgePort;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLinkdTopologyProvider extends AbstractTopologyProvider implements GraphProvider,  SearchProvider {

    abstract class LinkDetail<K, L> {
        private final String m_id;
        private final Vertex m_source;
        private final K m_sourceLink;
        private final Vertex m_target;
        private final L m_targetLink;

        public LinkDetail(String id, Vertex source, K sourceLink, Vertex target, L targetLink){
            m_id = id;
            m_source = source;
            m_sourceLink = sourceLink;
            m_target = target;
            m_targetLink = targetLink;
        }

        public abstract int hashCode();

        public abstract boolean equals(Object obj);

        public abstract Integer getSourceIfIndex();

        public abstract Integer getTargetIfIndex();

        public abstract String getType();

        public String getId() {
            return m_id;
        }

        public Vertex getSource() {
            return m_source;
        }

        public Vertex getTarget() {
            return m_target;
        }

        public K getSourceLink() {
            return m_sourceLink;
        }

        public L getTargetLink() {
            return m_targetLink;
        }
    }

    public static final String TOPOLOGY_NAMESPACE_LINKD = "nodes";
    protected static final String HTML_TOOLTIP_TAG_OPEN = "<p>";
    protected static final String HTML_TOOLTIP_TAG_END  = "</p>";
    /**
     * Always print at least one digit after the decimal point,
     * and at most three digits after the decimal point.
     */
    protected static final DecimalFormat s_oneDigitAfterDecimal = new DecimalFormat("0.0##");
    /**
     * Print no digits after the decimal point (heh, nor a decimal point).
     */
    protected static final DecimalFormat s_noDigitsAfterDecimal = new DecimalFormat("0");

    protected static final EnumMap<OnmsNode.NodeType, String> m_nodeStatusMap;

    static final String getDefaultEdgeId(int sourceId,int targetId) {
        return Math.min(sourceId, targetId) + "|" + Math.max(sourceId, targetId);
    }

    static {
        m_nodeStatusMap = new EnumMap<>(OnmsNode.NodeType.class);
        m_nodeStatusMap.put(OnmsNode.NodeType.ACTIVE, "Active");
        m_nodeStatusMap.put(OnmsNode.NodeType.UNKNOWN, "Unknown");
        m_nodeStatusMap.put(OnmsNode.NodeType.DELETED, "Deleted");
    }

    private final boolean m_aclEnabled;

    private SelectionAware selectionAwareDelegate = new EnhancedLinkdSelectionAware();
    private static Logger LOG = LoggerFactory.getLogger(EnhancedLinkdTopologyProvider.class);

    protected AbstractLinkdTopologyProvider() {
        super(TOPOLOGY_NAMESPACE_LINKD);
        String aclsProp = System.getProperty("org.opennms.web.aclsEnabled");
        m_aclEnabled = aclsProp != null ? aclsProp.equals("true") : false;
    }

    /**
     * Method used to convert an integer bits-per-second value to a more
     * readable vale using commonly recognized abbreviation for network
     * interface speeds. Feel free to expand it as necessary to accommodate
     * different values.
     *
     * @param ifSpeed
     *            The bits-per-second value to be converted into a string
     *            description
     * @return A string representation of the speed (&quot;100 Mbps&quot; for
     *         example)
     */
    public static String getHumanReadableIfSpeed(long ifSpeed) {
        DecimalFormat formatter;
        double displaySpeed;
        String units;

        if (ifSpeed >= 1000000000L) {
            if ((ifSpeed % 1000000000L) == 0) {
                formatter = s_noDigitsAfterDecimal;
            } else {
                formatter = s_oneDigitAfterDecimal;
            }
            displaySpeed = ((double) ifSpeed) / 1000000000.0;
            units = "Gbps";
        } else if (ifSpeed >= 1000000L) {
            if ((ifSpeed % 1000000L) == 0) {
                formatter = s_noDigitsAfterDecimal;
            } else {
                formatter = s_oneDigitAfterDecimal;
            }
            displaySpeed = ((double) ifSpeed) / 1000000.0;
            units = "Mbps";
        } else if (ifSpeed >= 1000L) {
            if ((ifSpeed % 1000L) == 0) {
                formatter = s_noDigitsAfterDecimal;
            } else {
                formatter = s_oneDigitAfterDecimal;
            }
            displaySpeed = ((double) ifSpeed) / 1000.0;
            units = "kbps";
        } else {
            formatter = s_noDigitsAfterDecimal;
            displaySpeed = (double) ifSpeed;
            units = "bps";
        }

        return formatter.format(displaySpeed) + " " + units;
    }

    public static String getIconName(String nodeSysObjectId) {
        if (nodeSysObjectId == null) {
            return "linkd.system";
        }
        if (nodeSysObjectId.startsWith(".")) {
            return "linkd.system.snmp" + nodeSysObjectId;
        }
        return "linkd.system.snmp." + nodeSysObjectId;
    }

    public static OnmsSnmpInterface getByNodeIdAndIfIndex(Integer ifIndex, Vertex source, Map<Integer,List<OnmsSnmpInterface>> snmpmap) {
        if(source.getId() != null && StringUtils.isNumeric(source.getId()) && ifIndex != null 
                && snmpmap.containsKey(Integer.parseInt(source.getId()))) {
            for (OnmsSnmpInterface snmpiface: snmpmap.get(Integer.parseInt(source.getId()))) {
                if (ifIndex.intValue() == snmpiface.getIfIndex().intValue())
                    return snmpiface;
            }
        }
        return null;
    }

    public static AbstractVertex createLinkdVertex(OnmsNode sourceNode, OnmsIpInterface primary) {
        AbstractVertex vertex = new SimpleLeafVertex(TOPOLOGY_NAMESPACE_LINKD, sourceNode.getId().toString(), 0, 0);
        vertex.setIconKey(getIconName(sourceNode.getSysObjectId()));
        vertex.setLabel(sourceNode.getLabel());
        vertex.setIpAddress(InetAddressUtils.str(primary.getIpAddress()));
        vertex.setNodeID(sourceNode.getId());
        vertex.setTooltipText(getNodeTooltipDefaultText(InetAddressUtils.str(primary.getIpAddress()),
                                                                sourceNode.getLabel(),
                                                                primary.isManaged(),
                                                                sourceNode.getSysLocation(),
                                                                sourceNode.getType())
        );
        return vertex;
    }

    public static String getEdgeTooltipText(BridgePort sourcelink,
            Vertex source, Vertex target,
            List<OnmsIpInterface> targetInterfaces,
            Map<Integer, List<OnmsSnmpInterface>> snmpmap, String mac) {
        final StringBuilder tooltipText = new StringBuilder();
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append("Bridge Layer2");
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        OnmsSnmpInterface sourceInterface = getByNodeIdAndIfIndex(sourcelink.getBridgePortIfIndex(), target,snmpmap);
        
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append(source.getLabel());
        if (sourceInterface != null) {
            tooltipText.append("(");
            tooltipText.append(sourceInterface.getIfName());
            tooltipText.append(")");
        }
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append(target.getLabel());
        tooltipText.append("(");
        tooltipText.append(mac);
        tooltipText.append(")");
        tooltipText.append("(");
        if (targetInterfaces.size() == 1) {
            tooltipText.append(InetAddressUtils.str(targetInterfaces.get(0).getIpAddress()));
        } else if (targetInterfaces.size() > 1) {
            tooltipText.append("Multiple ip Addresses ");
        } else {
            tooltipText.append("No ip Address found");
        }
        tooltipText.append(")");
        tooltipText.append(HTML_TOOLTIP_TAG_END);        

        if ( sourceInterface != null) {
            if (sourceInterface.getIfSpeed() != null) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append(getHumanReadableIfSpeed(sourceInterface.getIfSpeed()));
                tooltipText.append(HTML_TOOLTIP_TAG_END);
            }
        }


        return tooltipText.toString();
    }

    public static String getEdgeTooltipText(String mac, Vertex target, List<OnmsIpInterface> ipifaces) {
        final StringBuilder tooltipText = new StringBuilder();
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append("Bridge Layer2");
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append(target.getLabel());
        tooltipText.append("(");
        tooltipText.append(mac);
        tooltipText.append(")");
        tooltipText.append("(");
        if (ipifaces.size() == 1) {
            tooltipText.append(InetAddressUtils.str(ipifaces.get(0).getIpAddress()));
        } else if (ipifaces.size() > 1) {
            tooltipText.append("Multiple ip Addresses ");
        } else {
            tooltipText.append("No ip Address found");
        }
        tooltipText.append(")");
        tooltipText.append(HTML_TOOLTIP_TAG_END);        
        
        return tooltipText.toString();
    }


    public static String getEdgeTooltipText(BridgePort port, Vertex target, Map<Integer,List<OnmsSnmpInterface>> snmpmap) {
        final StringBuilder tooltipText = new StringBuilder();
        OnmsSnmpInterface targetInterface = getByNodeIdAndIfIndex(port.getBridgePortIfIndex(), target,snmpmap);
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append("Bridge Layer2");
        tooltipText.append(HTML_TOOLTIP_TAG_END);
        
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append(target.getLabel());
        if (targetInterface != null) {
            tooltipText.append("(");
            tooltipText.append(targetInterface.getIfName());
            tooltipText.append(")");
        }
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        if ( targetInterface != null) {
            if (targetInterface.getIfSpeed() != null) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append(getHumanReadableIfSpeed(targetInterface.getIfSpeed()));
                tooltipText.append(HTML_TOOLTIP_TAG_END);
            }
        }
        
        return tooltipText.toString();
    }

    public static String getEdgeTooltipText(LinkDetail<?,?> linkDetail,Map<Integer,List<OnmsSnmpInterface>> snmpmap) {

        final StringBuilder tooltipText = new StringBuilder();
        Vertex source = linkDetail.getSource();
        Vertex target = linkDetail.getTarget();
        OnmsSnmpInterface sourceInterface = getByNodeIdAndIfIndex(linkDetail.getSourceIfIndex(), source,snmpmap);
        OnmsSnmpInterface targetInterface = getByNodeIdAndIfIndex(linkDetail.getTargetIfIndex(), target,snmpmap);

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append(linkDetail.getType());
        String layerText = " Layer 2";
        if (sourceInterface != null && targetInterface != null) {
            final List<OnmsIpInterface> sourceNonLoopback = sourceInterface.getIpInterfaces().stream().filter(iface -> {
                return !iface.getNetMask().isLoopbackAddress();
            }).collect(Collectors.toList());
            final List<OnmsIpInterface> targetNonLoopback = targetInterface.getIpInterfaces().stream().filter(iface -> {
                return !iface.getNetMask().isLoopbackAddress();
            }).collect(Collectors.toList());

            if (!sourceNonLoopback.isEmpty() && !targetNonLoopback.isEmpty()) {
                // if both the source and target have non-loopback IP interfaces, assume this is a layer3 edge
                layerText = " Layer3/Layer2";
            }
        }
        tooltipText.append(layerText);
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append( source.getLabel());
        if (sourceInterface != null ) {
            tooltipText.append("(");
            tooltipText.append(sourceInterface.getIfName());
            tooltipText.append(")");
        }
        tooltipText.append(HTML_TOOLTIP_TAG_END);
        
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append(target.getLabel());
        if (targetInterface != null) {
            tooltipText.append("(");
            tooltipText.append(targetInterface.getIfName());
            tooltipText.append(")");
        }
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        if ( targetInterface != null) {
            if (targetInterface.getIfSpeed() != null) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append(getHumanReadableIfSpeed(targetInterface.getIfSpeed()));
                tooltipText.append(HTML_TOOLTIP_TAG_END);
            }
        } else if (sourceInterface != null) {
            if (sourceInterface.getIfSpeed() != null) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append(getHumanReadableIfSpeed(sourceInterface.getIfSpeed()));
                tooltipText.append(HTML_TOOLTIP_TAG_END);
            }
        }
        return tooltipText.toString();
    }

    public static String getNodeTooltipDefaultText(String ip, String label, boolean isManaged, String location,NodeType nodeType) {
        final StringBuilder tooltipText = new StringBuilder();
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append(label);
        tooltipText.append(": ");
        if (ip != null) {
            tooltipText.append("(");
            tooltipText.append(ip);
            tooltipText.append(")");
        }
        tooltipText.append("(");
        tooltipText.append(m_nodeStatusMap.get(nodeType));
        if (ip != null) {
            if (isManaged) {
                tooltipText.append( "/Managed");
            } else {
                tooltipText.append( "/Unmanaged");
            }
        }
        tooltipText.append(")");
        tooltipText.append(HTML_TOOLTIP_TAG_END);
        
        if (location != null && location.length() > 0) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append(location);
                tooltipText.append(HTML_TOOLTIP_TAG_END);
        }
        return tooltipText.toString();

    }

    //Search Provider methods
    @Override
    public String getSearchProviderNamespace() {
        return TOPOLOGY_NAMESPACE_LINKD;
    }
    
    @Override
    public void onFocusSearchResult(SearchResult searchResult, OperationContext operationContext) {

    }

    @Override
    public void onDefocusSearchResult(SearchResult searchResult, OperationContext operationContext) {

    }

    @Override
    public boolean supportsPrefix(String searchPrefix) {
        return AbstractSearchProvider.supportsPrefix("nodes=", searchPrefix);
    }

    @Override
    public Set<VertexRef> getVertexRefsBy(SearchResult searchResult, GraphContainer container) {
        LOG.debug("SearchProvider->getVertexRefsBy: called with search result: '{}'", searchResult);
        org.opennms.features.topology.api.topo.Criteria criterion = findCriterion(searchResult.getId(), container);

        Set<VertexRef> vertices = ((VertexHopCriteria)criterion).getVertices();
        LOG.debug("SearchProvider->getVertexRefsBy: found '{}' vertices.", vertices.size());

        return vertices;
    }

    @Override
    public void addVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        LOG.debug("SearchProvider->addVertexHopCriteria: called with search result: '{}'", searchResult);
        VertexHopCriteria criterion = LinkdHopCriteriaFactory.createCriteria(searchResult.getId(), searchResult.getLabel());
        container.addCriteria(criterion);
        LOG.debug("SearchProvider->addVertexHop: adding hop criteria {}.", criterion);
        logCriteriaInContainer(container);
    }

    @Override
    public void removeVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        LOG.debug("SearchProvider->removeVertexHopCriteria: called with search result: '{}'", searchResult);

        Criteria criterion = findCriterion(searchResult.getId(), container);

        if (criterion != null) {
            LOG.debug("SearchProvider->removeVertexHopCriteria: found criterion: {} for searchResult {}.", criterion, searchResult);
            container.removeCriteria(criterion);
        } else {
            LOG.debug("SearchProvider->removeVertexHopCriteria: did not find criterion for searchResult {}.", searchResult);
        }

        logCriteriaInContainer(container);
    }

    @Override
    public void onCenterSearchResult(SearchResult searchResult, GraphContainer graphContainer) {
        LOG.debug("SearchProvider->onCenterSearchResult: called with search result: '{}'", searchResult);
    }

    @Override
    public void onToggleCollapse(SearchResult searchResult, GraphContainer graphContainer) {
        LOG.debug("SearchProvider->onToggleCollapse: called with search result: '{}'", searchResult);
    }

    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType type) {
       return selectionAwareDelegate.getSelection(selectedVertices, type);
    }

    @Override
    public boolean contributesTo(ContentType type) {
        return selectionAwareDelegate.contributesTo(type);
    }

    private org.opennms.features.topology.api.topo.Criteria findCriterion(String resultId, GraphContainer container) {

        org.opennms.features.topology.api.topo.Criteria[] criteria = container.getCriteria();
        for (org.opennms.features.topology.api.topo.Criteria criterion : criteria) {
            if (criterion instanceof LinkdHopCriteria ) {
                String id = ((LinkdHopCriteria) criterion).getId();
                if (id.equals(resultId)) {
                    return criterion;
                }
            }
        }
        return null;
    }

    private void logCriteriaInContainer(GraphContainer container) {
        Criteria[] criteria = container.getCriteria();
        LOG.debug("SearchProvider->addVertexHopCriteria: there are now {} criteria in the GraphContainer.", criteria.length);
        for (Criteria crit : criteria) {
            LOG.debug("SearchProvider->addVertexHopCriteria: criterion: '{}' is in the GraphContainer.", crit);
        }
    }


    public boolean isAclEnabled() {
        return m_aclEnabled;
    }

    protected final Vertex getOrCreateVertex(OnmsNode node,OnmsIpInterface primary, boolean add) {
        Vertex source = getVertex(getNamespace(), node.getNodeId());
        if (source == null) {
            source = createLinkdVertex(node,primary); 
            if (add) {
                addVertices(source);
            }
        }        
        return source;
    }
    
    protected final LinkdEdge connectCloudMacVertices(String targetmac, VertexRef sourceRef, VertexRef targetRef,String nameSpace) {
        SimpleConnector source = new SimpleConnector(sourceRef.getNamespace(), sourceRef.getId()+"-"+targetRef.getId()+"-connector", sourceRef);
        SimpleConnector target = new SimpleConnector(targetRef.getNamespace(), targetRef.getId()+"-"+sourceRef.getId()+"-connector", targetRef);

        LinkdEdge edge = new LinkdEdge(nameSpace, targetRef.getId()+":"+targetmac, source, target);
        edge.setTargetEndPoint(targetmac);
        addEdges(edge);
        
        return edge;
    }

    protected final LinkdEdge connectVertices(BridgePort targetport, VertexRef sourceRef, VertexRef targetRef,String nameSpace) {
        SimpleConnector source = new SimpleConnector(sourceRef.getNamespace(), sourceRef.getId()+"-"+targetRef.getId()+"-connector", sourceRef);
        SimpleConnector target = new SimpleConnector(targetRef.getNamespace(), targetRef.getId()+"-"+sourceRef.getId()+"-connector", targetRef);

        LinkdEdge edge = new LinkdEdge(nameSpace, targetRef.getId()+":"+targetport.getBridgePort(), source, target);
        edge.setTargetNodeid(targetport.getNodeId());
        if (targetport.getBridgePortIfIndex() != null)
            edge.setTargetEndPoint(String.valueOf(targetport.getBridgePortIfIndex()));
        addEdges(edge);
        
        return edge;
    }

    protected final LinkdEdge connectVertices(BridgeMacLink link, VertexRef sourceRef, VertexRef targetRef,String nameSpace) {
        SimpleConnector source = new SimpleConnector(sourceRef.getNamespace(), sourceRef.getId()+"-"+link.getId()+"-connector", sourceRef);
        SimpleConnector target = new SimpleConnector(targetRef.getNamespace(), targetRef.getId()+"-"+link.getId()+"-connector", targetRef);

        LinkdEdge edge = new LinkdEdge(nameSpace, String.valueOf(link.getId()), source, target);
        edge.setSourceNodeid(link.getNode().getId());
        if (link.getBridgePortIfIndex() != null)
            edge.setSourceEndPoint(String.valueOf(link.getBridgePortIfIndex()));
        edge.setTargetEndPoint(String.valueOf(link.getMacAddress()));
        addEdges(edge);
        
        return edge;
    }
    
    protected final LinkdEdge connectVertices(LinkDetail<?,?> linkdetail, String nameSpace) {
        SimpleConnector source = new SimpleConnector(linkdetail.getSource().getNamespace(), linkdetail.getSource().getId()+"-"+linkdetail.getId()+"-connector", linkdetail.getSource());
        SimpleConnector target = new SimpleConnector(linkdetail.getTarget().getNamespace(), linkdetail.getTarget().getId()+"-"+linkdetail.getId()+"-connector", linkdetail.getTarget());

        LinkdEdge edge = new LinkdEdge(nameSpace, linkdetail.getId(), source, target);
        try {
            edge.setSourceNodeid(Integer.parseInt(linkdetail.getSource().getId()));
        } catch (NumberFormatException e) {
            
        }
        try {
            edge.setTargetNodeid(Integer.parseInt(linkdetail.getTarget().getId()));
        } catch (NumberFormatException e) {
            
        }
        if (linkdetail.getSourceIfIndex() != null)
            edge.setSourceEndPoint(String.valueOf(linkdetail.getSourceIfIndex()));
        if (linkdetail.getTargetIfIndex() != null)
            edge.setTargetEndPoint(String.valueOf(linkdetail.getTargetIfIndex()));
        addEdges(edge);
        
        return edge;
    }

}
