/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.provision.detector.msexchange;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;

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

        m_detector = m_detectorFactory.createDetector(new HashMap<>());
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
