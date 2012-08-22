/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.ServiceInfo;
import org.opennms.netmgt.model.MockServiceDaemon;

public class JmxDaemonStatusDaoTest extends TestCase {
    static private MBeanServer mBeanServer;
	static private ObjectName objectName[] = new ObjectName[4];
	static private String names[] = {"test","test2","notifd","test3"};
	private JmxDaemonStatusDao jmxDaemonStatusDao;
	static {
		mBeanServer = MBeanServerFactory.createMBeanServer();
		// mBeanServer = (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);
		int i=0;
		try {
			for(i = 0; i < 4; i++){
			  objectName[i] = new ObjectName("opennms:Name=" + names[i]);
			}
		} catch (MalformedObjectNameException e) {
			throw new JmxObjectNameException("Malformed name while initializing ObjectName with name '"+objectName[i]+"'", e);
		} catch (NullPointerException e) {
			throw new JmxObjectNameException("Null value passed to new ObjectName -param '"+objectName[i]+"'", e);
		}
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		for(int i = 0; i < 4; i++){
		  MockServiceDaemon serviceDaemonStub = new MockServiceDaemon(names[i]);
		  serviceDaemonStub.start();
		  
		  mBeanServer.registerMBean(serviceDaemonStub, objectName[i]);
		}
		
		jmxDaemonStatusDao = new JmxDaemonStatusDao();
		jmxDaemonStatusDao.setMbeanServer(mBeanServer);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		for(int i = 0; i < 4; i++){
			mBeanServer.unregisterMBean(objectName[i]);
		}
	}

	public void testGetAllStatuses(){
		// get all the services
		try{
			Map<String, ServiceInfo> services = jmxDaemonStatusDao.getCurrentDaemonStatus();
			// assert on count
			assertEquals("Unexpected number of mbeans found", 4, services.size());
			// assert presense of specific service
			ServiceInfo service = services.get("notifd");
			// assert on status of a specific service
			String status = service.getServiceStatus();
			assertEquals("Unexpected State: ", "Started", status);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public void testGetServiceHandleForValidService(){
		// get notifd service
		// assert the service returned is not null
	}
	
	public void testGetServiceHandleForInvalidService(){
		// get nottobefound service
		// assert null return
	}
	
	public void testGetServiceHandleForNullServiceStr(){
		// get null service
		// assert null service passes exception
	}

	

}
