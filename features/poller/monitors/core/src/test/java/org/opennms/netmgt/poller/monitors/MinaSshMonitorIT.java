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

import java.io.File;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyPairResourceWriter;
import org.springframework.util.FileSystemUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/opennms/emptyContext.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml"})
@JUnitConfigurationEnvironment
public class MinaSshMonitorIT {
    private static final Logger LOG = LoggerFactory.getLogger(MinaSshMonitorIT.class);
    public static final InetAddress HOST_TO_TEST = InetAddressUtils.getLocalHostAddress();
    public static final String HOST_LOCALHOSTNAME = InetAddressUtils.getLocalHostName();

    private static SshServer sshd;
    private static int port;
    private static List<KeyPair> keyPairs;
    private static Path tmpDir;
    private static Path identityFilePath;

    @Rule
    public TestName m_test = new TestName();

    @BeforeClass
    public static void startSshServer() throws Exception {
        LOG.info("======== BeforeClass " + MinaSshMonitorIT.class);

        tmpDir = Files.createTempDirectory(MinaSshMonitorIT.class.getSimpleName());
        identityFilePath = File.createTempFile(MinaSshMonitorIT.class.getSimpleName(), null, tmpDir.toFile()).toPath();

        sshd = SshServer.setUpDefaultServer();
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        sshd.setPasswordAuthenticator((username, password, session) -> {
                boolean result = (username != null) && username.equals(password);
                LOG.info("authenticate(session={}) username={} / password={} - success={}", session, username, password, result);
                return result;
            }
        );
        sshd.setPublickeyAuthenticator((username, key, session) -> {
                String keyType = KeyUtils.getKeyType(key);
                boolean result = false;
                for(KeyPair kp : keyPairs) {
                    if (key.equals(kp.getPublic())) {
                        result = true;
                        break;
                    }
                }
                String fp = KeyUtils.getFingerPrint(key);
                LOG.info("authenticate(session={}) username={} fingerprint={} - success={}", session, username, fp, result);
                return result;
            }
        );
        sshd.start();
        port = sshd.getPort();
        LOG.info("SshServer started on port {}", port);

        keyPairs = Collections.singletonList(KeyUtils.generateKeyPair(KeyPairProvider.SSH_RSA, 1024));
        for(KeyPair kp : keyPairs) {
            String fp = KeyUtils.getFingerPrint(kp.getPublic());
            LOG.info("client fingerprint: {}", fp);
            try (OutputStream out = Files.newOutputStream(identityFilePath)) {
                OpenSSHKeyPairResourceWriter.INSTANCE.writePrivateKey(kp, "comment", null, out);
            }
            LOG.info("client fingerprint: {}, written to {}", fp, identityFilePath);
        }
    }

    @AfterClass
    public static void stopSshServer() throws Exception {
        if (sshd != null) {
            try {
                sshd.stop(true);
            } finally {
                sshd = null;
            }
        }
        //FileSystemUtils.deleteRecursively(tmpDir.toFile());
        LOG.info("======== AfterClass " + MinaSshMonitorIT.class);
    }

    @Before
    public void startUp() throws Exception {
        LOG.info("======== Starting test " + m_test.getMethodName());
    }

    @After
    public void tearDown() throws Exception {
        LOG.info("======== Finished test " + m_test.getMethodName());
    }

    @Test
    public void testPoll() throws UnknownHostException {

        ServiceMonitor sm = new MinaSshMonitor();
        MonitoredService svc = new MockMonitoredService(1, HOST_LOCALHOSTNAME, HOST_TO_TEST, "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("username", "testuser");
        //parms.put("password", "ZrgPjTtymUUTTlu0V7BAVp+5mfQ=");
        parms.put("password", "testuser");
        parms.put("port", port);

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Up", svc), ps.isUp());
        assertFalse(createAssertMessage(ps, "not Down", svc), ps.isDown());
    }

    @Test
    public void testPollWithIdentity() throws UnknownHostException {

        ServiceMonitor sm = new MinaSshMonitor();
        MonitoredService svc = new MockMonitoredService(1, HOST_LOCALHOSTNAME, HOST_TO_TEST, "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("username", "testuser");
        parms.put("identity-file", identityFilePath.toString());
        parms.put("port", port);

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Up", svc), ps.isUp());
        assertFalse(createAssertMessage(ps, "not Down", svc), ps.isDown());
    }

    @Test
    public void testPollWithInvalidHost() throws UnknownHostException {

        ServiceMonitor sm = new MinaSshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "InvalidHostIPAddress", InetAddressUtils.UNPINGABLE_ADDRESS, "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("username", "testuser");
        parms.put("password", "testuser");
        parms.put("port", port);

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Unavailable", svc), ps.isUnavailable());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPollWithNoIpAddress() throws UnknownHostException {

        ServiceMonitor sm = new MinaSshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "NoIpAddress", null, "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("username", "testuser");
        parms.put("password", "testuser");
        parms.put("port", port);

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Down", svc), ps.isDown());
        assertFalse(createAssertMessage(ps, "not Up", svc), ps.isUp());
    }

    private String createAssertMessage(PollStatus ps, String expectation, MonitoredService svc) {
        return "polled service is " + ps.toString() + " not " + expectation + " due to: " + ps.getReason() + " (do you have an SSH daemon running on " + svc.getNodeLabel()+"/"+InetAddressUtils.str(svc.getAddress()) + "?)";
    }
}
