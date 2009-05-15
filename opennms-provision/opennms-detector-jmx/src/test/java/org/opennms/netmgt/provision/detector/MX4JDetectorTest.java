/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.provision.detector.jmx.MX4JDetector;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class MX4JDetectorTest {
       
    @Autowired
    public MX4JDetector m_detector;
    
    public static MBeanServer m_beanServer;
    private static Registry m_registry;
    private JMXConnectorServer m_connectorServer;
    
    @BeforeClass
    public static void beforeTest() throws RemoteException{
        m_registry = LocateRegistry.createRegistry(9999);
        m_beanServer = ManagementFactory.getPlatformMBeanServer();
    }
    
    @Before
    public void setUp() throws IOException {
        assertNotNull(m_detector);
        
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/server");
        
        m_connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, m_beanServer);
        m_connectorServer.start();
        
        m_detector.setPort(9999);
        m_detector.setUrlPath("/server");
    }
    
    @After
    public void tearDown() throws IOException{
        m_connectorServer.stop();
    }
    
    @Test
    public void testDetectoredWired(){
        assertNotNull(m_detector);
    }
   
    @Test
    public void testDetectorSuccess() throws IOException{
        m_detector.onInit();
        assertTrue(m_detector.isServiceDetected(InetAddress.getLocalHost(), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorWrongPort() throws UnknownHostException{
        m_detector.setPort(9000);
        m_detector.onInit();
        assertFalse(m_detector.isServiceDetected(InetAddress.getLocalHost(), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorWrongUrlPath() throws UnknownHostException{
        m_detector.setUrlPath("wrongpath");
        m_detector.onInit();
        assertFalse(m_detector.isServiceDetected(InetAddress.getLocalHost(), new NullDetectorMonitor()));
    }
}
