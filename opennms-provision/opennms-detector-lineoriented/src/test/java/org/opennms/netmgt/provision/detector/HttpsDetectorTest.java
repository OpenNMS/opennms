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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/detectors.xml"})
public class HttpsDetectorTest {

    @Autowired
    private HttpsDetectorFactory m_detectorFactory;
    
    private HttpsDetector m_detector;

    @Rule
    public WireMockRule m_wireMockRule = new WireMockRule(wireMockConfig()
            .dynamicPort()
            .dynamicHttpsPort());

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
        m_detector = m_detectorFactory.createDetector(new HashMap<>());

        /* make sure defaults are initialized */
        m_detector.setPort(m_wireMockRule.httpsPort());
        m_detector.setUseSSLFilter(true);
        m_detector.setUrl("/");
        m_detector.setCheckRetCode(false);
        m_detector.setMaxRetCode(500);
        m_detector.setRetries(0);
    }

    @After
    public void tearDown() {
        if(m_detector != null) {
            m_detector.dispose();
            m_detector = null;
        }
    }

    @Test(timeout=20000)
    public void testDetectorFailWrongPort() throws Exception {
        m_detector.setPort(2000);
        m_detector.init();

        m_wireMockRule.stubFor(get(urlEqualTo("/")).willReturn(getOKResponse()));
        assertFalse(doCheck(m_detector.isServiceDetected(InetAddressUtils.getLocalHostAddress())));
    }

    @Test(timeout=20000)
    public void testDetectorFailNotAServerResponse() throws Exception {
        m_detector.init();

        m_wireMockRule.stubFor(get(urlEqualTo("/")).willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));
        assertFalse(doCheck(m_detector.isServiceDetected(InetAddressUtils.getLocalHostAddress())));
    }

    @Test(timeout=20000)
    public void testDetectorFailNotFoundResponseMaxRetCode399() throws Exception {
        m_detector.setCheckRetCode(true);
        m_detector.setUrl("/blog");
        m_detector.setMaxRetCode(301);
        m_detector.init();

        m_wireMockRule.stubFor(get(urlEqualTo("/blog")).willReturn(getNotFoundResponse()));

        assertFalse(doCheck(m_detector.isServiceDetected(InetAddressUtils.getLocalHostAddress())));
    }

    @Test(timeout=20000)
    public void testDetectorSucessMaxRetCode399() throws Exception {
        m_detector.setCheckRetCode(true);
        m_detector.setUrl("/blog");
        m_detector.setMaxRetCode(399);
        m_detector.init();

        m_wireMockRule.stubFor(get(urlEqualTo("/blog")).willReturn(getOKResponse()));

        assertTrue(doCheck(m_detector.isServiceDetected(InetAddressUtils.getLocalHostAddress())));
    }

    @Test(timeout=20000)
    public void testDetectorFailMaxRetCodeBelow200() throws Exception {
        m_detector.setCheckRetCode(true);
        m_detector.setUrl("/blog");
        m_detector.setMaxRetCode(199);
        m_detector.init();

        m_wireMockRule.stubFor(get(urlEqualTo("/blog")).willReturn(getOKResponse()));

        assertFalse(doCheck(m_detector.isServiceDetected(InetAddressUtils.getLocalHostAddress())));
    }

    @Test(timeout=20000)
    public void testDetectorMaxRetCode600() throws Exception {
        m_detector.setCheckRetCode(true);
        m_detector.setMaxRetCode(600);
        m_detector.init();

        m_wireMockRule.stubFor(get(urlEqualTo("/")).willReturn(getOKResponse()));

        assertTrue(doCheck(m_detector.isServiceDetected(InetAddressUtils.getLocalHostAddress())));
    }


    @Test(timeout=20000)
    public void testDetectorSucessCheckCodeTrue() throws Exception {
        m_detector.setCheckRetCode(true);
        m_detector.setUrl("http://localhost/");
        m_detector.setPort(m_wireMockRule.httpsPort());
        m_detector.init();
        m_detector.setIdleTime(1000);

        m_wireMockRule.stubFor(get(urlEqualTo("/")).willReturn(getOKResponse()));

        assertTrue(doCheck(m_detector.isServiceDetected(InetAddressUtils.getLocalHostAddress())));
    }

    @Test(timeout=20000)
    public void testDetectorSuccessCheckCodeFalse() throws Exception {
        m_detector.setCheckRetCode(false);
        m_detector.init();

        m_wireMockRule.stubFor(get(urlEqualTo("/")).willReturn(getOKResponse()));

        assertTrue(doCheck(m_detector.isServiceDetected(InetAddressUtils.getLocalHostAddress())));
    }

    @Test(timeout=20000)
    public void testDetectorSuccess() throws Exception {
        m_wireMockRule.stubFor(get(urlEqualTo("/")).willReturn(getOKResponse()));

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
