package org.opennms.netmgt.model.topology;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.BridgeElement;

public class Bridge {
    final Integer m_id;
    Integer m_rootPort;
    boolean m_isRootBridge=false;
    Set<String> m_bridgeIds=new HashSet<String>();
    Set<String> m_otherStpRoots=new HashSet<String>();

    public Bridge(BridgeElement bridgeElement) {
        super();
        m_id = bridgeElement.getNode().getId();
        m_bridgeIds.add(bridgeElement.getBaseBridgeAddress());
        if (InetAddressUtils.isValidStpBridgeId(bridgeElement.getStpDesignatedRoot())) {
            String stpRoot = InetAddressUtils.getBridgeAddressFromStpBridgeId(bridgeElement.getStpDesignatedRoot());
            if (!stpRoot.equals(bridgeElement.getBaseBridgeAddress()))
                m_otherStpRoots.add(stpRoot);
        } 
    }

    public Bridge(Integer id) {
        super();
        m_id = id;
    }


    public void addBridgeMac(String mac) {
        m_bridgeIds.add(mac);
    }
    
    public Set<String> getBridgeMacs() {
        return m_bridgeIds;
    }

    public void setBridgeElements(List<BridgeElement> bridgeIds) {
        m_bridgeIds.clear();
        m_otherStpRoots.clear();
        for (BridgeElement elem: bridgeIds) {
            m_bridgeIds.add(elem.getBaseBridgeAddress());
            if (InetAddressUtils.isValidStpBridgeId(elem.getStpDesignatedRoot())) {
                String stpRoot = InetAddressUtils.getBridgeAddressFromStpBridgeId(elem.getStpDesignatedRoot());
                if ( stpRoot.equals(elem.getBaseBridgeAddress()))
                        continue;
                m_otherStpRoots.add(stpRoot);
            }
        }
    }
    
    public boolean hasBridgeId(String bridgeId) {
        return m_bridgeIds.contains(bridgeId);
    }
            
    public Set<String> getOtherStpRoots() {
        return m_otherStpRoots;
    }


    public Integer getRootPort() {
        return m_rootPort;
    }

    public void setRootPort(Integer rootPort) {
        m_rootPort = rootPort;
    }

    public boolean isRootBridge() {
        return m_isRootBridge;
    }

    public void setRootBridge(boolean isRootBridge) {
        m_isRootBridge = isRootBridge;
    }

    public void addBridgeElement(BridgeElement bridgeElement) {
        if (bridgeElement.getNode().getId() != m_id)
            return; 
        m_bridgeIds.add(bridgeElement.getBaseBridgeAddress());
        if (InetAddressUtils.isValidStpBridgeId(bridgeElement.getStpDesignatedRoot())) {
            String stpRoot = InetAddressUtils.getBridgeAddressFromStpBridgeId(bridgeElement.getStpDesignatedRoot());
            if (!stpRoot.equals(bridgeElement.getBaseBridgeAddress()))
                m_otherStpRoots.add(stpRoot);
        }            
    }

    public Integer getId() {
        return m_id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
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
        Bridge other = (Bridge) obj;
        if (m_id == null) {
            if (other.m_id != null)
                return false;
        } else if (!m_id.equals(other.m_id))
            return false;
        return true;
    }


    

}
