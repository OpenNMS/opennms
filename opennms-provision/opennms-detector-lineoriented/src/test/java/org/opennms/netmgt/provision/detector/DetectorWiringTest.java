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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.provision.detector.simple.CitrixDetector;
import org.opennms.netmgt.provision.detector.simple.CitrixDetectorFactory;
import org.opennms.netmgt.provision.detector.simple.DominoIIOPDetector;
import org.opennms.netmgt.provision.detector.simple.DominoIIOPDetectorFactory;
import org.opennms.netmgt.provision.detector.simple.FtpDetector;
import org.opennms.netmgt.provision.detector.simple.FtpDetectorFactory;
import org.opennms.netmgt.provision.detector.simple.HttpDetector;
import org.opennms.netmgt.provision.detector.simple.HttpDetectorFactory;
import org.opennms.netmgt.provision.detector.simple.HttpsDetector;
import org.opennms.netmgt.provision.detector.simple.HttpsDetectorFactory;
import org.opennms.netmgt.provision.detector.simple.ImapDetector;
import org.opennms.netmgt.provision.detector.simple.ImapDetectorFactory;
import org.opennms.netmgt.provision.detector.simple.LdapDetector;
import org.opennms.netmgt.provision.detector.simple.LdapDetectorFactory;
import org.opennms.netmgt.provision.detector.simple.LdapsDetector;
import org.opennms.netmgt.provision.detector.simple.LdapsDetectorFactory;
import org.opennms.netmgt.provision.detector.simple.NrpeDetector;
import org.opennms.netmgt.provision.detector.simple.NrpeDetectorFactory;
import org.opennms.netmgt.provision.detector.simple.Pop3Detector;
import org.opennms.netmgt.provision.detector.simple.Pop3DetectorFactory;
import org.opennms.netmgt.provision.detector.simple.SmtpDetector;
import org.opennms.netmgt.provision.detector.simple.SmtpDetectorFactory;
import org.opennms.netmgt.provision.detector.simple.TcpDetector;
import org.opennms.netmgt.provision.detector.simple.TcpDetectorFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author brozow
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class DetectorWiringTest implements InitializingBean {
    
    @Autowired
    private HttpDetectorFactory m_httpDetectorFactory;
    @Autowired 
    private Pop3DetectorFactory m_pop3DetectorFactory;
    @Autowired 
    private CitrixDetectorFactory m_citrixDetectorFactory;
    @Autowired 
    private DominoIIOPDetectorFactory m_dominoIIOPDetectorFactory;
    @Autowired 
    private FtpDetectorFactory m_ftpDetectorFactory;
    @Autowired 
    private HttpsDetectorFactory m_httpsDetectorFactory;
    @Autowired 
    private ImapDetectorFactory m_imapDetectorFactory;
    @Autowired 
    private LdapDetectorFactory m_ldapDetectorFactory;
    @Autowired 
    private LdapsDetectorFactory m_ldapsDetectorFactory;
    @Autowired 
    private NrpeDetectorFactory m_nrpeDetectorFactory;
    @Autowired 
    private SmtpDetectorFactory m_smtpDetectorFactory;
    @Autowired 
    private TcpDetectorFactory m_tcpDetectorFactory; 

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }

    @Test
    public void testSomething() {
        // All checks are in the InitializingBean method
    }
}
