package org.opennms.netmgt.provision.persist.policies;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.provision.BasePolicy;
import org.opennms.netmgt.provision.IpInterfacePolicy;
import org.opennms.netmgt.provision.annotations.Policy;
import org.opennms.netmgt.provision.annotations.Require;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>MatchingIpInterfacePolicy class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Scope("prototype")
@Policy("Match IP Interface")
public class MatchingIpInterfacePolicy extends BasePolicy<OnmsIpInterface> implements IpInterfacePolicy {
    
    

    public static enum Action { MANAGE, UNMANAGE, DO_NOT_PERSIST };
    
    private Action m_action = Action.DO_NOT_PERSIST;

    /**
     * <p>getAction</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Require({"MANAGE", "UNMANAGE", "DO_NOT_PERSIST"})
    public String getAction() {
        return m_action.toString();
    }
    
    /**
     * <p>setAction</p>
     *
     * @param action a {@link java.lang.String} object.
     */
    public void setAction(String action) {
        if (Action.MANAGE.toString().equalsIgnoreCase(action)) {
            m_action = Action.MANAGE;
        } else if (Action.UNMANAGE.toString().equalsIgnoreCase(action)) {
            m_action = Action.UNMANAGE;
        } else {
            m_action = Action.DO_NOT_PERSIST;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public OnmsIpInterface act(OnmsIpInterface iface) {
        switch (m_action) {
        case DO_NOT_PERSIST: 
            LogUtils.debugf(this, "NOT Peristing %s according to policy", iface);
            return null;
        case MANAGE:
            LogUtils.debugf(this, "Managing %s according to policy", iface);
            iface.setIsManaged("M");
            return iface;
        case UNMANAGE:
            LogUtils.debugf(this, "Unmanaging %s according to policy", iface);
            iface.setIsManaged("U");
            return iface;
        default:
            return iface;    
        }
    }
    
    /**
     * <p>setIpAddress</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     */
    public void setIpAddress(String ipAddress) {
        putCriteria("ipAddress", ipAddress);
    }
    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddress() {
        return getCriteria("ipAddress");
    }
    /**
     * <p>setHostName</p>
     *
     * @param hostName a {@link java.lang.String} object.
     */
    public void setHostName(String hostName) {
        putCriteria("ipHostName", hostName);
    }
    /**
     * <p>getHostName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHostName() {
        return getCriteria("ipHostName");
    }
}
