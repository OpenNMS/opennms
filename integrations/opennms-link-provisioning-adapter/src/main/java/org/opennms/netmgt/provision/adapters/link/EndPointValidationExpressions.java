package org.opennms.netmgt.provision.adapters.link;

import org.opennms.netmgt.provision.adapters.link.endpoint.AndEndPointValidationExpression;
import org.opennms.netmgt.provision.adapters.link.endpoint.EndPointValidationExpressionImpl;
import org.opennms.netmgt.provision.adapters.link.endpoint.MatchingSnmpEndPointValidationExpression;
import org.opennms.netmgt.provision.adapters.link.endpoint.OrEndPointValidationExpression;
import org.opennms.netmgt.provision.adapters.link.endpoint.PingEndPointValidationExpression;

public abstract class EndPointValidationExpressions {
    
    public static EndPointValidationExpressionImpl ping() {
        return new PingEndPointValidationExpression();
    }
    
    public static EndPointValidationExpressionImpl match(final String oid, final String regex) {
        return new MatchingSnmpEndPointValidationExpression(regex, oid);
    }
    
    public static EndPointValidationExpressionImpl and(final EndPointValidationExpressionImpl... validators) {
        return new AndEndPointValidationExpression(validators);
    }
    
    public static EndPointValidationExpressionImpl or(final EndPointValidationExpressionImpl... validators) {
        return new OrEndPointValidationExpression(validators);
    }
    
}
