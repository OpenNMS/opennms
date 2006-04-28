package org.opennms.netmgt.dao.jdbc.agent;

import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSnmpAgent;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

public class LazyAgent extends OnmsSnmpAgent {
	
	
	private static final long serialVersionUID = 1L;

	private DataSource m_dataSource;
	private boolean m_dirty;
	private boolean m_loaded = false;
	
	public LazyAgent(DataSource dataSource) {
		m_dataSource = dataSource;
	}

	public boolean isDirty() {
		return m_dirty;
	}

	public boolean isLoaded() {
		return m_loaded;
	}

	public void setDirty(boolean dirty) {
		m_dirty = dirty;
	}

	public void setLoaded(boolean loaded) {
		m_loaded = loaded;
	}


    public String toString() {
		load();
		return super.toString();
	}

    private void load() {
		if (!m_loaded) {
			new FindAgentById(m_dataSource).findUnique(getId());
		}
	}

	public Set getAttributes() {
		load();
		return super.getAttributes();
	}

	public SnmpAgentConfig getConfig() {
		load();
		return super.getConfig();
	}

	public String getSysObjectId() {
		load();
		return super.getSysObjectId();
	}

	public void setAttributes(Set attributes) {
		load();
		setDirty(true);
		super.setAttributes(attributes);
	}

	public void setConfig(SnmpAgentConfig config) {
		load();
		setDirty(true);
		super.setConfig(config);
	}

	public void setSysObjectId(String sysObjectId) {
		load();
		setDirty(true);
		super.setSysObjectId(sysObjectId);
	}

	public String getIpAddress() {
		load();
		return super.getIpAddress();
	}

	public OnmsNode getNode() {
		load();
		return super.getNode();
	}

	public OnmsServiceType getServiceType() {
		load();
		return super.getServiceType();
	}

	public void setIpAddress(String ipAddress) {
		load();
		setDirty(true);
		super.setIpAddress(ipAddress);
	}

	public void setNode(OnmsNode node) {
		load();
		setDirty(true);
		super.setNode(node);
	}

	public void setServiceType(OnmsServiceType serviceType) {
		load();
		setDirty(true);
		super.setServiceType(serviceType);
	}

	public String getSysContact() {
		load();
		return super.getSysContact();
	}

	public String getSysDescription() {
		load();
		return super.getSysDescription();
	}

	public String getSysLocation() {
		load();
		return super.getSysLocation();
	}

	public String getSysName() {
		load();
		return super.getSysName();
	}

	public void setSysContact(String sysContact) {
		load();
		setDirty(true);
		super.setSysContact(sysContact);
	}

	public void setSysDescription(String sysDescription) {
		load();
		setDirty(true);
		super.setSysDescription(sysDescription);
	}

	public void setSysLocation(String sysLocation) {
		load();
		setDirty(true);
		super.setSysLocation(sysLocation);
	}

	public void setSysName(String sysName) {
		load();
		setDirty(true);
		super.setSysName(sysName);
	}


	

}
