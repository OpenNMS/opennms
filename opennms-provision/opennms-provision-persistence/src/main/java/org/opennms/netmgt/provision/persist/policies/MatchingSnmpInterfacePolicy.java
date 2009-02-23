package org.opennms.netmgt.provision.persist.policies;

import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.provision.BasePolicy;

public class MatchingSnmpInterfacePolicy extends BasePolicy<OnmsSnmpInterface> {
    private String m_ifDescr;
    private String m_ifName;
    private String m_ifType;
    private String m_ipAddress;
    private String m_netmask;
    private String m_physAddr;
    private String m_ifIndex;
    private String m_ifSpeed;
    private String m_ifAdminStatus;
    private String m_ifOperStatus;
    private String m_ifAlias;
    
    public OnmsSnmpInterface apply(OnmsSnmpInterface iface) {
        if (m_ifDescr != null) {
            if (!match(iface.getIfDescr(), m_ifDescr)) {
                return null;
            }
        }
        if (m_ifName != null) {
            if (!match(iface.getIfName(), m_ifName)) {
                return null;
            }
        }
        if (m_ifType != null) {
            if (iface.getIfType() == null) {
                return null;
            }
            if (!match(iface.getIfType().toString(), m_ifType)) {
                return null;
            }
        }
        if (m_ipAddress != null) {
            if (!match(iface.getIpAddress(), m_ipAddress)) {
                return null;
            }
        }
        if (m_netmask != null) {
            if (!match(iface.getIpAddress(), m_netmask)) {
                return null;
            }
        }
        if (m_physAddr != null) {
            if (!match(iface.getPhysAddr(), m_physAddr)) {
                return null;
            }
        }
        if (m_ifIndex != null) {
            if (iface.getIfIndex() == null) {
                return null;
            }
            if (!match(iface.getIfIndex().toString(), m_ifIndex)) {
                return null;
            }
        }
        if (m_ifSpeed != null) {
            if (iface.getIfSpeed() == null) {
                return null;
            }
            if (!match(iface.getIfSpeed().toString(), m_ifSpeed)) {
                return null;
            }
        }
        if (m_ifAdminStatus != null) {
            if (iface.getIfAdminStatus() == null) {
                return null;
            }
            if (!match(iface.getIfAdminStatus().toString(), m_ifAdminStatus)) {
                return null;
            }
        }
        if (m_ifOperStatus != null) {
            if (iface.getIfOperStatus() == null) {
                return null;
            }
            if (!match(iface.getIfOperStatus().toString(), m_ifOperStatus)) {
                return null;
            }
        }
        if (m_ifAlias != null) {
            if (!match(iface.getIfAlias(), m_ifAlias)) {
                return null;
            }
        }
        return iface;
    }

    public String getIfDescr() {
        return m_ifDescr;
    }

    public void setIfDescr(String ifDescr) {
        m_ifDescr = ifDescr;
    }

    public String getIfName() {
        return m_ifName;
    }

    public void setIfName(String ifName) {
        m_ifName = ifName;
    }

    public String getIfType() {
        return m_ifType;
    }

    public void setIfType(String ifType) {
        m_ifType = ifType;
    }

    public String getIpAddress() {
        return m_ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        m_ipAddress = ipAddress;
    }

    public String getNetmask() {
        return m_netmask;
    }

    public void setNetmask(String netmask) {
        m_netmask = netmask;
    }

    public String getPhysAddr() {
        return m_physAddr;
    }

    public void setPhysAddr(String physAddr) {
        m_physAddr = physAddr;
    }

    public String getIfIndex() {
        return m_ifIndex;
    }

    public void setIfIndex(String ifIndex) {
        m_ifIndex = ifIndex;
    }

    public String getIfSpeed() {
        return m_ifSpeed;
    }

    public void setIfSpeed(String ifSpeed) {
        m_ifSpeed = ifSpeed;
    }

    public String getIfAdminStatus() {
        return m_ifAdminStatus;
    }

    public void setIfAdminStatus(String ifAdminStatus) {
        m_ifAdminStatus = ifAdminStatus;
    }

    public String getIfOperStatus() {
        return m_ifOperStatus;
    }

    public void setIfOperStatus(String ifOperStatus) {
        m_ifOperStatus = ifOperStatus;
    }

    public String getIfAlias() {
        return m_ifAlias;
    }

    public void setIfAlias(String ifAlias) {
        m_ifAlias = ifAlias;
    }

}
