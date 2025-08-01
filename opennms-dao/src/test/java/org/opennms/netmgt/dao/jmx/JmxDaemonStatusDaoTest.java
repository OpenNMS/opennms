/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.dao.jmx;

import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.opennms.netmgt.model.MockServiceDaemon;
import org.opennms.netmgt.model.ServiceInfo;

public class JmxDaemonStatusDaoTest extends TestCase {
    static private MBeanServer mBeanServer;
	static private ObjectName objectName[] = new ObjectName[4];
	static private String[] names = {"test","test2","notifd","test3"};
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
	
    @Override
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

    @Override
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
