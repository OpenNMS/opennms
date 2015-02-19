package org.opennms.web.category;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.xml.rtc.Node;

@XmlRootElement(name="node")
public class AvailabilityNode {
    @XmlAttribute(name="id")
    private final Long m_nodeId;

    @XmlAttribute(name="value")
    private final Double m_value;

    @XmlAttribute(name="service-count")
    private final Long m_serviceCount;

    @XmlAttribute(name="service-down-count")
    private final Long m_serviceDownCount;

    public AvailabilityNode() {
        m_nodeId = -1l;
        m_value = -1d;
        m_serviceCount = 0l;
        m_serviceDownCount = 0l;
    }

    public AvailabilityNode(final Node n) {
        m_nodeId = n.getNodeid();
        m_value = n.getNodevalue();
        m_serviceCount = n.getNodesvccount();
        m_serviceDownCount = n.getNodesvcdowncount();
    }

    public Long getId() {
        return m_nodeId;
    }

}
