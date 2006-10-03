package org.opennms.netmgt.dao;

import org.opennms.test.mock.MockLogAppender;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

public class BaseDaoTestCase extends
		AbstractTransactionalDataSourceSpringContextTests {

	private DistPollerDao m_distPollerDao;
	private NodeDao m_nodeDao;
	

	protected String[] getConfigLocations() {
        System.setProperty("opennms.home", "src/test/opennms-home");
		return new String[] { "classpath:/META-INF/opennms/applicationContext-dao.xml" };
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
