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

package org.opennms.netmgt.dao.jmx;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.QueryExp;

import org.opennms.netmgt.dao.api.DaemonStatusDao;
import org.opennms.netmgt.model.ServiceDaemon;
import org.opennms.netmgt.model.ServiceInfo;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jmx.access.MBeanProxyFactoryBean;

public class JmxDaemonStatusDao implements DaemonStatusDao {

	private MBeanServer mbeanServer;

	/**
	 * <p>Setter for the field <code>mbeanServer</code>.</p>
	 *
	 * @param mbeanServer a {@link javax.management.MBeanServer} object.
	 */
	public void setMbeanServer(MBeanServer mbeanServer) {
		this.mbeanServer = mbeanServer;
	}

	/**
	 * <p>getCurrentDaemonStatus</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
        @Override
	public Map<String, ServiceInfo> getCurrentDaemonStatus() {
		// TODO Auto-generated method stub
		Map<String, ServiceInfo> serviceInfo = new HashMap<String, ServiceInfo>();
		// go to the JMX Server and ask for all the MBeans...
		// ArrayList<MBeanServer> mbeans =
		// MBeanServerFactory.findMBeanServer(null);
		// get their names and corresponding status and plugit into Service Info

		Set<ObjectName> mBeanNames;
		try {
            mBeanNames = queryMbeanServerForNames(new ObjectName("opennms:*"), null);
		} catch (MalformedObjectNameException e) {
			throw new JmxObjectNameException(
					"Object name 'opennms:*' was malformed!", e);
		} catch (NullPointerException e) {
			throw new JmxObjectNameException("Object name param is null.", e);
		}

		for (ObjectName mBeanName : mBeanNames) {
			ServiceDaemon serviceDaemon = buildProxy(mBeanName);

			String name = serviceDaemon.getName();
			String status = serviceDaemon.getStatusText();
			serviceInfo.put(name, new ServiceInfo(name, status));
		}

		// Map the name of the service to ServiceInfo...
		// for testing adding a dummy service info node...
		serviceInfo.put("test", new ServiceInfo("test", "started"));
		return Collections.unmodifiableMap(serviceInfo);
	}

    private Set<ObjectName> queryMbeanServerForNames(ObjectName foo1, QueryExp foo2) {
        return (Set<ObjectName>) mbeanServer.queryNames(foo1, foo2);
    }
	
	/**
	 * <p>getCurrentDaemonStatusColl</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<ServiceInfo> getCurrentDaemonStatusColl() {
		// TODO Auto-generated method stub
		return this.getCurrentDaemonStatus().values();
	}
	
	/** {@inheritDoc} */
        @Override
	public ServiceDaemon getServiceHandle(String service) {
		Set<ObjectName> mBeanNames;
		try {
			mBeanNames = queryMbeanServerForNames(new ObjectName("opennms:Name=" + service + ",*"), null);
		} catch (MalformedObjectNameException e) {
			throw new JmxObjectNameException("Object name 'opennms:Name=" + service
					+ ",*' was malformed!", e);
		} catch (NullPointerException e) {
			throw new JmxObjectNameException("Object name param is null.", e);
		}

		ObjectName mBeanName = (ObjectName) DataAccessUtils
				.requiredUniqueResult(mBeanNames);
		return buildProxy(mBeanName);
	}

	private ServiceDaemon buildProxy(ObjectName mBeanName) {
		MBeanProxyFactoryBean mBeanProxyFactoryBean = new MBeanProxyFactoryBean();

		try {
			mBeanProxyFactoryBean.setObjectName(mBeanName.getCanonicalName());
		} catch (MalformedObjectNameException e) {
			throw new JmxObjectNameException("Object name '"
					+ mBeanName.getCanonicalName() + "' was malformed!", e);
		}

		mBeanProxyFactoryBean.setProxyInterface(ServiceDaemon.class);

		mBeanProxyFactoryBean.afterPropertiesSet();
		return (ServiceDaemon) mBeanProxyFactoryBean.getObject();
	}
}
