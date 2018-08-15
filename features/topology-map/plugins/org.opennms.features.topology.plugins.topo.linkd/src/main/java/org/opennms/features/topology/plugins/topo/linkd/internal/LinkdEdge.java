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

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.SimpleConnector;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.plugins.topo.linkd.internal.LinkdTopologyProvider.ProtocolSupported;
import org.opennms.netmgt.model.OnmsSnmpInterface;

public class LinkdEdge extends AbstractEdge implements Edge {

    private static final String HTML_TOOLTIP_TAG_OPEN = "<p>";
    private static final String HTML_TOOLTIP_TAG_END  = "</p>";
    
    public static LinkdEdge create(String id, Vertex sourceV, Vertex targetV, ProtocolSupported discoveredBy) {
        return new LinkdEdge(id, sourceV, targetV,discoveredBy);
    }

    public static LinkdEdge create(String id,
            AbstractVertex sourceV, AbstractVertex targetV,  
            OnmsSnmpInterface sourceinterface, OnmsSnmpInterface targetInterface,
            String sourceAddr, String targetAddr,
            ProtocolSupported discoveredBy) {
        
        SimpleConnector source = new SimpleConnector(LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD, sourceV.getId()+"-"+id+"-connector", sourceV);
        SimpleConnector target = new SimpleConnector(LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD, targetV.getId()+"-"+id+"-connector", targetV);

        LinkdEdge edge = new LinkdEdge(id, source, target,discoveredBy);
        edge.setSourceNodeid(sourceV.getNodeID());
        edge.setTargetNodeid(targetV.getNodeID());
        edge.setSourceLabel(sourceV.getLabel());
        edge.setTargetLabel(targetV.getLabel());
        
        if (sourceinterface != null) {
            edge.setSourceIfIndex(sourceinterface.getIfIndex());
            edge.setSourceIfName(sourceinterface.getIfName());
            if (sourceinterface.getIfSpeed() != null) {
                edge.setSpeed(InetAddressUtils.getHumanReadableIfSpeed(sourceinterface.getIfSpeed()));
            }
        }
        if (targetInterface != null) {
            edge.setTargetIfIndex(targetInterface.getIfIndex());
            edge.setTargetIfName(targetInterface.getIfName());
            if (edge.getSpeed() == null && targetInterface.getIfSpeed() != null) {
                edge.setSpeed(InetAddressUtils.getHumanReadableIfSpeed(targetInterface.getIfSpeed()));
            }

        }
        
        edge.setSourceAddr(sourceAddr);
        edge.setTargetAddr(targetAddr);

        return edge;
    }

    private Integer m_sourceNodeid;
    private Integer m_targetNodeid;

    private String m_sourceLabel;
    private String m_targetLabel;

    private String m_sourceIfName;
    private String m_targetIfName;

    private String m_sourceAddr;
    private String m_targetAddr;

    private String m_speed;
    
    private Integer m_sourceIfIndex;
    private Integer m_targetIfIndex;

    private final ProtocolSupported m_discoveredBy;
    
    private LinkdEdge(String id, Vertex source, Vertex target, ProtocolSupported discoveredBy) {
        super(LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD, id, source, target);
        m_discoveredBy = discoveredBy;
    }

    private LinkdEdge(String id, SimpleConnector source,
            SimpleConnector target, ProtocolSupported discoveredBy) {
        super(LinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD, id, source, target);
        m_discoveredBy = discoveredBy;
    }

    // Constructor to make cloneable easier for sub classes
    private LinkdEdge(LinkdEdge edgeToClone) {
            this(edgeToClone.getId(), edgeToClone.getSource().clone(), edgeToClone.getTarget().clone(),edgeToClone.getDiscoveredBy());
            setSourceLabel(edgeToClone.getSourceLabel());
            setSourceNodeid(edgeToClone.getSourceNodeid());
            setSourceIfIndex(edgeToClone.getSourceIfIndex());
            setSourceIfName(edgeToClone.getSourceIfName());
            setSourceAddr(edgeToClone.getSourceAddr());
            setTargetLabel(edgeToClone.getTargetLabel());
            setTargetNodeid(edgeToClone.getTargetNodeid());
            setTargetIfIndex(edgeToClone.getTargetIfIndex());
            setTargetIfName(edgeToClone.getTargetIfName());
            setTargetAddr(edgeToClone.getTargetAddr());
            setSpeed(edgeToClone.getSpeed());
            setLabel(edgeToClone.getLabel());
            setStyleName(edgeToClone.getStyleName());
            setTooltipText(edgeToClone.getTooltipText());
    }

    @Override
    public LinkdEdge clone() {
            return new LinkdEdge(this);
    }

    @Override
    public String  getTooltipText() {       
        final StringBuilder tooltipText = new StringBuilder();
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append("discovery by: ");
        tooltipText.append(m_discoveredBy.toString());
        tooltipText.append(HTML_TOOLTIP_TAG_END);
    
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append(m_sourceLabel);
        if (m_sourceIfName != null ) {
            tooltipText.append("(");
            tooltipText.append(m_sourceIfName);
            tooltipText.append(")");
        }
        if (m_sourceAddr != null ) {
            tooltipText.append("(");
            tooltipText.append(m_sourceAddr);
            tooltipText.append(")");
        }
        tooltipText.append(HTML_TOOLTIP_TAG_END);
        
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append(m_targetLabel);
        if (m_targetIfName != null) {
            tooltipText.append("(");
            tooltipText.append(m_targetIfName);
            tooltipText.append(")");
        }
        if (m_targetAddr != null ) {
            tooltipText.append("(");
            tooltipText.append(m_targetAddr);
            tooltipText.append(")");
        }
        tooltipText.append(HTML_TOOLTIP_TAG_END);
    
        if ( m_speed != null) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append(m_speed);
                tooltipText.append(HTML_TOOLTIP_TAG_END);
        }
        return tooltipText.toString();
    }

    public String getSourceAddr() {
        return m_sourceAddr;
    }

    public void setSourceAddr(String sourceAddr) {
        m_sourceAddr = sourceAddr;
    }

    public String getTargetAddr() {
        return m_targetAddr;
    }

    public void setTargetAddr(String targetIpAddr) {
        m_targetAddr = targetIpAddr;
    }

    public String getSourceLabel() {
        return m_sourceLabel;
    }


    public void setSourceLabel(String sourceLabel) {
        m_sourceLabel = sourceLabel;
    }


    public String getTargetLabel() {
        return m_targetLabel;
    }


    public void setTargetLabel(String targetLabel) {
        m_targetLabel = targetLabel;
    }


    public String getSourceIfName() {
        return m_sourceIfName;
    }


    public void setSourceIfName(String sourceIfName) {
        m_sourceIfName = sourceIfName;
    }


    public String getTargetIfName() {
        return m_targetIfName;
    }


    public void setTargetIfName(String targetIfName) {
        m_targetIfName = targetIfName;
    }


    public String getSpeed() {
        return m_speed;
    }

    public void setSpeed(String speed) {
        m_speed = speed;
    }


    public Integer getSourceIfIndex() {
        return m_sourceIfIndex;
    }

    public void setSourceIfIndex(Integer sourceIfIndex) {
        m_sourceIfIndex = sourceIfIndex;
    }

    public Integer getTargetIfIndex() {
        return m_targetIfIndex;
    }

    public void setTargetIfIndex(Integer targetIfIndex) {
        m_targetIfIndex = targetIfIndex;
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
    
}
