package org.opennms.netmgt.provision.adapters.link;

import org.opennms.netmgt.provision.adapters.link.endpoint.AndEndPointValidationExpression;
import org.opennms.netmgt.provision.adapters.link.endpoint.MatchingSnmpEndPointValidationExpression;
import org.opennms.netmgt.provision.adapters.link.endpoint.OrEndPointValidationExpression;
import org.opennms.netmgt.provision.adapters.link.endpoint.PingEndPointValidationExpression;

public abstract class EndPointValidationExpressions {
    
    public static EndPointValidationExpression ping() {
        return new PingEndPointValidationExpression();
    }
    
    public static EndPointValidationExpression match(final String oid, final String regex) {
        return new MatchingSnmpEndPointValidationExpression(regex, oid);
    }
    
    public static EndPointValidationExpression and(final EndPointValidationExpression... validators) {
        return new AndEndPointValidationExpression(validators);
    }
    
    public static EndPointValidationExpression or(final EndPointValidationExpression... validators) {
        return new OrEndPointValidationExpression(validators);
    }
    
}
