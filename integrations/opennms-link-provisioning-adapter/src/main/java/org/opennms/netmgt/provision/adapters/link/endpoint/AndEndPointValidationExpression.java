/**
 * 
 */
package org.opennms.netmgt.provision.adapters.link.endpoint;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.provision.adapters.link.EndPoint;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusException;
import org.opennms.netmgt.provision.adapters.link.EndPointValidationExpression;

@XmlRootElement(name="and")
public class AndEndPointValidationExpression extends AbstractEndPointValidationExpression {
    @XmlElementRef
    private final EndPointValidationExpression[] m_validators;

    public AndEndPointValidationExpression(EndPointValidationExpression[] validators) {
        m_validators = validators;
    }

    public void validate(EndPoint endPoint) throws EndPointStatusException {
        for(EndPointValidationExpression validator : m_validators) {
            validator.validate(endPoint);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("and(");
        boolean first = true;
        for(EndPointValidationExpression validator : m_validators) {
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