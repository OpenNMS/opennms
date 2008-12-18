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

import org.junit.Test;
import org.junit.runner.RunWith;
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
    
    @Test
    public void testHttpDetectorWiring() {
       HttpDetector bean = (HttpDetector) m_applicationContext.getBean("httpDetector");
       assertNotNull(bean);
    }
    
    @Test
    public void testPop3DetectorWiring() {
        Pop3Detector bean = (Pop3Detector) m_applicationContext.getBean("pop3Detector");
        assertNotNull(bean);
    }
    
    @Test
    public void testCitrixDetectorWiring() {
        CitrixDetector bean = (CitrixDetector) m_applicationContext.getBean("citrixDetector");
        assertNotNull(bean);
    }
    
    @Test
    public void testDominoIIOPDetectorWiring() {
        DominoIIOPDetector bean = (DominoIIOPDetector) m_applicationContext.getBean("dominoIIOPDetector");
        assertNotNull(bean);
    }
    
    @Test
    public void testFtpDetectorWiring() {
        FtpDetector bean = (FtpDetector) m_applicationContext.getBean("ftpDetector");
        assertNotNull(bean);
    }
    
    @Test
    public void testHttpsDetectorWiring() {
        HttpsDetector bean = (HttpsDetector) m_applicationContext.getBean("httpsDetector");
        assertNotNull(bean);
    }
    
    @Test 
    public void testImapDetectorWiring() {
        ImapDetector bean = (ImapDetector) m_applicationContext.getBean("imapDetector");
        assertNotNull(bean);
    }
    
    @Test
    public void testLdapDetectorWiring() {
        LdapDetector bean = (LdapDetector) m_applicationContext.getBean("ldapDetector");
        assertNotNull(bean);
    }
    
    @Test
    public void testNrpeDetectorWiring() {
        NrpeDetector bean = (NrpeDetector) m_applicationContext.getBean("nrpeDetector");
        assertNotNull(bean);
    }
    
    @Test 
    public void testSmtpDetectorWiring() {
        SmtpDetector bean = (SmtpDetector) m_applicationContext.getBean("smtpDetector");
        assertNotNull(bean);
    }
    
    @Test
    public void testTcpDetectorWiring() {
        TcpDetector bean = (TcpDetector) m_applicationContext.getBean("tcpDetector");
        assertNotNull(bean);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        m_applicationContext = applicationContext;
    }

}
