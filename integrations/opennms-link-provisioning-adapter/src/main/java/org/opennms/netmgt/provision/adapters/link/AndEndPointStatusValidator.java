/**
 * 
 */
package org.opennms.netmgt.provision.adapters.link;

import org.opennms.netmgt.provision.LinkMonitorValidatorTest.EndPoint;
import org.opennms.netmgt.provision.adapters.link.endpoint.EndPointStatusException;

final class AndEndPointStatusValidator implements EndPointStatusValidator {
    private final EndPointStatusValidator[] m_validators;

    AndEndPointStatusValidator(EndPointStatusValidator[] validators) {
        m_validators = validators;
    }

    public boolean validate(EndPoint endPoint) throws EndPointStatusException {
        for(EndPointStatusValidator validator : m_validators) {
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
        for(EndPointStatusValidator validator : m_validators) {
            if(first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(validator.toString());
        }
        
        return sb.toString();
    }
}