/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.provision.detector.radius.RadiusAuthDetector;
import org.opennms.netmgt.provision.support.NullDetectorMonitor;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class RadiusAuthDetectorTest implements ApplicationContextAware{
    
    @Autowired
    public RadiusAuthDetector m_detector;

    @Before
    public void setUp(){
         MockLogAppender.setupLogging();
    }
    
	@Test
	public void testDetectorFail() throws UnknownHostException{
	    m_detector.setTimeout(1);
	    m_detector.setNasID("asdfjlaks;dfjklas;dfj");
	    m_detector.setAuthType("chap");
	    m_detector.setPassword("invalid");
	    m_detector.setSecret("service");
	    m_detector.setUser("1273849127348917234891720348901234789012374");
	    m_detector.onInit();
		assertFalse(m_detector.isServiceDetected(InetAddress.getByName("192.168.1.100"), new NullDetectorMonitor()));
	}

	@Test
	@Ignore("have to have a radius server set up")
	public void testDetectorPass() throws UnknownHostException{
	    m_detector.setTimeout(1);
	    m_detector.setNasID("0");
	    m_detector.setAuthType("mschapv2");
	    m_detector.setPassword("password");
	    m_detector.setSecret("testing123");
	    m_detector.setUser("testing");
	    m_detector.onInit();
		assertTrue(m_detector.isServiceDetected(InetAddress.getByName("192.168.211.11"), new NullDetectorMonitor()));
	}

	
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        
    }
	
}
