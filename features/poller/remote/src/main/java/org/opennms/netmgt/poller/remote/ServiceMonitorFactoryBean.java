/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.remote;

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.poller.ServiceMonitor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>ServiceMonitorFactoryBean class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class ServiceMonitorFactoryBean implements FactoryBean<ServiceMonitor>, InitializingBean {
	
	private ServiceMonitor m_serviceMonitor;
	private Class<? extends ServiceMonitor> m_monitorClass;
	private Map<String,Object> m_monitorParameters;
	
	/**
	 * <p>setMonitorClass</p>
	 *
	 * @param serviceClass a {@link java.lang.Class} object.
	 */
	public void setMonitorClass(Class<? extends ServiceMonitor> serviceClass) {
		m_monitorClass = serviceClass;
	}
	
	/**
	 * <p>setMonitorParameters</p>
	 *
	 * @param serviceParameters a {@link java.util.Map} object.
	 */
	public void setMonitorParameters(Map<String,Object> serviceParameters) {
		m_monitorParameters = serviceParameters;
	}

	/**
	 * <p>getObject</p>
	 *
	 * @return a {@link org.opennms.netmgt.poller.ServiceMonitor} object.
	 * @throws java.lang.Exception if any.
	 */
        @Override
	public ServiceMonitor getObject() throws Exception {
		return m_serviceMonitor;
	}

	/**
	 * <p>getObjectType</p>
	 *
	 * @return a {@link java.lang.Class} object.
	 */
        @Override
	public Class<? extends ServiceMonitor> getObjectType() {
		return m_monitorClass;
	}

	/**
	 * <p>isSingleton</p>
	 *
	 * @return a boolean.
	 */
        @Override
	public boolean isSingleton() {
		return true;
	}

	/**
	 * <p>afterPropertiesSet</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		assertNotNull(m_monitorClass, "monitorClass");
		
		Assert.state(ServiceMonitor.class.isAssignableFrom(m_monitorClass), "monitorClass must implement the ServiceMonitor interface");
		
		if (m_monitorParameters == null)
			m_monitorParameters = new HashMap<String,Object>();
		
		m_serviceMonitor = (ServiceMonitor)m_monitorClass.newInstance();
		m_serviceMonitor.initialize(m_monitorParameters);
		
	}
	
	private void assertNotNull(Object propertyValue, String propertyName) {
		Assert.state(propertyValue != null, propertyName+" must be set for instances of "+Poller.class);
	}


}
