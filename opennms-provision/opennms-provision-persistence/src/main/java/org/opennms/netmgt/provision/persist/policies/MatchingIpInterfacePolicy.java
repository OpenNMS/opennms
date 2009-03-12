package org.opennms.netmgt.provision.persist.policies;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.provision.BasePolicy;
import org.opennms.netmgt.provision.IpInterfacePolicy;
import org.opennms.netmgt.provision.annotations.Require;
import org.opennms.netmgt.provision.annotations.Policy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Policy("Match IP Interface")
public class MatchingIpInterfacePolicy extends BasePolicy<OnmsIpInterface> implements IpInterfacePolicy {
    
    

    public static enum Action { MANAGE, UNMANAGE, DO_NOT_PERSIST };
    
    private Action m_action = Action.DO_NOT_PERSIST;

    @Require({"MANAGE", "UNMANAGE", "DO_NOT_PERSIST"})
    public String getAction() {
        return m_action.toString();
    }
    
    public void setAction(String action) {
        if (Action.MANAGE.toString().equalsIgnoreCase(action)) {
            m_action = Action.MANAGE;
        } else if (Action.UNMANAGE.toString().equalsIgnoreCase(action)) {
            m_action = Action.UNMANAGE;
        } else {
            m_action = Action.DO_NOT_PERSIST;
        }
    }
    
    @Override
    public OnmsIpInterface act(OnmsIpInterface iface) {
        switch (m_action) {
        case DO_NOT_PERSIST: 
            return null;
        case MANAGE:
            iface.setIsManaged("M");
            return iface;
        case UNMANAGE:
            iface.setIsManaged("U");
            return iface;
        default:
            return iface;    
        }
    }
    
    public void setIpAddress(String ipAddress) {
        putCriteria("ipAddress", ipAddress);
    }
    public String getIpAddress() {
        return getCriteria("ipAddress");
    }
    public void setHostName(String hostName) {
        putCriteria("ipHostName", hostName);
    }
    public String getHostName() {
        return getCriteria("ipHostName");
    }
}
