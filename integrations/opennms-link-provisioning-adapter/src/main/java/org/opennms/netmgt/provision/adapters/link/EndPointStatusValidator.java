package org.opennms.netmgt.provision.adapters.link;

import org.opennms.netmgt.provision.LinkMonitorValidatorTest.SnmpAgentValueGetter;

public interface EndPointStatusValidator{
   public boolean validate(SnmpAgentValueGetter valueGetter);
}