package org.opennms.netmgt.dao.jdbc.snmpif;

import org.opennms.netmgt.model.OnmsSnmpInterface;

public class SnmpInterfaceId {
    private Integer m_nodeId;
    private String m_ipAddr;
    private Integer m_ifIndex;
    
    public SnmpInterfaceId(Integer nodeId, String ipAddr, Integer ifIndex) {
        m_nodeId = nodeId;
        m_ipAddr = ipAddr;
        m_ifIndex = ifIndex;
    }
    
    public SnmpInterfaceId(OnmsSnmpInterface iface) {
        this(iface.getNode().getId(), iface.getIpAddress(), iface.getIfIndex());
    }

    public boolean equals(Object obj) {
        if (obj instanceof SnmpInterfaceId) {
            SnmpInterfaceId key = (SnmpInterfaceId) obj;
            return (
                    m_nodeId.equals(key.m_nodeId)
                    && m_ipAddr.equals(key.m_ipAddr)
                    && (m_ifIndex == null ? key.m_ifIndex == null : m_ifIndex.equals(key.m_ifIndex))
                    );
        }
        return false;
    }

    public int hashCode() {
        return m_nodeId.hashCode() ^ m_ipAddr.hashCode();
    }

    public String toSqlClause() {
        if (m_ifIndex == null) {
            return "nodeid = ? and ipaddr = ? and snmpifindex is null";
        } else {
            return "nodeid = ? and ipaddr = ? and snmpifindex = ?";
        }
    }
    
    public Object[] toSqlParmArray() {
        if (m_ifIndex == null) {
            return new Object[] { m_nodeId, m_ipAddr };
        } else {
            return new Object[] { m_nodeId, m_ipAddr, m_ifIndex };
        }
    }

    public Integer getNodeId() {
        return m_nodeId;
    }
    
    public String getIpAddr() {
        return m_ipAddr;
    }
    
    public Integer getIfIndex() {
        return m_ifIndex;
    }
    
}
