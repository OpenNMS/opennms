/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision.detector;

import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.provision.detector.ssh.SshDetector;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class SSHDetectorTest implements ApplicationContextAware{
	//Tested on a local server with SSH
    
    @Autowired
    public SshDetector m_detector;
    
    @Before
    public void setUp() {
        m_detector = new SshDetector();
        m_detector.setTimeout(1);
    }
    
	@Test
	public void testDetectorSuccess() throws UnknownHostException{
		//m_detector.init();
		//assertTrue(m_detector.isServiceDetected(InetAddress.getByName("192.168.1.103"), new NullDetectorMonitor()));
	}
	
	@Test
    public void testDetectorFailWrongPort() throws UnknownHostException{
	    //m_detector.setPort(30);
        //m_detector.init();
        //assertFalse(m_detector.isServiceDetected(InetAddress.getByName("192.168.1.103"), new NullDetectorMonitor()));
    }
	
	@Test
    public void testDetectorFailBanner() throws UnknownHostException{
	    //m_detector.setBanner("Hello there crazy");
        //m_detector.init();
        //assertFalse(m_detector.isServiceDetected(InetAddress.getByName("192.168.1.103"), new NullDetectorMonitor()));
    }

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        // TODO Auto-generated method stub
        
    }
	
}