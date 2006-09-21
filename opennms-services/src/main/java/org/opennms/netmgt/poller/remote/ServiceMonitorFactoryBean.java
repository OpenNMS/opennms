package org.opennms.netmgt.poller.remote;

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.poller.ServiceMonitor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class ServiceMonitorFactoryBean implements FactoryBean, InitializingBean {
	
	private ServiceMonitor m_serviceMonitor;
	private Class m_monitorClass;
	private Map m_monitorParameters;
	
	public void setMonitorClass(Class serviceClass) {
		m_monitorClass = serviceClass;
	}
	
	public void setMonitorParameters(Map serviceParameters) {
		m_monitorParameters = serviceParameters;
	}

	public Object getObject() throws Exception {
		return m_serviceMonitor;
	}

	public Class getObjectType() {
		return m_monitorClass;
	}

	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() throws Exception {
		assertNotNull(m_monitorClass, "monitorClass");
		
		Assert.state(ServiceMonitor.class.isAssignableFrom(m_monitorClass), "monitorClass must implement the ServiceMonitor interface");
		
		if (m_monitorParameters == null)
			m_monitorParameters = new HashMap();
		
		m_serviceMonitor = (ServiceMonitor)m_monitorClass.newInstance();
		m_serviceMonitor.initialize(null, m_monitorParameters);
		
	}
	
	private void assertNotNull(Object propertyValue, String propertyName) {
		Assert.state(propertyValue != null, propertyName+" must be set for instances of "+Poller.class);
	}


}
