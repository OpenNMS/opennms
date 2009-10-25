/**
 * 
 */
package org.opennms.netmgt.provision.adapters.link.endpoint;

import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.provision.adapters.link.EndPoint;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusException;

@XmlRootElement(name="pingable")
public class PingEndPointValidationExpression extends AbstractEndPointValidationExpression {
    public void validate(EndPoint endPoint) throws EndPointStatusException {
       boolean returnVal = endPoint.ping();
       if (!returnVal) {
           throw new EndPointStatusException(String.format("unable to ping endPoint %s", endPoint));
       }
    }
    
    public String toString() {
        return "pingable()";
    }
}