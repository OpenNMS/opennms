/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.protocols.radius.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.protocols.radius.monitor.MockRadiusServer;
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
    public RadiusAuthDetectorFactory m_detectorFactory;
    
    public RadiusAuthDetector m_detector;
    	private MockRadiusServer mockSrv = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp(){
        mockSrv = new MockRadiusServer();
        mockSrv.start(true,false);
        MockLogAppender.setupLogging();
        m_detector = m_detectorFactory.createDetector();
    }
    @After
    public void tearDown(){
        mockSrv.stop();
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
        assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr("127.0.0.1")));
    }

    @Test(timeout=90000)
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
    public void testDetectorPass() throws UnknownHostException{
        m_detector.setTimeout(1);
        m_detector.setNasID("0");
        //mschapv2 i sunsupported by tinyradius, use chap
        m_detector.setAuthType("chap");
        m_detector.setPassword("password");
        m_detector.setSecret("testing123");
        m_detector.setUser("testing");
        m_detector.onInit();
        assertTrue(m_detector.isServiceDetected(InetAddressUtils.addr("127.0.0.1")));
    }

    //@Test(timeout=90000)
    @Test
    @Ignore("have to have a radius server set up")
    public void testTTLSDetectorPass() throws UnknownHostException{
        m_detector.setTimeout(1);
        m_detector.setNasID("0");
        m_detector.setAuthType("eap-ttls");
        m_detector.setPassword("D9VvfY2MuXLu");
        m_detector.setSecret("superV");
        m_detector.setUser("testing@org.example");
        m_detector.setInnerIdentity("monitoring-use@org.example");
        m_detector.setTtlsInnerAuthType("pap");
        m_detector.onInit();
        assertTrue(m_detector.isServiceDetected(InetAddressUtils.addr("127.0.0.1")));
    }

    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }

}
