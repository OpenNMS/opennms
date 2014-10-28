/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.provision.detector.ssh.SshDetector;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class SSHDetectorTest implements ApplicationContextAware, InitializingBean {
    //Tested on a local server with SSH

    @Autowired
    public SshDetector m_detector;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        m_detector = new SshDetector();
        m_detector.setTimeout(1);
    }

    @Test(timeout=90000)
    public void testDetectorSuccess() throws UnknownHostException{
        //m_detector.init();
        //assertTrue(m_detector.isServiceDetected(InetAddressUtils.addr("192.168.1.103")));
    }

    @Test(timeout=90000)
    public void testDetectorFailWrongPort() throws UnknownHostException{
        //m_detector.setPort(30);
        //m_detector.init();
        //assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr("192.168.1.103")));
    }

    @Test(timeout=90000)
    public void testDetectorFailBanner() throws UnknownHostException{
        //m_detector.setBanner("Hello there crazy");
        //m_detector.init();
        //assertFalse(m_detector.isServiceDetected(InetAddressUtils.addr("192.168.1.103")));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        // TODO Auto-generated method stub

    }
}