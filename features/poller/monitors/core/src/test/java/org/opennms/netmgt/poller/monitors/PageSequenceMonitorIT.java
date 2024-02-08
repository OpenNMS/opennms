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
package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.MockLogger;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.opennms.core.utils.Base64;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 *
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:/META-INF/opennms/emptyContext.xml")
@JUnitConfigurationEnvironment
@JUnitHttpServer(port=10342)
public class PageSequenceMonitorIT {

    AbstractServiceMonitor m_monitor;
    Map<String, Object> m_params;

    @Before
    public void setUp() throws Exception {
        final Properties props = new Properties();
        props.put(MockLogger.LOG_KEY_PREFIX + "org.apache.http.client.protocol.ResponseProcessCookies", "ERROR");
        MockLogAppender.setupLogging(props);

        m_monitor = new PageSequenceMonitor();

        m_params = new HashMap<String, Object>();
        m_params.put("timeout", "8000");
        m_params.put("retries", "1");
    }

    protected MonitoredService getHttpService(String hostname) throws Exception {
        return getHttpService(hostname, InetAddressUtils.addr(hostname));
    }
    
    protected MonitoredService getHttpService(String hostname, InetAddress inetAddress) throws Exception {
        return new MockMonitoredService(1, hostname, inetAddress, "HTTP");
    }

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    public void testSimple() throws Exception {
        setPageSequenceParam("localhost");
        PollStatus googleStatus = m_monitor.poll(getHttpService("localhost"), m_params);
        assertTrue("Expected available but was "+googleStatus+": reason = "+googleStatus.getReason(), googleStatus.isAvailable());
        assertTrue("Expected a DS called 'response-time' but did not find one", googleStatus.getProperties().containsKey(PollStatus.PROPERTY_RESPONSE_TIME));
    }

    @Test
    public void testSimpleBogus() throws Exception {
        setPageSequenceParam(null);
        m_params.put("timeout", "500");
        m_params.put("retries", "0");
        PollStatus notLikely = m_monitor.poll(getHttpService("bogus", InetAddressUtils.addr("1.1.1.1")), m_params);
        assertTrue("Should not be available", notLikely.isUnavailable());
        // Check to make sure that the connection message is nice
        assertEquals("connect to 1.1.1.1:10342 [/1.1.1.1] failed: connect timed out", notLikely.getReason().toLowerCase());
        assertTrue("Expected a DS called 'response-time' but did not find one", notLikely.getProperties().containsKey(PollStatus.PROPERTY_RESPONSE_TIME));
    }

    private void setPageSequenceParam(String virtualHost) {
        String virtualHostParam;
        if (virtualHost == null) {
            virtualHostParam = "http-version=\"1.0\"";
        } else {
            virtualHostParam = "virtual-host=\"" + virtualHost + "\"";
        }

        m_params.put("page-sequence", "" +
            "<?xml version=\"1.0\"?>" +
            "<page-sequence>\n" + 
            "  <page path=\"/index.html\" port=\"10342\" user-agent=\"Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)\" successMatch=\"It was written by monkeys.\" " + virtualHostParam + "/>\n" + 
            "</page-sequence>\n");
    }

    @Test
    @JUnitHttpServer(port=10342, webapps=@Webapp(context="/opennms", path="src/test/resources/loginTestWar"))
    public void testLogin() throws Exception {

        m_params.put("page-sequence", "" +
            "<?xml version=\"1.0\"?>" +
            "<page-sequence>\n" + 
            "  <page virtual-host=\"localhost\" path=\"/opennms/\" port=\"10342\" successMatch=\"Password\" />\n" + 
            "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_check\" port=\"10342\" method=\"POST\" response-range=\"300-399\">\n" + 
            "    <parameter key=\"j_username\" value=\"demo\"/>\n" + 
            "    <parameter key=\"j_password\" value=\"demo\"/>\n" + 
            "  </page>\n" + 
            "  <page virtual-host=\"localhost\" path=\"/opennms/events.html\" port=\"10342\" successMatch=\"Event Queries\" />\n" + 
            "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_logout\" port=\"10342\" successMatch=\"Login with Username and Password\" />\n" + 
            "</page-sequence>\n");

        PollStatus status = m_monitor.poll(getHttpService("localhost"), m_params);
        assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
        assertTrue("Expected a DS called 'response-time' but did not find one", status.getProperties().containsKey(PollStatus.PROPERTY_RESPONSE_TIME));
    }

    @Test
    @JUnitHttpServer(port=10342, webapps=@Webapp(context="/opennms", path="src/test/resources/loginTestWar"))
    public void testLoginDynamicCredentials() throws Exception {
        m_params.put("page-sequence", "" +
            "<?xml version=\"1.0\"?>" +
            "<page-sequence>\n" + 
            "  <page path=\"/opennms/\" port=\"10342\" virtual-host=\"localhost\" successMatch=\"(?s)&lt;hea(.)&gt;&lt;titl(.)&gt;.*&lt;/for(.)&gt;&lt;/b(.)dy&gt;\">\n" +
            "    <session-variable name=\"ltr1\" match-group=\"1\" />\n" +
            "    <session-variable name=\"ltr2\" match-group=\"2\" />\n" +
            "    <session-variable name=\"ltr3\" match-group=\"3\" />\n" +
            "    <session-variable name=\"ltr4\" match-group=\"4\" />\n" +
            "  </page>\n" +
            "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_check\" port=\"10342\" method=\"POST\" response-range=\"300-399\">\n" +
            "    <parameter key=\"j_username\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
            "    <parameter key=\"j_password\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
            "  </page>\n" + 
            "  <page virtual-host=\"localhost\" path=\"/opennms/events.html\" port=\"10342\" successMatch=\"Event Queries\" />\n" + 
            "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_logout\" port=\"10342\" successMatch=\"Login with Username and Password\" />\n" + 
            "</page-sequence>\n");

        PollStatus status = m_monitor.poll(getHttpService("localhost"), m_params);
        assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
        assertTrue("Expected a DS called 'response-time' but did not find one", status.getProperties().containsKey(PollStatus.PROPERTY_RESPONSE_TIME));
    }

    @Test
    @JUnitHttpServer(port=10342, webapps=@Webapp(context="/opennms", path="src/test/resources/loginTestWar"))
    public void testLoginDynamicCredentialsTwice() throws Exception {
        m_params.put("page-sequence", "" +
            "<?xml version=\"1.0\"?>" +
            "<page-sequence>\n" + 
            "  <page path=\"/opennms/\" port=\"10342\" virtual-host=\"localhost\" successMatch=\"(?s)&gt;Login (.)(.)(.)(.) Username and Password&lt;\">\n" +
            "    <session-variable name=\"ltr1\" match-group=\"1\" />\n" +
            "    <session-variable name=\"ltr2\" match-group=\"2\" />\n" +
            "    <session-variable name=\"ltr3\" match-group=\"3\" />\n" +
            "    <session-variable name=\"ltr4\" match-group=\"4\" />\n" +
            "  </page>\n" +
            "  <!-- Pick out the letters w-i-t-h to try and log in, this will fail -->\n" +
            "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_check\" response-range=\"300-399\" locationMatch=\"/opennms/login\\?error\" port=\"10342\" method=\"POST\">\n" +
            "    <parameter key=\"j_username\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
            "    <parameter key=\"j_password\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
            "  </page>\n" + 
            "  <page virtual-host=\"localhost\" path=\"/opennms/login\" query=\"error\" port=\"10342\" failureMatch=\"(?s)Log out\" failureMessage=\"Login should have failed but did not\" successMatch=\"Your login attempt was not successful\"/>\n" +
            "  <!-- Pick out the letters d-e-m-o to try and log in, this will succeed -->\n" +
            "  <page path=\"/opennms/\" port=\"10342\" virtual-host=\"localhost\" successMatch=\"(?s)&lt;hea(.)&gt;&lt;titl(.)&gt;.*&lt;/for(.)&gt;&lt;/b(.)dy&gt;\">\n" +
            "    <session-variable name=\"ltr1\" match-group=\"1\" />\n" +
            "    <session-variable name=\"ltr2\" match-group=\"2\" />\n" +
            "    <session-variable name=\"ltr3\" match-group=\"3\" />\n" +
            "    <session-variable name=\"ltr4\" match-group=\"4\" />\n" +
            "  </page>\n" +
            "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_check\" response-range=\"300-399\" port=\"10342\" method=\"POST\">\n" +
            "    <parameter key=\"j_username\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
            "    <parameter key=\"j_password\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
            "  </page>\n" + 
            "  <page virtual-host=\"localhost\" path=\"/opennms/events.html\" port=\"10342\" successMatch=\"Event Queries\" />\n" + 
            "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_logout\" port=\"10342\" successMatch=\"Login with Username and Password\" />\n" + 
            "</page-sequence>\n");

        try {
            PollStatus status = m_monitor.poll(getHttpService("localhost"), m_params);
            assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
            assertTrue("Expected a DS called 'response-time' but did not find one", status.getProperties().containsKey(PollStatus.PROPERTY_RESPONSE_TIME));
        } finally {
            // Print some debug output if necessary
        }
    }

    @Test
    @JUnitHttpServer(port=10342, webapps=@Webapp(context="/opennms", path="src/test/resources/loginTestWar"))
    public void testLoginDynamicCredentialsRedirectPost() throws Exception {
        m_params.put("page-sequence", "" +
            "<?xml version=\"1.0\"?>" +
            "<page-sequence>\n" + 
            "  <page path=\"/opennms/\" port=\"10342\" virtual-host=\"localhost\" successMatch=\"(?s)&lt;hea(.)&gt;&lt;titl(.)&gt;.*&lt;/for(.)&gt;&lt;/b(.)dy&gt;\">\n" +
            "    <session-variable name=\"ltr1\" match-group=\"1\" />\n" +
            "    <session-variable name=\"ltr2\" match-group=\"2\" />\n" +
            "    <session-variable name=\"ltr3\" match-group=\"3\" />\n" +
            "    <session-variable name=\"ltr4\" match-group=\"4\" />\n" +
            "  </page>\n" +
            "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_check\" port=\"10342\" method=\"POST\" failureMatch=\"(?s)Your login attempt was not successful.*Reason: ([^&lt;]*)\" failureMessage=\"Login in Failed: ${1}\">\n" +
            "    <parameter key=\"j_username\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
            "    <parameter key=\"j_password\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
            "  </page>\n" + 
            "  <page virtual-host=\"localhost\" path=\"/opennms/events.html\" port=\"10342\" successMatch=\"Event Queries\" />\n" + 
            "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_logout\" port=\"10342\" successMatch=\"Login with Username and Password\" />\n" + 
            "</page-sequence>\n");

        Map<String,Object> params = new HashMap<String,Object>();
        for (Entry<String,Object> entry : m_params.entrySet()) {
            params.put(entry.getKey(), entry.getValue());
        }

        try {
            PollStatus status = m_monitor.poll(getHttpService("localhost"), params);
            assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
            assertTrue("Expected a DS called 'response-time' but did not find one", status.getProperties().containsKey(PollStatus.PROPERTY_RESPONSE_TIME));
        } finally {
            // Print some debug output if necessary
        }
    }

    @Test
    @JUnitHttpServer(port=10342, webapps=@Webapp(context="/opennms", path="src/test/resources/loginTestWar"))
    public void testRedirectLocationMatch() throws Exception {
        m_params.put("page-sequence", "" +
            "<?xml version=\"1.0\"?>" +
            "<page-sequence>\n" + 
            "  <page path=\"/opennms/\" port=\"10342\" virtual-host=\"localhost\" successMatch=\"(?s)&lt;hea(.)&gt;&lt;titl(.)&gt;.*&lt;/for(.)&gt;&lt;/b(.)dy&gt;\">\n" +
            "    <session-variable name=\"ltr1\" match-group=\"1\" />\n" +
            "    <session-variable name=\"ltr2\" match-group=\"2\" />\n" +
            "    <session-variable name=\"ltr3\" match-group=\"3\" />\n" +
            "    <session-variable name=\"ltr4\" match-group=\"4\" />\n" +
            "  </page>\n" +
            "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_check\" port=\"10342\" method=\"POST\" response-range=\"300-399\" locationMatch=\"/opennms/\">\n" +
            "    <parameter key=\"j_username\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
            "    <parameter key=\"j_password\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
            "  </page>\n" + 
            "</page-sequence>\n");

        PollStatus status = m_monitor.poll(getHttpService("localhost"), m_params);
        assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
        assertTrue("Expected a DS called 'response-time' but did not find one", status.getProperties().containsKey(PollStatus.PROPERTY_RESPONSE_TIME));
    }

    @Test
    @JUnitHttpServer(port=10342, webapps=@Webapp(context="/opennms", path="src/test/resources/loginTestWar"))
    public void testRedirectLocationDoesNotMatch() throws Exception {
        m_params.put("page-sequence", "" +
            "<?xml version=\"1.0\"?>" +
            "<page-sequence>\n" + 
            "  <page path=\"/opennms/\" port=\"10342\" virtual-host=\"localhost\" successMatch=\"(?s)&lt;hea(.)&gt;&lt;titl(.)&gt;.*&lt;/for(.)&gt;&lt;/b(.)dy&gt;\">\n" +
            "    <session-variable name=\"ltr1\" match-group=\"1\" />\n" +
            "    <session-variable name=\"ltr2\" match-group=\"2\" />\n" +
            "    <session-variable name=\"ltr3\" match-group=\"3\" />\n" +
            "    <session-variable name=\"ltr4\" match-group=\"4\" />\n" +
            "  </page>\n" +
            "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_check\" port=\"10342\" method=\"POST\" response-range=\"300-399\" locationMatch=\"/opensadfnms/\">\n" +
            "    <parameter key=\"j_username\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
            "    <parameter key=\"j_password\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
            "  </page>\n" + 
            "</page-sequence>\n");

        PollStatus status = m_monitor.poll(getHttpService("localhost"), m_params);
        assertTrue("Expected down but was "+status+": reason = "+status.getReason(), status.isDown());
        assertEquals("Failed to find '/opensadfnms/' in Location: header at http://127.0.0.1:10342/opennms/j_spring_security_check", status.getReason());
        assertTrue("Expected a DS called 'response-time' but did not find one", status.getProperties().containsKey(PollStatus.PROPERTY_RESPONSE_TIME));
    }

    @Test
    @JUnitHttpServer(port=10342, webapps=@Webapp(context="/opennms", path="src/test/resources/loginTestWar"))
    public void testDsNamePerPage() throws Exception {
        m_params.put("page-sequence", "" +
            "<?xml version=\"1.0\"?>" +
            "<page-sequence>\n" + 
            "  <page path=\"/opennms/\" ds-name=\"test1\" port=\"10342\" virtual-host=\"localhost\" successMatch=\"&lt;title&gt;(.*?)&lt;/title&gt;\" />\n" +
            "  <page path=\"/opennms/j_spring_security_check\" ds-name=\"test2\" port=\"10342\" virtual-host=\"localhost\" successMatch=\"&lt;title&gt;(.*?)&lt;/title&gt;\" />\n" +
                "</page-sequence>\n");

        PollStatus status = m_monitor.poll(getHttpService("localhost"), m_params);
        assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
        assertTrue("Expected three DSes", (3 == status.getProperties().size()));
        assertTrue("Expected a DS called 'test1' but did not find one", status.getProperties().containsKey("test1"));
        assertTrue("Expected a DS called 'test2' but did not find one", status.getProperties().containsKey("test2"));
        assertTrue("Expected a DS called 'response-time' but did not find one", status.getProperties().containsKey(PollStatus.PROPERTY_RESPONSE_TIME));
    }

    @Test
    @JUnitHttpServer(port=10342, webapps=@Webapp(context="/opennms", path="src/test/resources/loginTestWar"))
    public void testRequireIPv6() throws Exception {
        assumeTrue(!Boolean.getBoolean("skipIpv6Tests"));
        m_params.put("page-sequence", "" +
            "<?xml version=\"1.0\"?>" +
            "<page-sequence>\n" + 
            "  <page host=\"localhost\" virtual-host=\"localhost\" path=\"/opennms/\" port=\"10342\" requireIPv6=\"true\"/>\n" +
            "</page-sequence>\n");

        PollStatus status = m_monitor.poll(getHttpService("localhost"), m_params);
        assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
        assertTrue("Expected a DS called 'response-time' but did not find one", status.getProperties().containsKey(PollStatus.PROPERTY_RESPONSE_TIME));
    }

    @Test
    @JUnitHttpServer(port=10342, webapps=@Webapp(context="/opennms", path="src/test/resources/loginTestWar"))
    public void testRequireIPv4() throws Exception {
        m_params.put("page-sequence", "" +
            "<?xml version=\"1.0\"?>" +
            "<page-sequence>\n" + 
            "  <page host=\"localhost\" virtual-host=\"localhost\" path=\"/opennms/\" port=\"10342\" requireIPv4=\"true\"/>\n" +
            "</page-sequence>\n");

        PollStatus status = m_monitor.poll(getHttpService("localhost"), m_params);
        assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
        assertTrue("Expected a DS called 'response-time' but did not find one", status.getProperties().containsKey(PollStatus.PROPERTY_RESPONSE_TIME));
    }

    @Test
    @JUnitHttpServer(basicAuth = true, port = 10342, webapps = @Webapp(context = "/opennms", path = "src/test/resources/loginTestWar"))
    public void testHeaders() throws Exception {
        final Map<String, Object> params1 = new HashMap<>(m_params);
        params1.put("page-sequence", "" +
                "<?xml version=\"1.0\"?>" +
                "<page-sequence>\n" +
                "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_check\" port=\"10342\" method=\"POST\" response-range=\"300-399\" locationMatch=\"/opennms/\">\n" +
                "    <header name=\"Authorization\" value=\"Basic " + new String(Base64.encodeBase64(("admin:istrator").getBytes())) + "\" />\n" +
                "  </page>\n" +
                "</page-sequence>\n");
        final PollStatus status1 = m_monitor.poll(getHttpService("localhost"), params1);
        assertEquals(PollStatus.SERVICE_AVAILABLE, status1.getStatusCode());
        assertNull(status1.getReason());

        final Map<String, Object> params2 = new HashMap<>(m_params);
        params2.put("page-sequence", "" +
                "<?xml version=\"1.0\"?>" +
                "<page-sequence>\n" +
                "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_check\" port=\"10342\" method=\"POST\" response-range=\"300-399\" locationMatch=\"/opennms/\">\n" +
                "    <header name=\"Authorization\" value=\"Basic " + new String(Base64.encodeBase64(("admin:wrong").getBytes())) + "\" />\n" +
                "  </page>\n" +
                "</page-sequence>\n");
        final PollStatus status2 = m_monitor.poll(getHttpService("localhost"), params2);
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status2.getStatusCode());
        assertNotNull(status2.getReason());
    }

    @Test
    @JUnitHttpServer(basicAuth = true, port = 10342, webapps = @Webapp(context = "/opennms", path = "src/test/resources/loginTestWar"))
    public void testUserInfoToBasicAuth() throws Exception {
        final Map<String, Object> params1 = new HashMap<>(m_params);
        String correctUserInfo = "admin:istrator";
        params1.put("page-sequence", "" +
                "<?xml version=\"1.0\"?>" +
                "<page-sequence>\n" +
                "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_check\" port=\"10342\" method=\"POST\" response-range=\"300-399\" locationMatch=\"/opennms/\" user-info=\"" + correctUserInfo + "\" />\n" +
                "</page-sequence>\n");
        final PollStatus status1 = m_monitor.poll(getHttpService("localhost"), params1);
        assertEquals(PollStatus.SERVICE_AVAILABLE, status1.getStatusCode());
        assertNull(status1.getReason());

        final Map<String, Object> params2 = new HashMap<>(m_params);
        String wrongUserInfo = "admin:wrong";
        params2.put("page-sequence", "" +
                "<?xml version=\"1.0\"?>" +
                "<page-sequence>\n" +
                "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_check\" port=\"10342\" method=\"POST\" response-range=\"300-399\" locationMatch=\"/opennms/\" user-info=\"" + wrongUserInfo + "\" />\n" +
                "</page-sequence>\n");
        final PollStatus status2 = m_monitor.poll(getHttpService("localhost"), params2);
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status2.getStatusCode());
        assertNotNull(status2.getReason());
    }
}
