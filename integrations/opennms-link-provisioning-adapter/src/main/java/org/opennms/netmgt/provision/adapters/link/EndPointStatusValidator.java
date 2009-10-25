package org.opennms.netmgt.provision.adapters.link;

import org.opennms.netmgt.provision.LinkMonitorValidatorTest.EndPoint;

public interface EndPointStatusValidator{
   public boolean validate(EndPoint endPoint);
}