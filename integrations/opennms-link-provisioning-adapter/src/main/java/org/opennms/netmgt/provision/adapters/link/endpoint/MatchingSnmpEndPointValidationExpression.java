/**
 * 
 */
package org.opennms.netmgt.provision.adapters.link.endpoint;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.opennms.netmgt.provision.adapters.link.EndPoint;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusException;

@XmlRootElement(name="match-oid")
public class MatchingSnmpEndPointValidationExpression extends AbstractEndPointValidationExpression {
    @XmlAttribute(name="oid")
    private final String m_oid;
    @XmlValue
    private final String m_regex;

    public MatchingSnmpEndPointValidationExpression(String regex, String oid) {
        m_regex = regex;
        m_oid = oid;
    }

    public void validate(EndPoint endPoint) throws EndPointStatusException {
        String value = endPoint.get(m_oid).toString();
        if(value != null && value.matches(m_regex)) {
            return;
        }
        throw new EndPointStatusException("unable to validate endpoint " + endPoint + ": " + m_regex + " does not match value (" + value + ")");
    }

    public String toString() {
        return "match(" + m_regex + ")";
    }
}