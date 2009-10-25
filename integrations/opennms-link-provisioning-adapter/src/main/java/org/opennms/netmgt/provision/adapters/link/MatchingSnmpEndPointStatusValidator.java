/**
 * 
 */
package org.opennms.netmgt.provision.adapters.link;

import org.opennms.netmgt.provision.LinkMonitorValidatorTest.EndPoint;
import org.opennms.netmgt.provision.adapters.link.endpoint.EndPointStatusException;

final class MatchingSnmpEndPointStatusValidator implements EndPointStatusValidator {
    private final String m_regex;
    private final String m_oid;

    MatchingSnmpEndPointStatusValidator(String regex, String oid) {
        m_regex = regex;
        m_oid = oid;
    }

    public boolean validate(EndPoint endPoint) throws EndPointStatusException {
        String value = endPoint.get(m_oid).toString();
        if(value != null) {
            return value.matches(m_regex);
        } else {
            throw new EndPointStatusException("unable to validate endpoint " + endPoint + ": " + m_regex + " does not match value (" + value + ")");
        }
    }

    public String toString() {
        return "match(" + m_regex + ")";
    }
}