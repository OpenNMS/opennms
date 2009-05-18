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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.detector.datagram.NtpDetector;
import org.opennms.netmgt.provision.server.SimpleUDPServer;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
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
        m_detector = getDetector(NtpDetector.class);
        m_detector.setRetries(0);
        assertNotNull(m_detector);
        
        m_server = new SimpleUDPServer(){
          
            @Override
            public void onInit(){
                NtpMessage message = new NtpMessage();
                byte[] response = message.toByteArray();
                
                addRequestResponse(null, response);
            }
            
        };
        m_server.setPort(1800);
        try {
            m_server.setInetAddress(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    
    
    @Test
    public void testDetectorSuccess() throws Exception{
        m_server.onInit();
        m_server.startServer();
        
        m_detector.setPort(m_server.getPort());
        m_detector.setIpToValidate(m_server.getInetAddress().getHostAddress());
        m_detector.init();
        assertTrue("Testing for NTP service, got false when true is supposed to be returned", m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorFailWrongPort() throws Exception{
        m_server.onInit();
        m_server.startServer();
        
        m_detector.setPort(2000);
        m_detector.setIpToValidate(m_server.getInetAddress().getHostAddress());
        m_detector.init();
        assertFalse(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
    }
    
    @Test
    public void testDetectorFailIncorrectIp() throws Exception{
        m_server.onInit();
        m_server.startServer();
        
        m_detector.setPort(m_server.getPort());
        m_detector.setIpToValidate("127.0.0.1");
        m_detector.init();
        assertFalse(m_detector.isServiceDetected(m_server.getInetAddress(), new NullDetectorMonitor()));
    }
    
    private NtpDetector getDetector(Class<? extends ServiceDetector> detectorClass) {
        Object bean = m_applicationContext.getBean(detectorClass.getName());
        assertNotNull(bean);
        assertTrue(detectorClass.isInstance(bean));
        return (NtpDetector)bean;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext;
    }
}
