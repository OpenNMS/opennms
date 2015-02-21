package org.opennms.web.category;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.rtc.Node;

@XmlRootElement(name="node")
public class AvailabilityNode {
    @XmlAttribute(name="id")
    private Long m_nodeId;

    @XmlAttribute(name="availability")
    private Double m_availability;

    @XmlAttribute(name="service-count")
    private Long m_serviceCount;

    @XmlAttribute(name="service-down-count")
    private Long m_serviceDownCount;

    @XmlElementWrapper(name="ipinterfaces")
    @XmlElement(name="ipinterface")
    private final List<AvailabilityIpInterface> m_ipInterfaces = new ArrayList<>();

    public AvailabilityNode() {
        m_nodeId = -1l;
        m_availability = -1d;
        m_serviceCount = 0l;
        m_serviceDownCount = 0l;
    }

    public AvailabilityNode(final Node node) {
        m_nodeId = node.getNodeid();
        m_availability = node.getNodevalue();
        m_serviceCount = node.getNodesvccount();
        m_serviceDownCount = node.getNodesvcdowncount();
    }

    public AvailabilityNode(final OnmsNode node, final double availability) {
        m_nodeId = node.getId().longValue();
        m_availability = availability;
        m_serviceCount = 0l;
        m_serviceDownCount = 0l;
        for (final OnmsIpInterface iface : node.getIpInterfaces()) {
            for (final OnmsMonitoredService svc : iface.getMonitoredServices()) {
                m_serviceCount++;
                if (svc.isDown()) {
                    m_serviceDownCount++;
                }
            }
        }
    }

    public Long getId() {
        return m_nodeId;
    }

    public void addIpInterface(final AvailabilityIpInterface iface) {
        m_ipInterfaces.add(iface);
    }

}
