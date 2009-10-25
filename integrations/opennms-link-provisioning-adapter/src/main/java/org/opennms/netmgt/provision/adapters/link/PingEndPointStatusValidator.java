/**
 * 
 */
package org.opennms.netmgt.provision.adapters.link;

import org.opennms.netmgt.provision.LinkMonitorValidatorTest.EndPoint;
import org.opennms.netmgt.provision.adapters.link.endpoint.EndPointStatusException;

final class PingEndPointStatusValidator implements EndPointStatusValidator {
    public boolean validate(EndPoint endPoint) throws EndPointStatusException {
           return endPoint.ping();
    }
}