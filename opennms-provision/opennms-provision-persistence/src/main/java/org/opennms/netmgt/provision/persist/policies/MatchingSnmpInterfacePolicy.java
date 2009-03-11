package org.opennms.netmgt.provision.persist.policies;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.provision.BasePolicy;
import org.opennms.netmgt.provision.SnmpInterfacePolicy;
import org.opennms.netmgt.provision.annotations.Allow;
import org.opennms.netmgt.provision.annotations.Policy;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Policy("SNMP Interface Matching")
public class MatchingSnmpInterfacePolicy extends BasePolicy implements SnmpInterfacePolicy {
    
    public static enum Action { ENABLE_COLLECTION, DISABLE_COLLECTION, DO_NOT_PERSIST };
    public static enum Match { ANY_PARAMETER, ALL_PARAMETERS, NO_PARAMETERS };
    
    private final LinkedHashMap<String, String> m_criteria = new LinkedHashMap<String, String>();
    
    private Action m_action = Action.DO_NOT_PERSIST;
    private Match m_match = Match.ANY_PARAMETER;

    @Allow({"ENABLE_COLLECTION", "DISABLE_COLLECTION", "DO_NOT_PERSIST"})
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

    @Allow({"ANY_PARAMETER", "ALL_PARAMETERS", "NO_PARAMETERS"})
    public String getMatchBehavior() {
        return m_match.toString();
    }
    
    public void setMatchBehavior(String matchBehavior) {
        if (matchBehavior != null && matchBehavior.toUpperCase().contains("ALL")) {
            m_match = Match.ALL_PARAMETERS;
        } else if (matchBehavior != null && matchBehavior.toUpperCase().contains("NO")) {
            m_match = Match.NO_PARAMETERS;
        } else {
            m_match = Match.ANY_PARAMETER;
        }
    }
    
    public String getIfDescr() {
        return m_criteria.get("ifDescr");
    }

    public void setIfDescr(String ifDescr) {
        m_criteria.put("ifDescr", ifDescr);
    }

    public String getIfName() {
        return m_criteria.get("ifName");
    }

    public void setIfName(String ifName) {
        m_criteria.put("ifName", ifName);
    }

    public String getIfType() {
        return m_criteria.get("ifType");
    }

    public void setIfType(String ifType) {
        m_criteria.put("ifType", ifType);
    }

    public String getIpAddress() {
        return m_criteria.get("ipAddress");
    }

    public void setIpAddress(String ipAddress) {
        m_criteria.put("ipAddress", ipAddress);
    }

    public String getNetmask() {
        return m_criteria.get("netmask");
    }

    public void setNetmask(String netmask) {
        m_criteria.put("netmask", netmask);
    }

    public String getPhysAddr() {
        return m_criteria.get("physAddr");
    }

    public void setPhysAddr(String physAddr) {
        m_criteria.put("physAddr", physAddr);
    }

    public String getIfIndex() {
        return m_criteria.get("ifIndex");
    }

    public void setIfIndex(String ifIndex) {
        m_criteria.put("ifIndex", ifIndex);
    }

    public String getIfSpeed() {
        return m_criteria.get("ifSpeed");
    }

    public void setIfSpeed(String ifSpeed) {
        m_criteria.put("ifSpeed", ifSpeed);
    }

    public String getIfAdminStatus() {
        return m_criteria.get("ifAdminStatus");
    }

    public void setIfAdminStatus(String ifAdminStatus) {
        m_criteria.put("ifAdminStatus", ifAdminStatus);
    }

    public String getIfOperStatus() {
        return m_criteria.get("ifOperStatus");
    }

    public void setIfOperStatus(String ifOperStatus) {
        m_criteria.put("ifOperStatus", ifOperStatus);
    }

    public String getIfAlias() {
        return m_criteria.get("ifAlias");
    }

    public void setIfAlias(String ifAlias) {
        m_criteria.put("ifAlias", ifAlias);
    }
    
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
    
    public OnmsSnmpInterface apply(OnmsSnmpInterface iface) {
        if (iface == null) {
            return null;
        }
        
        if (matches(iface)) {
            return act(iface);
        }
        
        return iface;
    }

    private boolean matches(OnmsSnmpInterface iface) {
        
        switch (m_match) {
        case ALL_PARAMETERS: 
            return matchAll(iface);
        case NO_PARAMETERS:
            return matchNode(iface);
        case ANY_PARAMETER:
        default:
            return matchAny(iface);
        }                

    }

    private boolean matchAll(OnmsSnmpInterface iface) {
        BeanWrapper bean = new BeanWrapperImpl(iface);
        
        for(Entry<String, String> term : m_criteria.entrySet()) {
            
            String val = getPropertyValueAsString(bean, term.getKey());
            String matchExpression = term.getValue();
            
            if (!match(val, matchExpression)) {
                return false;
            }
        }
        
        return true;
        

    }
    private boolean matchAny(OnmsSnmpInterface iface) {
        BeanWrapper bean = new BeanWrapperImpl(iface);
        
        for(Entry<String, String> term : m_criteria.entrySet()) {
            
            String val = getPropertyValueAsString(bean, term.getKey());
            String matchExpression = term.getValue();
            
            if (match(val, matchExpression)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean matchNode(OnmsSnmpInterface iface) {
        return !matchAny(iface);
    }
    
    private String getPropertyValueAsString(BeanWrapper bean, String propertyName) {
        return (String) bean.convertIfNecessary(bean.getPropertyValue(propertyName), String.class);
    }

}
