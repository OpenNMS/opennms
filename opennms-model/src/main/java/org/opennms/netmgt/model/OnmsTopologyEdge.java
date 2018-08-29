package org.opennms.netmgt.model;

import org.opennms.netmgt.model.topology.Topology;

public class OnmsTopologyEdge extends OnmsTopologyRef {

    private static final String HTML_TOOLTIP_TAG_OPEN = "<p>";
    private static final String HTML_TOOLTIP_TAG_END  = "</p>";

    public static OnmsTopologyEdge create(OnmsTopologyVertex source, OnmsTopologyVertex target) {
        if (source !=  null && target != null && !source.getId().equals(target.getId())) {
            return new OnmsTopologyEdge(source.getId()+":"+target.getId(), source, target);
        }
        
        return null;
    }
        
    private final OnmsTopologyVertex m_source;
    private final OnmsTopologyVertex m_target;
       
    private String m_sourcePort;
    private String m_targetPort;

    private String m_sourceAddr;
    private String m_targetAddr;

    private String m_speed;
    
    private Integer m_sourceIfIndex;
    private Integer m_targetIfIndex;
    private Topology.ProtocolSupported m_discoveredBy;

    private OnmsTopologyEdge(String id, OnmsTopologyVertex source, OnmsTopologyVertex target) {
        super(id);
        m_source = source;
        m_target = target;
    }

    public String getSourcePort() {
        return m_sourcePort;
    }

    public void setSourcePort(String sourcePort) {
        m_sourcePort = sourcePort;
    }

    public String getTargetPort() {
        return m_targetPort;
    }

    public void setTargetPort(String targetPort) {
        m_targetPort = targetPort;
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

    public void setTargetAddr(String targetAddr) {
        m_targetAddr = targetAddr;
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

    public OnmsTopologyVertex getSource() {
        return m_source;
    }

    public OnmsTopologyVertex getTarget() {
        return m_target;
    }

    public String  getTooltipText() {       
        final StringBuilder tooltipText = new StringBuilder();
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append("discovery by: ");
        tooltipText.append(m_discoveredBy.toString());
        tooltipText.append(HTML_TOOLTIP_TAG_END);
    
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append(m_source.getNode().getLabel());
        if (m_sourcePort != null ) {
            tooltipText.append("(");
            tooltipText.append(m_sourcePort);
            tooltipText.append(")");
        }
        if (m_sourceAddr != null ) {
            tooltipText.append("(");
            tooltipText.append(m_sourceAddr);
            tooltipText.append(")");
        }
        tooltipText.append(HTML_TOOLTIP_TAG_END);
        
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append(m_target.getNode().getLabel());
        if (m_targetPort != null) {
            tooltipText.append("(");
            tooltipText.append(m_targetPort);
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

    public Topology.ProtocolSupported getDiscoveredBy() {
        return m_discoveredBy;
    }

    public void setDiscoveredBy(Topology.ProtocolSupported discoveredBy) {
        m_discoveredBy = discoveredBy;
    }



    
}
