/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.detector.simple.LdapDetector;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author Donald Desloge
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class LdapDetectorTest implements ApplicationContextAware{
    
//    private LdapDetector m_detector;
//    private static String DEFAULT_LOCAL_SERVER_IP = "192.168.1.103";
    /*
     * Setup a local Ldap server and test against that server. Then change the IP address to the server
     */
    
//    @Before
//    public void setUp() {
//        MockLogAppender.setupLogging();
//        m_detector = getDetector(LdapDetector.class);
//        m_detector.init();
//    }
    
    private ApplicationContext m_applicationContext;

    @Test(timeout=90000)
    public void testMyDetector() throws UnknownHostException {
        //assertTrue(m_detector.isServiceDetected(InetAddressUtils.addr(DEFAULT_LOCAL_SERVER_IP)));
    }
//    
//    @Test(timeout=90000)
//    public void testDetectorFailWrongPort() throws UnknownHostException {
//        m_detector.setPort(1200);
//        assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr(DEFAULT_LOCAL_SERVER_IP)));
//    }
//    
//    @Test(timeout=90000)
//    public void testDetectorFailNotALdapServer() throws UnknownHostException {
//        assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr("192.168.1.101")));
//    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext;
    }
    
    @SuppressWarnings("unused")
    private LdapDetector getDetector(Class<? extends ServiceDetector> detectorClass) {
        Object bean = m_applicationContext.getBean(detectorClass.getName());
        assertNotNull(bean);
        assertTrue(detectorClass.isInstance(bean));
        return (LdapDetector)bean;
    }
}
