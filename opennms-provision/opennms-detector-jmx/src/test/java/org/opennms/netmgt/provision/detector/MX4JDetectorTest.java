/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.provision.detector.jmx.MX4JDetector;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class MX4JDetectorTest implements InitializingBean {
       
    @Autowired
    public MX4JDetector m_detector;
    
    public static MBeanServer m_beanServer;
    private JMXConnectorServer m_connectorServer;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @BeforeClass
    public static void beforeTest() throws RemoteException{
        LocateRegistry.createRegistry(9999);
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
    
    @Test(timeout=90000)
    public void testDetectoredWired(){
        assertNotNull(m_detector);
    }
   
    @Test(timeout=90000)
    public void testDetectorSuccess() throws IOException{
        m_detector.init();
        assertTrue(m_detector.isServiceDetected(InetAddress.getLocalHost()));
    }
    
    @Test(timeout=90000)
    public void testDetectorWrongPort() throws UnknownHostException{
        m_detector.setPort(9000);
        m_detector.init();
        assertFalse(m_detector.isServiceDetected(InetAddress.getLocalHost()));
    }
    
    @Test(timeout=90000)
    public void testDetectorWrongUrlPath() throws UnknownHostException{
        m_detector.setUrlPath("wrongpath");
        m_detector.init();
        assertFalse(m_detector.isServiceDetected(InetAddress.getLocalHost()));
    }
}
