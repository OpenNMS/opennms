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
import org.opennms.netmgt.provision.detector.simple.DominoIIOPDetector;
import org.opennms.netmgt.provision.detector.simple.FtpDetector;
import org.opennms.netmgt.provision.detector.simple.HttpDetector;
import org.opennms.netmgt.provision.detector.simple.HttpsDetector;
import org.opennms.netmgt.provision.detector.simple.ImapDetector;
import org.opennms.netmgt.provision.detector.simple.LdapDetector;
import org.opennms.netmgt.provision.detector.simple.LdapsDetector;
import org.opennms.netmgt.provision.detector.simple.NrpeDetector;
import org.opennms.netmgt.provision.detector.simple.Pop3Detector;
import org.opennms.netmgt.provision.detector.simple.SmtpDetector;
import org.opennms.netmgt.provision.detector.simple.TcpDetector;
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
    @Qualifier(value="org.opennms.netmgt.provision.detector.simple.HttpDetector")
    private HttpDetector m_httpDetector;
    @Autowired 
    private Pop3Detector m_pop3Detector;
    @Autowired 
    private CitrixDetector m_citrixDetector;
    @Autowired 
    private DominoIIOPDetector m_dominoIIOPDetector;
    @Autowired 
    private FtpDetector m_ftpDetector;
    @Autowired 
    private HttpsDetector m_httpsDetector;
    @Autowired 
    private ImapDetector m_imapDetector;
    @Autowired 
    @Qualifier(value="org.opennms.netmgt.provision.detector.simple.LdapDetector")
    private LdapDetector m_ldapDetector;
    @Autowired 
    private LdapsDetector m_ldapsDetector;
    @Autowired 
    private NrpeDetector m_nrpeDetector;
    @Autowired 
    private SmtpDetector m_smtpDetector;
    @Autowired 
    private TcpDetector m_tcpDetector;

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
