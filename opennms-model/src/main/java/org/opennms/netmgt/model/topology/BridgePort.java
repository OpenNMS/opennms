package org.opennms.netmgt.model.topology;

import java.util.Date;

import org.opennms.netmgt.model.OnmsNode;

public class BridgePort {

    private OnmsNode m_node;
    private Integer m_bridgePort;
    private Integer m_bridgePortIfIndex;
    private String  m_bridgePortIfName;
    private Integer m_vlan;
    private Date m_createTime;
    private Date m_pollTime;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((m_bridgePort == null) ? 0 : m_bridgePort.hashCode());
        result = prime * result + ((m_node == null) ? 0 : m_node.getId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BridgePort other = (BridgePort) obj;
        if (m_bridgePort == null) {
            if (other.m_bridgePort != null)
                return false;
        } else if (!m_bridgePort.equals(other.m_bridgePort))
            return false;
        if (m_node == null) {
            if (other.m_node != null)
                return false;
        } else if (!m_node.getId().equals(other.m_node.getId()))
            return false;
        return true;
    }

    public OnmsNode getNode() {
        return m_node;
    }
    public void setNode(OnmsNode node) {
        m_node = node;
    }
    public Integer getBridgePort() {
        return m_bridgePort;
    }
    public void setBridgePort(Integer bridgePort) {
        m_bridgePort = bridgePort;
    }
    public Integer getBridgePortIfIndex() {
        return m_bridgePortIfIndex;
    }
    public void setBridgePortIfIndex(Integer bridgePortIfIndex) {
        m_bridgePortIfIndex = bridgePortIfIndex;
    }
    public String getBridgePortIfName() {
        return m_bridgePortIfName;
    }
    public void setBridgePortIfName(String bridgePortIfName) {
        m_bridgePortIfName = bridgePortIfName;
    }
    public Integer getVlan() {
        return m_vlan;
    }
    public void setVlan(Integer vlan) {
        m_vlan = vlan;
    }
    
    public Date getCreateTime() {
        return m_createTime;
    }
    public void setCreateTime(Date time) {
        m_createTime = time;
    }
    public Date getPollTime() {
        return m_pollTime;
    }
    public void setPollTime(Date time) {
        m_pollTime = time;
    }
    
}
