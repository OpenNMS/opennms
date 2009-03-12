package org.opennms.netmgt.provision.persist.policies;


import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.provision.BasePolicy;
import org.opennms.netmgt.provision.SnmpInterfacePolicy;
import org.opennms.netmgt.provision.annotations.Require;
import org.opennms.netmgt.provision.annotations.Policy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Policy("Match SNMP Interface")
public class MatchingSnmpInterfacePolicy extends BasePolicy<OnmsSnmpInterface> implements SnmpInterfacePolicy {
    
    public static enum Action { ENABLE_COLLECTION, DISABLE_COLLECTION, DO_NOT_PERSIST };
    
    private Action m_action = Action.DO_NOT_PERSIST;

    @Require({"ENABLE_COLLECTION", "DISABLE_COLLECTION", "DO_NOT_PERSIST"})
    public String getAction() {
        return m_action.toString();
    }
    
    public void setAction(String action) {
        if (action != null && action.toUpperCase().contains("ENABLE")) {
            m_action = Action.ENABLE_COLLECTION;
        } else if (action != null && action.toUpperCase().contains("DISABLE")) {
            m_action = Action.DISABLE_COLLECTION;
        } else {
            m_action = Action.DO_NOT_PERSIST;
        }
    }
    
    @Override
    public OnmsSnmpInterface act(OnmsSnmpInterface iface) {
        switch (m_action) {
        case DO_NOT_PERSIST: 
            return null;
        case DISABLE_COLLECTION:
            iface.setCollectionEnabled(false);
            return iface;
        case ENABLE_COLLECTION:
            iface.setCollectionEnabled(true);
            return iface;
        default:
            return iface;    
        }
    }
    
    public String getIfDescr() {
        return getCriteria("ifDescr");
    }

    public void setIfDescr(String ifDescr) {
        putCriteria("ifDescr", ifDescr);
    }

    public String getIfName() {
        return getCriteria("ifName");
    }

    public void setIfName(String ifName) {
        putCriteria("ifName", ifName);
    }

    public String getIfType() {
        return getCriteria("ifType");
    }

    public void setIfType(String ifType) {
        putCriteria("ifType", ifType);
    }

    public String getIpAddress() {
        return getCriteria("ipAddress");
    }

    public void setIpAddress(String ipAddress) {
        putCriteria("ipAddress", ipAddress);
    }

    public String getNetmask() {
        return getCriteria("netmask");
    }

    public void setNetmask(String netmask) {
        putCriteria("netmask", netmask);
    }

    public String getPhysAddr() {
        return getCriteria("physAddr");
    }

    public void setPhysAddr(String physAddr) {
        putCriteria("physAddr", physAddr);
    }

    public String getIfIndex() {
        return getCriteria("ifIndex");
    }

    public void setIfIndex(String ifIndex) {
        putCriteria("ifIndex", ifIndex);
    }

    public String getIfSpeed() {
        return getCriteria("ifSpeed");
    }

    public void setIfSpeed(String ifSpeed) {
        putCriteria("ifSpeed", ifSpeed);
    }

    public String getIfAdminStatus() {
        return getCriteria("ifAdminStatus");
    }

    public void setIfAdminStatus(String ifAdminStatus) {
        putCriteria("ifAdminStatus", ifAdminStatus);
    }

    public String getIfOperStatus() {
        return getCriteria("ifOperStatus");
    }

    public void setIfOperStatus(String ifOperStatus) {
        putCriteria("ifOperStatus", ifOperStatus);
    }

    public String getIfAlias() {
        return getCriteria("ifAlias");
    }

    public void setIfAlias(String ifAlias) {
        putCriteria("ifAlias", ifAlias);
    }
    
}
