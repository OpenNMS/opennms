package org.opennms.netmgt.mock;


import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.netmgt.xml.event.Snmp;

public class SnmpWrapper {
	Snmp m_snmp;
	
	public SnmpWrapper(Snmp snmp) {
		m_snmp = snmp;
	}
	
	public Snmp getSnmp() {
		return m_snmp;
	}
	
	public String toString() {
        Snmp snmp = m_snmp;
		return EventUtils.toString(snmp);
	}
}
