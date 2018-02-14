/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.msexchange;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class MSExchangeDetectorTest implements InitializingBean {

    private static String TEST_BANNER = "Microsoft Exchange";

    @Autowired
    MSExchangeDetectorFactory m_detectorFactory;

    MSExchangeDetector m_detector;

    SimpleServer m_pop3Server;
    SimpleServer m_imapServer;


    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception{
        MockLogAppender.setupLogging();

        m_pop3Server = new SimpleServer(){

            @Override
            public void onInit(){
                setBanner(TEST_BANNER);
            }
        };
        m_pop3Server.init();
        m_pop3Server.startServer();


        m_imapServer = new SimpleServer(){

            @Override
            public void onInit(){
                setBanner(TEST_BANNER);
            }
        };
        m_imapServer.init();
        m_imapServer.startServer();

        m_detector = m_detectorFactory.createDetector();
        m_detector.setPop3Port(110);
        m_detector.setImapPort(143);
        m_detector.setTimeout(500);
    }

    @After
    public void tearDown() throws IOException{
        if(m_imapServer != null){
            m_imapServer.stopServer();
            m_imapServer = null;
        }

        if(m_pop3Server != null){
            m_pop3Server.stopServer();
            m_pop3Server = null;
        }
    }

    @Test(timeout=20000)
    public void testDetectorWired(){
        assertNotNull(m_detector);
    }

    @Test(timeout=20000)
    public void testDetectorSuccess() throws UnknownHostException{
        m_detector.setImapPort(m_imapServer.getLocalPort());
        m_detector.setPop3Port(m_pop3Server.getLocalPort());
        m_detector.onInit();
        assertTrue(m_detector.isServiceDetected(m_pop3Server.getInetAddress()));
    }

    @Test(timeout=20000)
    public void testDetectorSuccessPop3FailImap() throws IOException{
        m_imapServer.stopServer();
        m_detector.setPop3Port(m_pop3Server.getLocalPort());
        m_detector.onInit();
        assertTrue(m_detector.isServiceDetected(m_pop3Server.getInetAddress()));
    }

    @Test(timeout=20000)
    public void testDetectorSuccessImapFailPop3() throws IOException{
        m_pop3Server.stopServer();
        m_detector.setImapPort(m_imapServer.getLocalPort());
        m_detector.onInit();
        assertTrue(m_detector.isServiceDetected(m_pop3Server.getInetAddress()));
    }

    @Test(timeout=20000)
    public void testDetectorFailWrongPort(){
        m_detector.setImapPort(9000);
        m_detector.setPop3Port(9001);
        m_detector.onInit();
        assertFalse(m_detector.isServiceDetected(m_pop3Server.getInetAddress()));
    }
}
