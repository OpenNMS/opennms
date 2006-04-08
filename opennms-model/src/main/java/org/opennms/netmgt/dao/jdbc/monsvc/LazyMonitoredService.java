package org.opennms.netmgt.dao.jdbc.monsvc;

import java.util.Date;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsServiceType;

public class LazyMonitoredService extends OnmsMonitoredService {
	
	private boolean m_loaded = false;
	private DataSource m_dataSource;
	private boolean m_dirty;
	
	public LazyMonitoredService(DataSource dataSource) {
		m_dataSource = dataSource;
	}

	public Date getLastFail() {
		load();
		return super.getLastFail();
	}

	public Date getLastGood() {
		load();
		return super.getLastGood();
	}

	public String getNotify() {
		load();
		return super.getNotify();
	}

	public String getQualifier() {
		load();
		return super.getQualifier();
	}

	public String getSource() {
		load();
		return super.getSource();
	}

	public String getStatus() {
		load();
		return super.getStatus();
	}

	public void setLastFail(Date lastfail) {
		load();
		setDirty(true);
		super.setLastFail(lastfail);
	}

	public void setLastGood(Date lastgood) {
		load();
		setDirty(true);
		super.setLastGood(lastgood);
	}

	public void setNotify(String notify) {
		load();
		setDirty(true);
		super.setNotify(notify);
	}

	public void setQualifier(String qualifier) {
		load();
		setDirty(true);
		super.setQualifier(qualifier);
	}

	public void setSource(String source) {
		load();
		setDirty(true);
		super.setSource(source);
	}

	public void setStatus(String status) {
		load();
		setDirty(true);
		super.setStatus(status);
	}

	private void load() {
		if (!m_loaded) {
			MonitoredServiceId id = new MonitoredServiceId(this);
			FindById.get(m_dataSource, id).find(id);
		}
	}

	public boolean isLoaded() {
		return m_loaded;
	}

	public void setLoaded(boolean loaded) {
		m_loaded = loaded;
	}

	public boolean isDirty() {
		return m_dirty;
	}
	
	public void setDirty(boolean dirty) {
		m_dirty = dirty;
	}

	public void setIpInterface(OnmsIpInterface ipInterface) {
		setDirty(true);
		super.setIpInterface(ipInterface);
	}

	public void setServiceType(OnmsServiceType service) {
		setDirty(true);
		super.setServiceType(service);
	}
	
}
