package org.opennms.netmgt.poller.remote.support;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.poller.PollStatus;

@XmlRootElement(name="poll-result")
@XmlAccessorType(XmlAccessType.NONE)
public class PollResult implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name="service-name")
    private String m_service;

    @XmlAttribute(name="service-id")
    private Integer m_serviceId;

    @XmlAttribute(name="node-label")
    private String m_nodeLabel;

    @XmlAttribute(name="node-id")
    private Integer m_nodeId;

    @XmlAttribute(name="ip-address")
    private String m_ipAddress;

    @XmlElement(name="poll-status")
    private PollStatus m_status;

    public PollResult() {
    }

    public PollResult(final String serviceName, final Integer serviceId, final String nodeLabel, final Integer nodeId, final String ipAddress, final PollStatus status) {
        m_service = serviceName;
        m_serviceId = serviceId;
        m_nodeLabel = nodeLabel;
        m_nodeId = nodeId;
        m_ipAddress = ipAddress;
        m_status = status;
    }

    public String getServiceName() {
        return m_service;
    }

    public Integer getServiceId() {
        return m_serviceId;
    }

    public String getNodeLabel() {
        return m_nodeLabel;
    }

    public Integer getNodeId() {
        return m_nodeId;
    }

    public String getIpAddress() {
        return m_ipAddress;
    }

    public PollStatus getPollStatus() {
        return m_status;
    }

    public boolean isUp() {
        if (m_status != null) {
            return m_status.isUp();
        }
        return true;
    }
}
