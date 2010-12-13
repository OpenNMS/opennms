/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 */

package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.provision.ServiceDetector;
import org.opennms.netmgt.provision.detector.simple.CitrixDetector;
import org.opennms.netmgt.provision.detector.simple.DominoIIOPDetector;
import org.opennms.netmgt.provision.detector.simple.FtpDetector;
import org.opennms.netmgt.provision.detector.simple.HttpDetector;
import org.opennms.netmgt.provision.detector.simple.HttpsDetector;
import org.opennms.netmgt.provision.detector.simple.ImapDetector;
import org.opennms.netmgt.provision.detector.simple.LdapDetector;
import org.opennms.netmgt.provision.detector.simple.NrpeDetector;
import org.opennms.netmgt.provision.detector.simple.Pop3Detector;
import org.opennms.netmgt.provision.detector.simple.SmtpDetector;
import org.opennms.netmgt.provision.detector.simple.TcpDetector;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author brozow
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class DetectorWiringTest implements ApplicationContextAware {
    
    protected ApplicationContext m_applicationContext;
    
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }

    private void testWiredDetector(Class<? extends ServiceDetector> detectorClass) {
        Object bean = m_applicationContext.getBean(detectorClass.getName());
        assertNotNull(bean);
        assertTrue(detectorClass.isInstance(bean));
    }
    
    @Test
    public void testHttpDetectorWiring() {
        testWiredDetector(HttpDetector.class);
    }
    
    @Test
    public void testPop3DetectorWiring() {
        testWiredDetector(Pop3Detector.class);
    }
    
    @Test
    public void testCitrixDetectorWiring() {
        testWiredDetector(CitrixDetector.class);
    }
    
    @Test
    public void testDominoIIOPDetectorWiring() {
        testWiredDetector(DominoIIOPDetector.class);
    }
    
    @Test
    public void testFtpDetectorWiring() {
        testWiredDetector(FtpDetector.class);
    }
    
    @Test
    public void testHttpsDetectorWiring() {
        testWiredDetector(HttpsDetector.class);
    }
    
    @Test 
    public void testImapDetectorWiring() {
        testWiredDetector(ImapDetector.class);
    }
    
    @Test
    public void testLdapDetectorWiring() {
        testWiredDetector(LdapDetector.class);
    }
    
    @Test
    public void testNrpeDetectorWiring() {
        testWiredDetector(NrpeDetector.class);
    }
    
    @Test 
    public void testSmtpDetectorWiring() {
        testWiredDetector(SmtpDetector.class);
    }
    
    @Test
    public void testTcpDetectorWiring() {
        testWiredDetector(TcpDetector.class);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext;
    }

}
