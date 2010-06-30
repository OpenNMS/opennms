package org.opennms.netmgt.provision.adapters.link;

import org.opennms.netmgt.provision.adapters.link.endpoint.AndEndPointValidationExpression;
import org.opennms.netmgt.provision.adapters.link.endpoint.EndPointValidationExpressionImpl;
import org.opennms.netmgt.provision.adapters.link.endpoint.MatchingSnmpEndPointValidationExpression;
import org.opennms.netmgt.provision.adapters.link.endpoint.OrEndPointValidationExpression;
import org.opennms.netmgt.provision.adapters.link.endpoint.PingEndPointValidationExpression;

/**
 * <p>Abstract EndPointValidationExpressions class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class EndPointValidationExpressions {
    
    /**
     * <p>ping</p>
     *
     * @return a {@link org.opennms.netmgt.provision.adapters.link.endpoint.EndPointValidationExpressionImpl} object.
     */
    public static EndPointValidationExpressionImpl ping() {
        return new PingEndPointValidationExpression();
    }
    
    /**
     * <p>match</p>
     *
     * @param oid a {@link java.lang.String} object.
     * @param regex a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.adapters.link.endpoint.EndPointValidationExpressionImpl} object.
     */
    public static EndPointValidationExpressionImpl match(final String oid, final String regex) {
        return new MatchingSnmpEndPointValidationExpression(regex, oid);
    }
    
    /**
     * <p>and</p>
     *
     * @param validators a {@link org.opennms.netmgt.provision.adapters.link.endpoint.EndPointValidationExpressionImpl} object.
     * @return a {@link org.opennms.netmgt.provision.adapters.link.endpoint.EndPointValidationExpressionImpl} object.
     */
    public static EndPointValidationExpressionImpl and(final EndPointValidationExpressionImpl... validators) {
        return new AndEndPointValidationExpression(validators);
    }
    
    /**
     * <p>or</p>
     *
     * @param validators a {@link org.opennms.netmgt.provision.adapters.link.endpoint.EndPointValidationExpressionImpl} object.
     * @return a {@link org.opennms.netmgt.provision.adapters.link.endpoint.EndPointValidationExpressionImpl} object.
     */
    public static EndPointValidationExpressionImpl or(final EndPointValidationExpressionImpl... validators) {
        return new OrEndPointValidationExpression(validators);
    }
    
}
