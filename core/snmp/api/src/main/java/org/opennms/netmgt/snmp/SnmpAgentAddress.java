package org.opennms.netmgt.snmp;

import java.net.InetAddress;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.springframework.util.Assert;

public final class SnmpAgentAddress {
    private final InetAddress m_address;
    private final Integer m_port;
    private int m_hashCode = 0;

    public SnmpAgentAddress(final InetAddress agentAddress, final Integer agentPort) {
        Assert.notNull(agentPort);
        m_address = agentAddress;
        m_port = agentPort;
        
        m_hashCode = new HashCodeBuilder(7, 15)
            .append(m_address)
            .append(m_port)
            .toHashCode();
    }

    public InetAddress getAddress() {
        return m_address;
    }
    
    public Integer getPort() {
        return m_port;
    }
    
    public boolean equals(final Object obj) {
        if (!(obj instanceof SnmpAgentAddress)) return false;
        final SnmpAgentAddress that = (SnmpAgentAddress)obj;
        return new EqualsBuilder()
            .append(this.getAddress(), that.getAddress())
            .append(this.getPort(), that.getPort())
            .isEquals();
    }
    
    public int hashCode() {
        return m_hashCode;
    }
    
    public String toString() {
    	return InetAddressUtils.str(m_address) + ":" + m_port;
    }
}