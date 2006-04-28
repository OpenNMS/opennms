package org.opennms.netmgt.model;

import java.util.Set;

import org.opennms.netmgt.snmp.SnmpAgentConfig;

public class OnmsSnmpAgent extends OnmsAgent {
	
	private String m_sysObjectId;
	private String m_sysName;
	private String m_sysDescription;
	private String m_sysLocation;
	private String m_sysContact;
	
	private SnmpAgentConfig m_config;
	private Set m_attributes;
	
	public Set getAttributes() {
		return m_attributes;
	}
	public void setAttributes(Set attributes) {
		m_attributes = attributes;
	}
	public SnmpAgentConfig getConfig() {
		return m_config;
	}
	public void setConfig(SnmpAgentConfig config) {
		m_config = config;
	}
	public String getSysObjectId() {
		return m_sysObjectId;
	}
	public void setSysObjectId(String sysObjectId) {
		m_sysObjectId = sysObjectId;
	}
	public String getSysContact() {
		return m_sysContact;
	}
	public void setSysContact(String sysContact) {
		m_sysContact = sysContact;
	}
	public String getSysDescription() {
		return m_sysDescription;
	}
	public void setSysDescription(String sysDescription) {
		m_sysDescription = sysDescription;
	}
	public String getSysLocation() {
		return m_sysLocation;
	}
	public void setSysLocation(String sysLocation) {
		m_sysLocation = sysLocation;
	}
	public String getSysName() {
		return m_sysName;
	}
	public void setSysName(String sysName) {
		m_sysName = sysName;
	}
}
