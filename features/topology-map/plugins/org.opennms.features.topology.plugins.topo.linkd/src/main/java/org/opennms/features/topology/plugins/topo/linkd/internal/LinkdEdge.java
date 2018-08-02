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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.SimpleConnector;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.plugins.topo.linkd.internal.LinkdTopologyProvider.ProtocolSupported;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.topology.BridgePort;

public class LinkdEdge extends AbstractEdge implements Edge {

    static final String getDefaultEdgeId(int sourceId,int targetId) {
        return Math.min(sourceId, targetId) + "|" + Math.max(sourceId, targetId);
    }

    private static final String HTML_TOOLTIP_TAG_OPEN = "<p>";
    private static final String HTML_TOOLTIP_TAG_END  = "</p>";

    private static OnmsSnmpInterface getByNodeIdAndIfIndex(Integer ifIndex, Vertex source, Map<Integer,List<OnmsSnmpInterface>> snmpmap) {
        if(source.getId() != null && StringUtils.isNumeric(source.getId()) && ifIndex != null 
                && snmpmap.containsKey(Integer.parseInt(source.getId()))) {
            for (OnmsSnmpInterface snmpiface: snmpmap.get(Integer.parseInt(source.getId()))) {
                if (ifIndex.intValue() == snmpiface.getIfIndex().intValue())
                    return snmpiface;
            }
        }
        return null;
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
                tooltipText.append(InetAddressUtils.getHumanReadableIfSpeed(sourceInterface.getIfSpeed()));
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
                tooltipText.append(InetAddressUtils.getHumanReadableIfSpeed(targetInterface.getIfSpeed()));
                tooltipText.append(HTML_TOOLTIP_TAG_END);
            }
        }
        
        return tooltipText.toString();
    }

    public static String getEdgeTooltipText(LinkdEdgeDetail<?,?> linkDetail,Map<Integer,List<OnmsSnmpInterface>> snmpmap) {

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
                tooltipText.append(InetAddressUtils.getHumanReadableIfSpeed(targetInterface.getIfSpeed()));
                tooltipText.append(HTML_TOOLTIP_TAG_END);
            }
        } else if (sourceInterface != null) {
            if (sourceInterface.getIfSpeed() != null) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append(InetAddressUtils.getHumanReadableIfSpeed(sourceInterface.getIfSpeed()));
                tooltipText.append(HTML_TOOLTIP_TAG_END);
            }
        }
        return tooltipText.toString();
    }

    private Integer m_sourceNodeid;
    private Integer m_targetNodeid;

    private String m_sourceEndPoint;
    private String m_targetEndPoint;
    
    private final ProtocolSupported m_discoveredBy;
    
    public LinkdEdge(String id, Vertex source, Vertex target, ProtocolSupported discoveredBy) {
        super(LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD, id, source, target);
        m_discoveredBy = discoveredBy;
    }

    public LinkdEdge(String id, SimpleConnector source,
            SimpleConnector target, ProtocolSupported discoveredBy) {
        super(LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD, id, source, target);
        m_discoveredBy = discoveredBy;
    }

    // Constructor to make cloneable easier for sub classes
    protected LinkdEdge(LinkdEdge edgeToClone) {
            this(edgeToClone.getId(), edgeToClone.getSource().clone(), edgeToClone.getTarget().clone(),edgeToClone.getDiscoveredBy());
            setSourceNodeid(edgeToClone.getSourceNodeid());
            setTargetNodeid(edgeToClone.getTargetNodeid());
            setSourceEndPoint(edgeToClone.getSourceEndPoint());
            setTargetEndPoint(edgeToClone.getTargetEndPoint());
            setLabel(edgeToClone.getLabel());
            setStyleName(edgeToClone.getStyleName());
            setTooltipText(edgeToClone.getTooltipText());
    }

    @Override
    public LinkdEdge clone() {
            return new LinkdEdge(this);
    }

    public ProtocolSupported getDiscoveredBy() {
        return m_discoveredBy;
    }

    public Integer getSourceNodeid() {
        return m_sourceNodeid;
    }

    public void setSourceNodeid(Integer sourceNodeid) {
        m_sourceNodeid = sourceNodeid;
    }

    public Integer getTargetNodeid() {
        return m_targetNodeid;
    }

    public void setTargetNodeid(Integer targetNodeid) {
        m_targetNodeid = targetNodeid;
    }

    public String getSourceEndPoint() {
        return m_sourceEndPoint;
    }

    public void setSourceEndPoint(String sourceEndPoint) {
        m_sourceEndPoint = sourceEndPoint;
    }

    public String getTargetEndPoint() {
        return m_targetEndPoint;
    }

    public void setTargetEndPoint(String targetEndPoint) {
        m_targetEndPoint = targetEndPoint;
    }

    public boolean containsVertexEndPoint(String vertexRef, String endpointRef) {
        if (vertexRef == null)
            return false;
        if (endpointRef == null)
            return false;
        if (getSource() != null && getSourceEndPoint() != null 
                && getSource().getVertex().getId().equals(vertexRef) && getSourceEndPoint().equals(endpointRef))
            return true;
        if (getTarget() != null && getTargetEndPoint() != null 
                && getTarget().getVertex().getId().equals(vertexRef) && getTargetEndPoint().equals(endpointRef))
            return true;
        return false;
    }
}
