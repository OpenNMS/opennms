package org.opennms.netmgt.mock;

import java.util.Date;

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
		StringBuffer b = new StringBuffer("Snmp: ");

		if (m_snmp.getVersion() != null) {
			b.append("Version: " + m_snmp.getVersion() + "\n");
		}
		
		b.append("TimeStamp: " + new Date(m_snmp.getTimeStamp()) + "\n");
		
		if (m_snmp.getCommunity() != null) {
			b.append("Community: " + m_snmp.getCommunity() + "\n");
		}

		b.append("Generic: " + m_snmp.getGeneric() + "\n");
		b.append("Specific: " + m_snmp.getSpecific() + "\n");
		
		if (m_snmp.getId() != null) {
			b.append("Id: " + m_snmp.getId() + "\n");
		}
		if (m_snmp.getIdtext() != null) {
			b.append("Idtext: " + m_snmp.getIdtext() + "\n");
		}
		
		b.append("End Snmp\n");
		return b.toString();
	}
}
