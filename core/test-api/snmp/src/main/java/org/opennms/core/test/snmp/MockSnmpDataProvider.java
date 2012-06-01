package org.opennms.core.test.snmp;

import java.io.IOException;

import org.opennms.netmgt.snmp.SnmpAgentAddress;
import org.springframework.core.io.Resource;

public interface MockSnmpDataProvider {

	public void setDataForAddress(SnmpAgentAddress address, Resource resource) throws IOException ;

	public void resetData();

}
