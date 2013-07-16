/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.detector.datagram.NtpDetector;
import org.opennms.netmgt.provision.server.SimpleUDPServer;
import org.opennms.netmgt.provision.support.ntp.NtpMessage;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class NtpDetectorTest implements ApplicationContextAware {
    
    private ApplicationContext m_applicationContext;
    public NtpDetector m_detector;
    private SimpleUDPServer m_server;
    
    @Before
    public void setUp(){
        MockLogAppender.setupLogging();

        m_detector = getDetector(NtpDetector.class);
        m_detector.setRetries(0);
        assertNotNull(m_detector);
        
        m_server = new SimpleUDPServer(){
          
            @Override
            public void onInit(){
                NtpMessage message = new NtpMessage();
                message.version = 3;
                message.mode = 4;
                message.stratum = 3;
                message.precision = 24;
                message.rootDelay = 24.17;
                message.rootDispersion = 56.82;
                message.referenceTimestamp = message.transmitTimestamp;
                message.originateTimestamp = message.transmitTimestamp;
                message.receiveTimestamp = message.transmitTimestamp;
                message.transmitTimestamp = message.transmitTimestamp;
                byte[] response = message.toByteArray();
                
                addRequestResponse(null, response);
            }
            
        };
        m_server.setPort(1800);
        m_server.setInetAddress(InetAddressUtils.getLocalHostAddress());
    }
    
    @After
    public void tearDown() throws IOException {
        m_server.stopServer();
        m_server = null;
    }
     
    @Test(timeout=90000)
    public void testDetectorSuccess() throws Exception{
        m_server.onInit();
        m_server.startServer();
        
        m_detector.setPort(m_server.getPort());
        m_detector.setIpToValidate(InetAddressUtils.str(m_server.getInetAddress()));
        m_detector.init();
        assertTrue("Testing for NTP service, got false when true is supposed to be returned", m_detector.isServiceDetected(m_server.getInetAddress()));
    }
    
    @Test(timeout=90000)
    public void testDetectorFailWrongPort() throws Exception{
        m_server.onInit();
        m_server.startServer();
        
        m_detector.setPort(2000);
        m_detector.setIpToValidate(InetAddressUtils.str(m_server.getInetAddress()));
        m_detector.init();
        assertFalse(m_detector.isServiceDetected(m_server.getInetAddress()));
    }
    
    // This test is no longer valid because setIpToValidate is no longer needed.
    @Ignore
    @Test(timeout=90000)
    public void testDetectorFailIncorrectIp() throws Exception{
        m_server.onInit();
        m_server.startServer();
        
        m_detector.setPort(m_server.getPort());
        m_detector.setIpToValidate("127.0.0.10");
        m_detector.init();
        assertFalse(m_detector.isServiceDetected(m_server.getInetAddress()));
    }
    
    private NtpDetector getDetector(Class<? extends ServiceDetector> detectorClass) {
        Object bean = m_applicationContext.getBean(detectorClass.getName());
        assertNotNull(bean);
        assertTrue(detectorClass.isInstance(bean));
        return (NtpDetector)bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext;
    }
}
