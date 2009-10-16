package org.opennms.netmgt.provision.adapters.link;

import java.net.UnknownHostException;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;

public abstract class EndPointStatusValidators {
    
    public static EndPointStatusValidator ping(final String oid) {
        return new EndPointStatusValidator() {
            
            public boolean validate(SnmpAgentConfig agentConfig) throws UnknownHostException {
                
                return getValue(agentConfig, oid) != null ? true : false;
            }
        };
    }
    
    public static EndPointStatusValidator match(final String oid, final String regex) {
        return new EndPointStatusValidator() {
            
            public boolean validate(SnmpAgentConfig agentConfig) throws UnknownHostException {
                String value = getValue(agentConfig, oid);
                if(value != null) {
                    return value.matches(regex);
                }else{
                  return false;
                }
            }
            
            public String toString() {
                return "match(" + regex + ")";
            }
        };
    }
    
    public static EndPointStatusValidator and(final EndPointStatusValidator... validators) {
        
        return new EndPointStatusValidator() {
            
            public boolean validate(SnmpAgentConfig agentConfig) throws UnknownHostException {
                for(EndPointStatusValidator validator : validators) {
                    if(!validator.validate(agentConfig)) {
                        return false;
                    }
                }
                
                return true;
            }
            
            public String toString() {
                StringBuffer sb = new StringBuffer();
                sb.append("and(");
                boolean first = true;
                for(EndPointStatusValidator validator : validators) {
                    if(first) {
                        first = false;
                    }else {
                        sb.append(", ");
                    }
                    sb.append(validator.toString());
                }
                
                return sb.toString();
            }
        };
    }
    
    public static EndPointStatusValidator or(final EndPointStatusValidator... validators) {
        return new EndPointStatusValidator() {
            
            public boolean validate(SnmpAgentConfig agentConfig) throws UnknownHostException {
                for(EndPointStatusValidator validator : validators) {
                    if(validator.validate(agentConfig)) {
                        return true;
                    }
                }
                return false;
            }
            
            public String toString() {
                StringBuffer sb = new StringBuffer();
                sb.append("or(");
                boolean first = true;
                for(EndPointStatusValidator validator : validators) {
                    if(first) {
                        first = false;
                    }else {
                        sb.append(", ");
                    }
                    sb.append(validator.toString());
                }
                
                return sb.toString();
            }
            
        };
    }
    
    private static String getValue(SnmpAgentConfig agentConfig, String oid) {
        SnmpValue val = SnmpUtils.get(agentConfig, SnmpObjId.get(oid));
        if(val == null || val.isNull() || val.isEndOfMib() || val.isError()) {
            return null;
        }else {
            return val.toString();
        }
    }
}
