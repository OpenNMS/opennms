/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.radius.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class RadiusAuthDetectorTest implements ApplicationContextAware, InitializingBean {
    
    @Autowired
    public RadiusAuthDetector m_detector;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp(){
         MockLogAppender.setupLogging();
    }
    
	@Test(timeout=90000)
	public void testDetectorFail() throws UnknownHostException{
	    m_detector.setTimeout(1);
	    m_detector.setNasID("asdfjlaks;dfjklas;dfj");
	    m_detector.setAuthType("chap");
	    m_detector.setPassword("invalid");
	    m_detector.setSecret("service");
	    m_detector.setUser("1273849127348917234891720348901234789012374");
	    m_detector.onInit();
		assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr("192.168.1.100")));
	}

	@Test(timeout=90000)
	@Ignore
	public void testRunDetectorInTempThread() throws InterruptedException {
		for(int i = 0; i < 1000; i++) {
			Thread t = new Thread() {
                                @Override
				public void run() {
					try {
						testDetectorFail();
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			t.start();
			t.join();
		}
	}

	@Test(timeout=90000)
	@Ignore("have to have a radius server set up")
	public void testDetectorPass() throws UnknownHostException{
	    m_detector.setTimeout(1);
	    m_detector.setNasID("0");
	    m_detector.setAuthType("mschapv2");
	    m_detector.setPassword("password");
	    m_detector.setSecret("testing123");
	    m_detector.setUser("testing");
	    m_detector.onInit();
		assertTrue(m_detector.isServiceDetected(InetAddressUtils.addr("192.168.211.11")));
	}


	@Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        
    }
	
}
