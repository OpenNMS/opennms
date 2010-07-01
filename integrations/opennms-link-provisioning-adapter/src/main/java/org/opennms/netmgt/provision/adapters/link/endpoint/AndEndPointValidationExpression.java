
/**
 * <p>AndEndPointValidationExpression class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.provision.adapters.link.endpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.provision.adapters.link.EndPoint;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusException;
import org.opennms.netmgt.provision.adapters.link.EndPointValidationExpression;

@XmlRootElement(name="and")
public class AndEndPointValidationExpression extends EndPointValidationExpressionImpl {
    @XmlElementRef
    private List<EndPointValidationExpressionImpl> m_validators = Collections.synchronizedList(new ArrayList<EndPointValidationExpressionImpl>());

    /**
     * <p>Constructor for AndEndPointValidationExpression.</p>
     */
    public AndEndPointValidationExpression() {
    }

    /**
     * <p>Constructor for AndEndPointValidationExpression.</p>
     *
     * @param validators an array of {@link org.opennms.netmgt.provision.adapters.link.endpoint.EndPointValidationExpressionImpl} objects.
     */
    public AndEndPointValidationExpression(EndPointValidationExpressionImpl[] validators) {
        for (EndPointValidationExpressionImpl e : validators) {
            m_validators.add(e);
        }
    }

    /** {@inheritDoc} */
    public void validate(EndPoint endPoint) throws EndPointStatusException {
        for(EndPointValidationExpression validator : m_validators) {
            validator.validate(endPoint);
        }
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
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
