package org.opennms.netmgt.dao.jdbc;

import javax.sql.DataSource;

public class JdbcContext {
	
	DataSource m_dataSource;
	

	public JdbcContext(DataSource dataSource) {
		m_dataSource = dataSource;
	}
	
	public DataSource getDataSource() {
		return m_dataSource;
	}
	
	public void setDataSource(DataSource dataSource) {
		m_dataSource = dataSource;
	}
	
	

}
