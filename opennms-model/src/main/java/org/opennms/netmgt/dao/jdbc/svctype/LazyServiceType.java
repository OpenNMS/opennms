package org.opennms.netmgt.dao.jdbc.svctype;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsServiceType;

public class LazyServiceType extends OnmsServiceType {

	private boolean m_loaded = false;
	private DataSource m_dataSource;
	
	public LazyServiceType(DataSource dataSource) {
		m_dataSource = dataSource;
	}
	
	public String getName() {
		load();
		return super.getName();
	}

	public void setName(String name) {
		load();
		super.setName(name);
	}

	private void load() {
		if (!m_loaded) {
			// this loads data into the object cache
			new FindByServiceTypeId(m_dataSource).findUnique(getId());
		}
	}

	public boolean isLoaded() {
		return m_loaded;
	}

	public void setLoaded(boolean loaded) {
		m_loaded = loaded;
	}
	
	

}
