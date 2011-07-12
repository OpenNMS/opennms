package org.opennms.mock.snmp;

import org.opennms.netmgt.snmp.SnmpAgentAddress;
import org.springframework.core.io.Resource;

public interface MockSnmpDataProvider {

	public void setDataForAddress(SnmpAgentAddress address, Resource resource);

}
