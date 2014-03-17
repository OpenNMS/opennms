package org.opennms.netmgt.config.agents;

import java.net.InetAddress;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.network.InetAddressXmlAdapter;

@XmlRootElement(name="agent")
public class AgentResponse {
    private InetAddress m_address;
    private Integer m_port;
    private String m_serviceName;
    private Map<String,String> m_parameters;

    public AgentResponse() {
    }

    public AgentResponse(final InetAddress address, final Integer port, final String serviceName, final Map<String,String> parameters) {
        m_address = address;
        m_port = port;
        m_serviceName = serviceName;
        m_parameters = parameters;
    }

    @XmlElement(name="address")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    public InetAddress getAddress() {
        return m_address;
    }
    @XmlElement(name="port")
    public Integer getPort() {
        return m_port;
    }
    @XmlElement(name="serviceName")
    public String getServiceName() {
        return m_serviceName;
    }
    @XmlElementWrapper(name="parameters")
    @XmlElement(name="parameter")
    public Map<String,String> getParameters() {
        return m_parameters;
    }

    @Override
    public String toString() {
        return "AgentResponse [address=" + m_address + ", port=" + m_port + ", serviceName=" + m_serviceName + ", parameters=" + m_parameters + "]";
    }
}