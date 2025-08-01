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
package org.opennms.netmgt.provision.detector;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.DetectFutureListener;
import org.opennms.netmgt.provision.detector.simple.TcpDetector;
import org.opennms.netmgt.provision.detector.simple.TcpDetectorFactory;
import org.opennms.netmgt.provision.server.SimpleServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class TcpDetectorTest {
    private SimpleServer m_server;
    @Autowired
    private TcpDetectorFactory m_detectorFactory;
    private TcpDetector m_detector;
    private String m_serviceName;
    private int m_timeout;
    private String m_banner;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }

    private void initializeDetector() {
        m_detector = m_detectorFactory.createDetector(new HashMap<>());
        m_detector.setServiceName(getServiceName());
        m_detector.setTimeout(getTimeout());
        m_detector.setBanner(getBanner());
        m_detector.init();
    }

    private void initializeDefaultDetector() {
        setServiceName("TCP");
        setTimeout(500);
        setBanner(".*");

        initializeDetector();
    }

    private void intializeNullBannerDetector() {
        setServiceName("TCP");
        setTimeout(500);
        setBanner(null);

        initializeDetector();
    }

    @After
    public void tearDown() throws IOException {
        if(m_server != null){
            m_server.stopServer();
            m_server = null;
        }
    }


    @Test(timeout=20000)
    public void testSuccessServer() throws Exception {
        initializeDefaultDetector();

        m_server = new SimpleServer() {

            @Override
            public void onInit() {
                setBanner("Hello");
            }

        };
        m_server.init();
        m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());

        DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
        future.addListener(new DetectFutureListener<DetectFuture>() {

            @Override
            public void operationComplete(DetectFuture future) {
                TcpDetector detector = m_detector;
                m_detector = null;
                detector.dispose();
            }

        });

        assertNotNull(future);
        future.awaitForUninterruptibly();
        assertTrue(future.isServiceDetected());
    }



    @Test(timeout=20000)
    public void testFailureNoBannerSentWhenExpectingABanner() throws Exception {
        initializeDefaultDetector();

        m_server = new SimpleServer() {

            @Override
            public void onInit() {

            }

        };
        m_server.init();
        m_server.startServer();

        m_detector.setPort(m_server.getLocalPort());

        DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
        assertNotNull(future);
        future.awaitForUninterruptibly();
        assertFalse("Test should fail because no banner was sent when expecting a banner to be sent",future.isServiceDetected());

    }

    @Test(timeout=20000)
    public void testFailureConnectionTimesOutWhenExpectingABanner() throws Exception {
        initializeDefaultDetector();

        m_server = new SimpleServer() {

            @Override
            public void onInit() {
                setTimeout(500);
            }

        };
        m_server.init();
        m_server.startServer();

        m_detector.setPort(m_server.getLocalPort());

        DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
        assertNotNull(future);
        future.awaitForUninterruptibly();
        assertFalse("Test should fail because no banner was sent when expecting a banner to be sent",future.isServiceDetected());

    }

    @Test(timeout=20000)
    @Ignore
    public void testSuccessNotExpectingBannerNoBannerSent() throws Exception {
        intializeNullBannerDetector();

        m_server = new SimpleServer() {

            @Override
            public void onInit() {
                setTimeout(500);
            }

        };
        m_server.init();
        m_server.startServer();

        m_detector.setBanner(null);
        m_detector.setPort(m_server.getLocalPort());

        DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
        assertNotNull(future);
        future.awaitForUninterruptibly();
        assertTrue("Test should pass if we don't set a banner property and nothing responds", future.isServiceDetected());

    }



    @Test(timeout=20000)
    public void testFailureClosedPort() throws Exception {
        initializeDefaultDetector();

        m_server = new SimpleServer() {

            @Override
            public void onInit() {
                setBanner("BLIP");
            }

        };
        m_server.init();
        //m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());

        //assertFalse("Test should fail because the server closes before detection takes place", m_detector.isServiceDetected(m_server.getInetAddress()));

        DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
        assertNotNull(future);
        future.awaitForUninterruptibly();
        assertFalse(future.isServiceDetected());

    }

    /**
     * I think that this test is redundant with {@link #testFailureClosedPort()} since neither
     * server is actually started. The detector just times out on both connections.
     */
    @Test(timeout=20000)
    public void testServerCloses() throws Exception{
        initializeDefaultDetector();

        m_server = new SimpleServer() {

            @Override
            public void onInit() {
                shutdownServer("Closing");
            }

        };
        m_server.init();
        //m_server.startServer();
        m_detector.setPort(m_server.getLocalPort());

        //assertFalse("Test should fail because the server closes before detection takes place", m_detector.isServiceDetected(m_server.getInetAddress()));

        DetectFuture future = m_detector.isServiceDetected(m_server.getInetAddress());
        assertNotNull(future);
        future.awaitForUninterruptibly();
        assertFalse(future.isServiceDetected());
    }

    @Test(timeout=20000)
    public void testNoServerPresent() throws Exception {
        initializeDefaultDetector();

        m_detector.setPort(1999);
        //assertFalse("Test should fail because the server closes before detection takes place", m_detector.isServiceDetected(m_server.getInetAddress()));
        DetectFuture future = m_detector.isServiceDetected(InetAddress.getLocalHost());
        future.addListener(new DetectFutureListener<DetectFuture>() {

            @Override
            public void operationComplete(DetectFuture future) {
                TcpDetector detector = m_detector;
                m_detector = null;
                detector.dispose();
            }

        });
        assertNotNull(future);
        future.awaitForUninterruptibly();
        assertFalse(future.isServiceDetected());
    }

    public void setServiceName(String serviceName) {
        m_serviceName = serviceName;
    }

    public String getServiceName() {
        return m_serviceName;
    }

    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }

    public int getTimeout() {
        return m_timeout;
    }

    public void setBanner(String banner) {
        m_banner = banner;
    }

    public String getBanner() {
        return m_banner;
    }
}
