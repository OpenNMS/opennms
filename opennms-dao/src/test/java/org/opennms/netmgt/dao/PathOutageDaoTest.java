package org.opennms.netmgt.dao;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.opennms.netmgt.dao.ibatis.PathOutage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class PathOutageDaoTest extends TestCase {
	
	public void testAdd() throws Exception {
	
		try {
			ApplicationContext context = new ClassPathXmlApplicationContext("./src/main/resources/applicationContext-ibatis.xml");
		
		PathOutageDao poDao = (PathOutageDao)context.getBean("pathOutage");
		
		PathOutage pathOutage = new PathOutage();
		
		pathOutage.setNodeId(1);
		pathOutage.setCriticalPathIp("192.168.0.1");
		pathOutage.setCriticalPathServiceName("ICMP");
		
		poDao.save(pathOutage);
		
		assertNotNull("Expected to retrieve a path outage but none were found", poDao.getAll());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(new File(".").getCanonicalPath());
			// TODO: handle exception
		}
	
	}
	
}
