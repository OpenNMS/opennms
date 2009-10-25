package org.opennms.netmgt.provision.adapters.link;

import org.opennms.netmgt.provision.LinkMonitorValidatorTest.EndPoint;

public abstract class EndPointStatusValidators {
    
    public static EndPointStatusValidator ping() {
        return new EndPointStatusValidator() {
            
            public boolean validate(EndPoint endPoint) {
                   return endPoint.ping();
            }
        };
    }
    
    public static EndPointStatusValidator match(final String oid, final String regex) {
        return new EndPointStatusValidator() {
            
            public boolean validate(EndPoint endPoint) {
                String value = endPoint.get(oid).toString();
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
            
            public boolean validate(EndPoint endPoint) {
                for(EndPointStatusValidator validator : validators) {
                    if(!validator.validate(endPoint)) {
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
            
            public boolean validate(EndPoint endPoint) {
                for(EndPointStatusValidator validator : validators) {
                    if(validator.validate(endPoint)) {
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
    
}
