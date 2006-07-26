package org.opennms.netmgt.dao.jmx;

import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.ServiceInfo;

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
		  ServiceDaemonStub serviceDaemonStub = new ServiceDaemonStub(names[i]);
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
		} catch (Exception e) {
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
