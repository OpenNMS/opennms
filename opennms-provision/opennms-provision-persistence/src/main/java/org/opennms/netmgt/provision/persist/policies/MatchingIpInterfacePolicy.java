package org.opennms.netmgt.provision.persist.policies;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterface;
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
    
    

    public static enum Action { MANAGE, UNMANAGE, DO_NOT_PERSIST, ENABLE_SNMP_POLL,DISABLE_SNMP_POLL, ENABLE_COLLECTION, DISABLE_COLLECTION };
    
    private Action m_action = Action.DO_NOT_PERSIST;

    /**
     * <p>getAction</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Require({"MANAGE", "UNMANAGE", "DO_NOT_PERSIST", "ENABLE_SNMP_POLL", "DISABLE_SNMP_POLL", "ENABLE_COLLECTION", "DISABLE_COLLECTION"})
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
        } else if (Action.ENABLE_SNMP_POLL.toString().equalsIgnoreCase(action)) {
            m_action = Action.ENABLE_SNMP_POLL;
        } else if (Action.DISABLE_SNMP_POLL.toString().equalsIgnoreCase(action)) {
            m_action = Action.DISABLE_SNMP_POLL;
        } else if (action != null && action.toUpperCase().equals("ENABLE_COLLECTION")) {
            m_action = Action.ENABLE_COLLECTION;
        } else if (action != null && action.toUpperCase().equals("DISABLE_COLLECTION")) {
            m_action = Action.DISABLE_COLLECTION;
        } else {
            m_action = Action.DO_NOT_PERSIST;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public OnmsIpInterface act(OnmsIpInterface iface) {
        OnmsSnmpInterface snmpiface = iface.getSnmpInterface();
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
        case ENABLE_SNMP_POLL:
            LogUtils.debugf(this, "Snmp Polling %s according to policy", iface);
            snmpiface.setPoll("P");
            iface.setSnmpInterface(snmpiface);
            return iface;
        case DISABLE_SNMP_POLL:
            LogUtils.debugf(this, "Disable Snmp Polling %s according to policy", iface);
            snmpiface.setPoll("N");
            iface.setSnmpInterface(snmpiface);
            return iface;
        case DISABLE_COLLECTION:
            LogUtils.debugf(this, "Disabled collection for %s according to policy", iface);
            snmpiface.setCollectionEnabled(false);
            iface.setSnmpInterface(snmpiface);
            return iface;
        case ENABLE_COLLECTION:
            LogUtils.debugf(this, "Enabled collection for %s according to policy", iface);
            snmpiface.setCollectionEnabled(true);
            iface.setSnmpInterface(snmpiface);
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
