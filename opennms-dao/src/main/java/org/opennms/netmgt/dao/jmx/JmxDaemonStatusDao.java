//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.dao.jmx;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.QueryExp;

import org.opennms.netmgt.dao.DaemonStatusDao;
import org.opennms.netmgt.dao.ServiceInfo;
import org.opennms.netmgt.model.ServiceDaemon;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jmx.access.MBeanProxyFactoryBean;

/**
 * <p>JmxDaemonStatusDao class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
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
			String status = serviceDaemon.status();
			serviceInfo.put(name, new ServiceInfo(name, status));
		}

		// Map the name of the service to ServiceInfo...
		// for testing adding a dummy service info node...
		serviceInfo.put("test", new ServiceInfo("test", "started"));
		return serviceInfo;
	}

    @SuppressWarnings("unchecked")
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
