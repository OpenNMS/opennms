package org.opennms.netmgt.dao;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

public class BaseDaoTestCase extends
		AbstractTransactionalDataSourceSpringContextTests {

	private DistPollerDao m_distPollerDao;
	private NodeDao m_nodeDao;
	

	protected String[] getConfigLocations() {
		return new String[] { "classpath:/org/opennms/netmgt/dao/applicationContext-dao.xml" };
	}

	public void setDistPollerDao(DistPollerDao distPollerDao) {
		m_distPollerDao = distPollerDao;
	}

	public DistPollerDao getDistPollerDao() {
		return m_distPollerDao;
	}
	
	public void setNodeDao(NodeDao nodeDao) {
		m_nodeDao = nodeDao;
	}
	
	public NodeDao getNodeDao() {
		return m_nodeDao;
	}
	
	


}
