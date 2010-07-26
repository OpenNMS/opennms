
/**
 * <p>PingEndPointValidationExpression class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.provision.adapters.link.endpoint;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.provision.adapters.link.EndPoint;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusException;

@XmlRootElement(name="pingable")
public class PingEndPointValidationExpression extends EndPointValidationExpressionImpl {
    /** {@inheritDoc} */
    public void validate(EndPoint endPoint) throws EndPointStatusException {
       boolean returnVal = endPoint.ping();
       if (!returnVal) {
           throw new EndPointStatusException(String.format("unable to ping endPoint %s", endPoint));
       }
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return "pingable()";
    }
}
