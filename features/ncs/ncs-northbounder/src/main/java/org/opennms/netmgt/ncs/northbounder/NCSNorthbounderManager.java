package org.opennms.netmgt.ncs.northbounder;

import org.opennms.core.soa.Registration;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.netmgt.alarmd.api.Northbounder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class NCSNorthbounderManager implements InitializingBean, DisposableBean {
	
	@Autowired
	private ServiceRegistry m_serviceRegistry;

	@Autowired
	private NCSNorthbounderConfigDao m_configDao;
	
	private Registration m_registration = null;

	@Override
	public void afterPropertiesSet() throws Exception {
		NCSNorthbounderConfig config = m_configDao.getConfig();

		NCSNorthbounder northbounder = new NCSNorthbounder(config);
		
		m_serviceRegistry.register(northbounder, Northbounder.class);

	}
	
	@Override
	public void destroy() throws Exception {
		m_registration.unregister();
	}


}
