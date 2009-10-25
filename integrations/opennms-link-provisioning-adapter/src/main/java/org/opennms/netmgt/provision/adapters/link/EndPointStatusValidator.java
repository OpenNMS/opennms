package org.opennms.netmgt.provision.adapters.link;

import org.opennms.netmgt.provision.LinkMonitorValidatorTest.EndPoint;
import org.opennms.netmgt.provision.adapters.link.endpoint.EndPointStatusException;

public interface EndPointStatusValidator {
   public boolean validate(EndPoint endPoint) throws EndPointStatusException;
}