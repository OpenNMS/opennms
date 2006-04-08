package org.opennms.netmgt.dao.jdbc.distpoller;

import java.math.BigDecimal;
import java.util.Date;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsDistPoller;

public class LazyDistPoller extends OnmsDistPoller {
	
	private boolean m_loaded = false;
	private DataSource m_dataSource;
	
	public LazyDistPoller(DataSource dataSource) {
		m_dataSource = dataSource;
	}

	public Integer getAdminState() {
		load();
		return super.getAdminState();
	}

	public String getComment() {
		load();
		return super.getComment();
	}

	public BigDecimal getDiscoveryLimit() {
		load();
		return super.getDiscoveryLimit();
	}

	public String getIpAddress() {
		load();
		return super.getIpAddress();
	}

	public Date getLastEventPull() {
		load();
		return super.getLastEventPull();
	}

	public Date getLastNodePull() {
		load();
		return super.getLastNodePull();
	}

	public Date getLastPackagePush() {
		load();
		return super.getLastPackagePush();
	}

	public Integer getRunState() {
		load();
		return super.getRunState();
	}

	public void setAdminState(Integer dpadminstate) {
		load();
		super.setAdminState(dpadminstate);
	}

	public void setComment(String dpcomment) {
		load();
		super.setComment(dpcomment);
	}

	public void setDiscoveryLimit(BigDecimal dpdisclimit) {
		load();
		super.setDiscoveryLimit(dpdisclimit);
	}

	public void setIpAddress(String dpip) {
		load();
		super.setIpAddress(dpip);
	}

	public void setLastEventPull(Date dplasteventpull) {
		load();
		super.setLastEventPull(dplasteventpull);
	}

	public void setLastNodePull(Date dplastnodepull) {
		load();
		super.setLastNodePull(dplastnodepull);
	}

	public void setLastPackagePush(Date dplastpackagepush) {
		load();
		super.setLastPackagePush(dplastpackagepush);
	}

	public void setRunState(Integer dprunstate) {
		load();
		super.setRunState(dprunstate);
	}

	private void load() {
		if (!m_loaded) {
			new FindByName(m_dataSource).findUnique(getName());
		}
	}

	public boolean isLoaded() {
		return m_loaded;
	}

	public void setLoaded(boolean loaded) {
		m_loaded = loaded;
	}
	

}
