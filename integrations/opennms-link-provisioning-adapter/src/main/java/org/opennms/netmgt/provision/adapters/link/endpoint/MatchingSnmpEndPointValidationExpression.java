/**
 * 
 */
package org.opennms.netmgt.provision.adapters.link.endpoint;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.provision.adapters.link.EndPoint;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusException;

@XmlRootElement(name="match-oid")
public class MatchingSnmpEndPointValidationExpression extends EndPointValidationExpressionImpl {
    @XmlAttribute(name="oid")
    private String m_oid = null;

    public MatchingSnmpEndPointValidationExpression() {
    }
    
    public MatchingSnmpEndPointValidationExpression(String regex, String oid) {
        setValue(regex);
        m_oid = oid;
    }

    public void validate(EndPoint endPoint) throws EndPointStatusException {
        String value = endPoint.get(m_oid).toString();
        if(value != null && value.matches(m_value)) {
            return;
        }
        throw new EndPointStatusException("unable to validate endpoint " + endPoint + ": " + m_value + " does not match value (" + value + ")");
    }

    public String toString() {
        return "match(" + m_value + ")";
    }
}