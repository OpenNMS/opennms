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

package org.opennms.netmgt.provision.detector;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.detector.simple.HttpsDetector;
import org.opennms.netmgt.provision.detector.simple.HttpsDetectorFactory;
import org.opennms.netmgt.provision.server.SSLServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class HttpsDetectorTest {
    private static final int SSL_PORT = 7142;

    @Autowired
    private HttpsDetectorFactory m_detectorFactory;
    
    private HttpsDetector m_detector;
    private SSLServer m_server;

    @Rule
    public WireMockRule m_wireMockRule = new WireMockRule(wireMockConfig().httpsPort(SSL_PORT));

    private ResponseDefinitionBuilder getOKResponse() {
        return aResponse()
                .withHeader("Server", "Apache/2.0.54")
                .withHeader("Last-Modified", "Fri, 16 Jun 2006 01:52:14 GMT")
                .withHeader("ETag", "\"778216aa-2f-aa66cf80\"")
                .withHeader("Content-Type", "text/html")
                .withBody("<html>\r\n<body>\r\n<!-- default -->\r\n</body>\r\n</html>");
    };

    private ResponseDefinitionBuilder getNotFoundResponse() {
        return aResponse()
                .withStatus(404)
                .withHeader("Last-Modified", "Fri, 16 Jun 2006 01:52:14 GMT")
                .withHeader("ETag", "\"778216aa-2f-aa66cf80\"")
                .withHeader("Content-Type", "text/html")
                .withBody("<html>\r\n<body>\r\n<!-- default -->\r\n</body>\r\n</html>");
    }


    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        m_detector = m_detectorFactory.createDetector();

        /* make sure defaults are initialized */
        m_detector.setPort(SSL_PORT);
        m_detector.setUseSSLFilter(true);
        m_detector.setUrl("/");
        m_detector.setCheckRetCode(false);
        m_detector.setMaxRetCode(500);
        m_detector.setRetries(0);
    }

    @After
    public void tearDown() throws IOException {
        if(m_server != null) {
            m_server.stopServer();
            m_server = null;
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Test(timeout=20000)
    public void testDetectorFailWrongPort() throws Exception {
        m_detector.setPort(2000);
        m_detector.init();

        stubFor(get(urlEqualTo("/")).willReturn(getOKResponse()));
        assertFalse(doCheck(m_detector.isServiceDetected(InetAddressUtils.getLocalHostAddress())));
    }

    @Test(timeout=20000)
    public void testDetectorFailNotAServerResponse() throws Exception {
        m_detector.init();

        stubFor(get(urlEqualTo("/")).willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));
        assertFalse(doCheck(m_detector.isServiceDetected(InetAddressUtils.getLocalHostAddress())));
    }

    @Test(timeout=20000)
    public void testDetectorFailNotFoundResponseMaxRetCode399() throws Exception {
        m_detector.setCheckRetCode(true);
        m_detector.setUrl("/blog");
        m_detector.setMaxRetCode(301);
        m_detector.init();

        stubFor(get(urlEqualTo("/blog")).willReturn(getNotFoundResponse()));

        assertFalse(doCheck(m_detector.isServiceDetected(InetAddressUtils.getLocalHostAddress())));
    }

    @Test(timeout=20000)
    public void testDetectorSucessMaxRetCode399() throws Exception {
        m_detector.setCheckRetCode(true);
        m_detector.setUrl("/blog");
        m_detector.setMaxRetCode(399);
        m_detector.init();

        stubFor(get(urlEqualTo("/blog")).willReturn(getOKResponse()));

        assertTrue(doCheck(m_detector.isServiceDetected(InetAddressUtils.getLocalHostAddress())));
    }

    @Test(timeout=20000)
    public void testDetectorFailMaxRetCodeBelow200() throws Exception {
        m_detector.setCheckRetCode(true);
        m_detector.setUrl("/blog");
        m_detector.setMaxRetCode(199);
        m_detector.init();

        stubFor(get(urlEqualTo("/blog")).willReturn(getOKResponse()));

        assertFalse(doCheck(m_detector.isServiceDetected(InetAddressUtils.getLocalHostAddress())));
    }

    @Test(timeout=20000)
    public void testDetectorMaxRetCode600() throws Exception {
        m_detector.setCheckRetCode(true);
        m_detector.setMaxRetCode(600);
        m_detector.init();

        stubFor(get(urlEqualTo("/")).willReturn(getOKResponse()));

        assertTrue(doCheck(m_detector.isServiceDetected(InetAddressUtils.getLocalHostAddress())));
    }


    @Test(timeout=20000)
    public void testDetectorSucessCheckCodeTrue() throws Exception {
        m_detector.setCheckRetCode(true);
        m_detector.setUrl("http://localhost/");
        m_detector.setPort(SSL_PORT);
        m_detector.init();
        m_detector.setIdleTime(1000);

        stubFor(get(urlEqualTo("/")).willReturn(getOKResponse()));

        assertTrue(doCheck(m_detector.isServiceDetected(InetAddressUtils.getLocalHostAddress())));
    }

    @Test(timeout=20000)
    public void testDetectorSuccessCheckCodeFalse() throws Exception {
        m_detector.setCheckRetCode(false);
        m_detector.init();

        stubFor(get(urlEqualTo("/")).willReturn(getOKResponse()));

        assertTrue(doCheck(m_detector.isServiceDetected(InetAddressUtils.getLocalHostAddress())));
    }

    @Test(timeout=20000)
    public void testDetectorSuccess() throws Exception {
        stubFor(get(urlEqualTo("/")).willReturn(getOKResponse()));

        m_detector.init();
        assertTrue(doCheck(m_detector.isServiceDetected(InetAddressUtils.getLocalHostAddress())));
    }

    /**
     * @param serviceDetected
     * @return
     * @throws InterruptedException 
     */
    private boolean doCheck(DetectFuture serviceDetected) throws InterruptedException {
        DetectFuture future = serviceDetected;
        future.awaitFor();

        return future.isServiceDetected();
    }
}
