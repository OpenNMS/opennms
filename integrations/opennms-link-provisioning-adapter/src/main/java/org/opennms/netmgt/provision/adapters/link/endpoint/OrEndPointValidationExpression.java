
/**
 * <p>OrEndPointValidationExpression class.</p>
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

@XmlRootElement(name="or")
public class OrEndPointValidationExpression extends EndPointValidationExpressionImpl {
    @XmlElementRef
    private List<EndPointValidationExpressionImpl> m_validators = Collections.synchronizedList(new ArrayList<EndPointValidationExpressionImpl>());

    /**
     * <p>Constructor for OrEndPointValidationExpression.</p>
     */
    public OrEndPointValidationExpression() {
    }

    /**
     * <p>Constructor for OrEndPointValidationExpression.</p>
     *
     * @param validators an array of {@link org.opennms.netmgt.provision.adapters.link.endpoint.EndPointValidationExpressionImpl} objects.
     */
    public OrEndPointValidationExpression(EndPointValidationExpressionImpl[] validators) {
        for (EndPointValidationExpressionImpl e : validators) {
            m_validators.add(e);
        }
    }

    /** {@inheritDoc} */
    public void validate(EndPoint endPoint) throws EndPointStatusException {
        EndPointStatusException reason = null;
        for(EndPointValidationExpression validator : m_validators) {
            try {
                validator.validate(endPoint);
                return;
            } catch (EndPointStatusException e) {
                reason = e;
            }
        }
        if (reason != null) {
            throw reason;
        }
        throw new EndPointStatusException("no validators in this 'or'");
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("or(");
        boolean first = true;
        for(EndPointValidationExpression validator : m_validators) {
            if(first) {
                first = false;
            }else {
                sb.append(", ");
            }
            sb.append(validator.toString());
        }
        
        return sb.toString();
    }
}
