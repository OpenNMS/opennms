/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.MockLogger;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 *
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:META-INF/opennms/emptyContext.xml")
@JUnitConfigurationEnvironment
@JUnitHttpServer(port=10342)
public class PageSequenceMonitorTest {

    AbstractServiceMonitor m_monitor;
    Map<String, Object> m_params;

    @Before
    public void setUp() throws Exception {
        final Properties props = new Properties();
        props.put(MockLogger.LOG_KEY_PREFIX + "org.apache.http.client.protocol.ResponseProcessCookies", "ERROR");
        MockLogAppender.setupLogging(props);

        m_monitor = new PageSequenceMonitor();
        m_monitor.initialize(Collections.<String, Object>emptyMap());

        m_params = new HashMap<String, Object>();
        m_params.put("timeout", "8000");
        m_params.put("retries", "1");
    }

    protected MonitoredService getHttpService(String hostname) throws Exception {
        return getHttpService(hostname, InetAddressUtils.addr(hostname));
    }
    
    protected MonitoredService getHttpService(String hostname, InetAddress inetAddress) throws Exception {
        MonitoredService svc = new MockMonitoredService(1, hostname, inetAddress, "HTTP");
    	m_monitor.initialize(svc);
    	return svc;
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
    }

    @Test
    public void testSimpleBogus() throws Exception {
        setPageSequenceParam(null);
        m_params.put("timeout", "500");
        m_params.put("retries", "0");
        PollStatus notLikely = m_monitor.poll(getHttpService("bogus", InetAddressUtils.addr("1.1.1.1")), m_params);
        assertTrue("should not be available", notLikely.isUnavailable());
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
    @Ignore("EBay tests stopped working, we REALLY need to make our own repeatable version of this test")
    public void testHttps() throws Exception {
        m_params.put("page-sequence", "" +
            "<?xml version=\"1.0\"?>" +
            "<page-sequence>\n" + 
            "  <page scheme=\"https\" host=\"scgi.ebay.com\" path=\"/ws/eBayISAPI.dll\" query=\"RegisterEnterInfo\" port=\"443\" user-agent=\"Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)\" successMatch=\"ebaystatic.com/\"/>\n" + 
            "</page-sequence>\n");

        try {
            PollStatus googleStatus = m_monitor.poll(getHttpService("scgi.ebay.com"), m_params);
            assertTrue("Expected available but was "+googleStatus+": reason = "+googleStatus.getReason(), googleStatus.isAvailable());
        } finally {
            // Print some debug output if necessary
        }
    }

    @Test
    @Ignore("EBay tests stopped working, we REALLY need to make our own repeatable version of this test")
    public void testHttpsWithHostValidation() throws Exception {
        m_params.put("page-sequence", "" +
            "<?xml version=\"1.0\"?>" +
            "<page-sequence>\n" + 
            "  <page scheme=\"https\" path=\"/ws/eBayISAPI.dll\" query=\"RegisterEnterInfo\" port=\"443\" user-agent=\"Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)\" successMatch=\"ebaystatic.com/\" virtual-host=\"scgi.ebay.com\" disable-ssl-verification=\"false\"/>\n" + 
            "</page-sequence>\n");

        try {
            m_monitor.poll(getHttpService("scgi.ebay.com"), m_params);
            fail("Expected SSL host mismatch error");
        } catch (Throwable e) {
            assertTrue("Wrong exception caught: " + e.getClass().getName(), e instanceof AssertionError);
        }
    }

    @Test
    @Ignore("EBay tests stopped working, we REALLY need to make our own repeatable version of this test")
    public void testHttpsWithoutHostValidation() throws Exception {
        m_params.put("page-sequence", "" +
            "<?xml version=\"1.0\"?>" +
            "<page-sequence>\n" + 
            "  <page scheme=\"https\" path=\"/ws/eBayISAPI.dll\" query=\"RegisterEnterInfo\" port=\"443\" user-agent=\"Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)\" successMatch=\"ebaystatic.com/\" virtual-host=\"scgi.ebay.com\"/>\n" + 
            "</page-sequence>\n");

        try {
            PollStatus googleStatus = m_monitor.poll(getHttpService("scgi.ebay.com"), m_params);
            assertTrue("Expected available but was "+googleStatus+": reason = "+googleStatus.getReason(), googleStatus.isAvailable());
        } finally {
            // Print some debug output if necessary
        }

        m_params.put("page-sequence", "" +
            "<?xml version=\"1.0\"?>" +
            "<page-sequence>\n" + 
            "  <page scheme=\"https\" path=\"/ws/eBayISAPI.dll\" query=\"RegisterEnterInfo\" port=\"443\" user-agent=\"Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)\" successMatch=\"ebaystatic.com/\" virtual-host=\"scgi.ebay.com\" disable-host-verification=\"true\"/>\n" + 
            "</page-sequence>\n");

        try {
            PollStatus googleStatus = m_monitor.poll(getHttpService("scgi.ebay.com"), m_params);
            assertTrue("Expected available but was "+googleStatus+": reason = "+googleStatus.getReason(), googleStatus.isAvailable());
        } finally {
            // Print some debug output if necessary
        }
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

    }

    @Test
    public void testVirtualHost() throws Exception {
        m_params.put("page-sequence", "" +
            "<?xml version=\"1.0\"?>" +
            "<page-sequence>\n" + 
            "  <page user-agent=\"Donald\" path=\"/\" port=\"80\" successMatch=\"Get the Network to Work\" virtual-host=\"www.opennms.com\"/>\n" + 
            "</page-sequence>\n");

        PollStatus status = m_monitor.poll(getHttpService("www.opennms.com"), m_params);
        assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
    }

    @Test
    @Ignore("This test doesn't work against the new version of the website")
    public void testVirtualHostBadBehaviorForWordpressPlugin() throws Exception {
        m_params.put("page-sequence", "" +
            "<?xml version=\"1.0\"?>" +
            "<page-sequence>\n" + 
            "  <page path=\"/\" port=\"80\" successMatch=\"Get the Network to Work\" user-agent=\"Jakarta Commons-HttpClient/3.0.1\" virtual-host=\"www.opennms.com\"/>\n" + 
            "</page-sequence>\n");

        PollStatus status = m_monitor.poll(getHttpService("www.opennms.com"), m_params);
        assertTrue("Expected unavailable but was "+status+": reason = "+status.getReason(), status.isDown());
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
            "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_check\" response-range=\"300-399\" locationMatch=\"/opennms/spring_security_login\\?login_error\" port=\"10342\" method=\"POST\">\n" +
            "    <parameter key=\"j_username\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
            "    <parameter key=\"j_password\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
            "  </page>\n" + 
            "  <page virtual-host=\"localhost\" path=\"/opennms/spring_security_login\" query=\"login_error\" port=\"10342\" failureMatch=\"(?s)Log out\" failureMessage=\"Login should have failed but did not\" successMatch=\"Your login attempt was not successful\"/>\n" +
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
    }

    @Test
    @JUnitHttpServer(port=10342, webapps=@Webapp(context="/opennms", path="src/test/resources/loginTestWar"))
    public void testRequireIPv6() throws Exception {
        m_params.put("page-sequence", "" +
            "<?xml version=\"1.0\"?>" +
            "<page-sequence>\n" + 
            "  <page host=\"localhost\" virtual-host=\"localhost\" path=\"/opennms/\" port=\"10342\" requireIPv6=\"true\"/>\n" +
            "</page-sequence>\n");

        PollStatus status = m_monitor.poll(getHttpService("localhost"), m_params);
        assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
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
    }
}
