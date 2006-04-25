package org.opennms.netmgt.dao.jdbc.ipif;

import java.util.Date;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;

public class LazyIpInterface extends OnmsIpInterface {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3388830282688753457L;
	private boolean m_loaded = false;
	private DataSource m_dataSource;
	private boolean m_dirty;
	
	public LazyIpInterface(DataSource dataSource) {
		m_dataSource = dataSource;
	}

	public String getIpHostName() {
		load();
		return super.getIpHostName();
	}

	public Date getIpLastCapsdPoll() {
		load();
		return super.getIpLastCapsdPoll();
	}

	public Integer getIpStatus() {
		load();
		return super.getIpStatus();
	}

	public String getIsManaged() {
		load();
		return super.getIsManaged();
	}

	public CollectionType getIsSnmpPrimary() {
		load();
		return super.getIsSnmpPrimary();
	}

	public Set getMonitoredServices() {
		load();
		return super.getMonitoredServices();
	}

	public void setIpHostName(String iphostname) {
		load();
		setDirty(true);
		super.setIpHostName(iphostname);
	}

	public void setIpLastCapsdPoll(Date iplastcapsdpoll) {
		load();
		setDirty(true);
		super.setIpLastCapsdPoll(iplastcapsdpoll);
	}

	public void setIpStatus(Integer ipstatus) {
		load();
		setDirty(true);
		super.setIpStatus(ipstatus);
	}

	public void setIsManaged(String ismanaged) {
		load();
		setDirty(true);
		super.setIsManaged(ismanaged);
	}

	public void setIsSnmpPrimary(CollectionType issnmpprimary) {
		load();
		setDirty(true);
		super.setIsSnmpPrimary(issnmpprimary);
	}

	public void setMonitoredServices(Set ifServices) {
		load();
		setDirty(true);
		super.setMonitoredServices(ifServices);
	}

	private void load() {
		if (!m_loaded) {
			IpInterfaceId id = new IpInterfaceId(this);
			FindById.get(m_dataSource, id).find(id);
		}
	}

	public void setLoaded(boolean loaded) {
		m_loaded = loaded;
	}
	
	public boolean isLoaded() {
		return m_loaded;
	}

	public boolean isDirty() {
		return m_dirty;
	}
	
	public void setDirty(boolean dirty) {
		m_dirty = dirty;
	}

	public void setIfIndex(Integer ifindex) {
		setDirty(true);
		super.setIfIndex(ifindex);
	}

	public void setIpAddress(String ipaddr) {
		setDirty(true);
		super.setIpAddress(ipaddr);
	}

	public void setNode(OnmsNode node) {
		setDirty(true);
		super.setNode(node);
	}

}
