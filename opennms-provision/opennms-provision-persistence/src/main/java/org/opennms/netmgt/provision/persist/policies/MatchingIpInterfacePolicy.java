package org.opennms.netmgt.provision.persist.policies;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.provision.BasePolicy;
import org.opennms.netmgt.provision.IpInterfacePolicy;
import org.opennms.netmgt.provision.annotations.Policy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Policy("IP Interface Matching")
public class MatchingIpInterfacePolicy extends BasePolicy implements IpInterfacePolicy {
    private String m_ipAddress;
    private String m_hostName;

    public OnmsIpInterface apply(OnmsIpInterface iface) {
        if (m_ipAddress != null) {
            if (!match(iface.getIpAddress(), m_ipAddress)) {
                return null;
            }
        }
        if (m_hostName != null) {
            if (!match(iface.getIpHostName(), m_hostName)) {
                return null;
            }
        }
        return iface;
    }
 
    public void setIpAddress(String ipAddress) {
        m_ipAddress = ipAddress;
    }
    public String getIpAddress() {
        return m_ipAddress;
    }
    public void setHostName(String hostName) {
        m_hostName = hostName;
    }
    public String getHostName() {
        return m_hostName;
    }
}
