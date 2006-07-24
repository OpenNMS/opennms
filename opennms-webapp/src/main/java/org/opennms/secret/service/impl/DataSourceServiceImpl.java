/**
 * 
 */
package org.opennms.secret.service.impl;

import java.util.List;

import org.opennms.secret.dao.DataSourceDao;
import org.opennms.secret.model.DataSource;
import org.opennms.secret.model.InterfaceService;
import org.opennms.secret.model.Node;
import org.opennms.secret.model.NodeInterface;
import org.opennms.secret.service.DataSourceService;

/**
 * @author Ted Kaczmarek	
 *
 */
public class DataSourceServiceImpl implements DataSourceService {
	private DataSourceDao m_dataSourceDao;
	
	public void setDataSourceDao(DataSourceDao dataSourceDao) {
		m_dataSourceDao = dataSourceDao;
	}
	
	/* (non-Javadoc)
	 * @see org.opennms.secret.service.DataSourceService#getDataSourcesByInterface(org.opennms.secret.model.NodeInterface)
	 */
	public List getDataSourcesByInterface(NodeInterface iface) {
		return m_dataSourceDao.getDataSourcesByInterface(iface);
	}

    /* (non-Javadoc)
     * @see org.opennms.secret.service.DataSourceService#getDataSourcesByService(org.opennms.secret.model.InterfaceService)
     */
    public DataSource getDataSourceByService(InterfaceService service) {
        return m_dataSourceDao.getDataSourceByService(service);
    }

	/* (non-Javadoc)
	 * @see org.opennms.secret.service.DataSourceService#getDataSourcesByNode(org.opennms.secret.model.Node)
	 */
	public List getDataSourcesByNode(Node node) {
		return m_dataSourceDao.getDataSourcesByNode(node);
    }
    
    /* (non-Javadoc)
     * @see org.opennms.secret.service.DataSourceService#getDataSourcesById(String)
     */
    public DataSource getDataSourceById(String id) {
        return m_dataSourceDao.getDataSourceById(id);
    }


}
