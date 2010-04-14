/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: August 18, 2006
 *
 * Copyright (C) 2004-2006 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.poller.remote;

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.poller.ServiceMonitor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class ServiceMonitorFactoryBean implements FactoryBean<ServiceMonitor>, InitializingBean {
	
	private ServiceMonitor m_serviceMonitor;
	private Class<? extends ServiceMonitor> m_monitorClass;
	private Map<String,Object> m_monitorParameters;
	
	public void setMonitorClass(Class<? extends ServiceMonitor> serviceClass) {
		m_monitorClass = serviceClass;
	}
	
	public void setMonitorParameters(Map<String,Object> serviceParameters) {
		m_monitorParameters = serviceParameters;
	}

	public ServiceMonitor getObject() throws Exception {
		return m_serviceMonitor;
	}

	public Class<? extends ServiceMonitor> getObjectType() {
		return m_monitorClass;
	}

	public boolean isSingleton() {
		return true;
	}

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
