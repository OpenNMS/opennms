package org.opennms.netmgt.model;

public class SnmpInterfaceBuilder {
	
	private OnmsSnmpInterface m_snmpIf;

	public SnmpInterfaceBuilder(OnmsSnmpInterface snmpIf) {
		m_snmpIf = snmpIf;
	}

	public SnmpInterfaceBuilder setIfSpeed(long ifSpeed) {
		m_snmpIf.setIfSpeed(new Long(ifSpeed));
		return this;
	}

}
