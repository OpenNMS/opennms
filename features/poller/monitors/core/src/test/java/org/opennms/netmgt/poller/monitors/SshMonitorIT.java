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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.PatternSyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.ssh.SshServerDataProvider;
import org.opennms.core.test.ssh.SshServerDataProviderAware;
import org.opennms.core.test.ssh.annotations.JUnitSshServer;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/opennms/emptyContext.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml"})
@JUnitConfigurationEnvironment
@JUnitSshServer
public class SshMonitorIT implements SshServerDataProviderAware {
    public static final InetAddress HOST_TO_TEST = InetAddressUtils.getLocalHostAddress();

    private SshServerDataProvider dataProvider;

    @Before
    public void setUp() {
        Properties props = new Properties();
        // props.setProperty("log4j.logger.org.apache.sshd", "DEBUG");
        MockLogAppender.setupLogging(props);
    }

    @Test
    public void testPoll() throws UnknownHostException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", HOST_TO_TEST, "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("port", dataProvider.getPort());

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Up"), ps.isUp());
        assertFalse(createAssertMessage(ps, "not Down"), ps.isDown());
    }

    @Test
    public void testPollWithMatch() throws UnknownHostException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", HOST_TO_TEST, "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("match", "SSH");
        parms.put("port", dataProvider.getPort());

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Up"), ps.isUp());
        assertFalse(createAssertMessage(ps, "not Down"), ps.isDown());
    }

    @Test
    public void testPollWithStarBanner() throws UnknownHostException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", HOST_TO_TEST, "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("banner", "*");
        parms.put("port", dataProvider.getPort());

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Up"), ps.isUp());
        assertFalse(createAssertMessage(ps, "not Down"), ps.isDown());
    }

    @Test
    public void testPollWithRegexpBanner() throws UnknownHostException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", HOST_TO_TEST, "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("banner", "^SSH");
        parms.put("port", dataProvider.getPort());

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Up"), ps.isUp());
        assertFalse(createAssertMessage(ps, "not Down"), ps.isDown());
    }

    @Test
    public void testPollWithBannerOpenSSH() throws UnknownHostException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", HOST_TO_TEST, "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("banner", "OpenSSH");
        parms.put("port", dataProvider.getPort());

        dataProvider.setIdentification("This is not really OpenSSH muahaha");

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Up"), ps.isUp());
        assertFalse(createAssertMessage(ps, "not Down"), ps.isDown());
    }

    @Test
    public void testPollWithBannerMissing() throws UnknownHostException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", HOST_TO_TEST, "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("banner", "OpenNMS");
        parms.put("port", dataProvider.getPort());

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Down"), ps.isDown());
        assertFalse(createAssertMessage(ps, "not Up"), ps.isUp());
    }

    @Test
    public void testPollWithBannerOpenSSHRegexp() throws UnknownHostException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", HOST_TO_TEST, "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("banner", "^SSH\\-2\\.0\\-OpenSSH_\\d+\\.\\d+.*$");
        parms.put("port", dataProvider.getPort());

        dataProvider.setIdentification("OpenSSH_4.20.69");

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Up"), ps.isUp());
        assertFalse(createAssertMessage(ps, "not Down"), ps.isDown());
    }

    @Test
    public void testPollWithInvalidRegexpBanner() throws UnknownHostException, PatternSyntaxException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", HOST_TO_TEST, "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("banner", "^SSH\\-2\\.0\\-OpenSSH_\\d+\\.\\d+\\g$");
        parms.put("port", dataProvider.getPort());

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(ps.isUnavailable());
        assertTrue(createAssertMessage(ps, "Unavailable"), ps.isUnavailable());
    }

    @Test
    public void testPollWithInvalidRegexpMatch() throws UnknownHostException, PatternSyntaxException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", HOST_TO_TEST, "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("banner", "^SSH\\-2\\.0\\-OpenSSH_\\d+\\.\\d+\\g$");
        parms.put("port", dataProvider.getPort());

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Unavailable"), ps.isUnavailable());
    }

    @Test
    public void testPollWithInvalidHost() throws UnknownHostException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", InetAddressUtils.UNPINGABLE_ADDRESS, "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("port", dataProvider.getPort());

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Unavailable"), ps.isUnavailable());
    }

    @Test
    public void testPollWithNoIpAddress() throws UnknownHostException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", null, "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("banner", "OpenNMS");
        parms.put("port", dataProvider.getPort());

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Down"), ps.isDown());
        assertFalse(createAssertMessage(ps, "not Up"), ps.isUp());
    }

    private String createAssertMessage(PollStatus ps, String expectation) {
        return "polled service is " + ps.toString() + " not " + expectation + " due to: " + ps.getReason() + " (do you have an SSH daemon running on " + HOST_TO_TEST + "?)";
    }

    @Override
    public void setSshServerDataProvider(final SshServerDataProvider provider) {
        this.dataProvider = provider;
    }
}
