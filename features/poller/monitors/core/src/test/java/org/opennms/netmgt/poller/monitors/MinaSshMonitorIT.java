/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyPairResourceWriter;

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
        LOG.info("======== BeforeClass {}", MinaSshMonitorIT.class);

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

        keyPairs = List.of(KeyUtils.generateKeyPair(KeyPairProvider.SSH_RSA, 1024));
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
        LOG.info("======== AfterClass {}", MinaSshMonitorIT.class);
    }

    @Before
    public void startUp() throws Exception {
        LOG.info("======== Starting test {}", m_test.getMethodName());
    }

    @After
    public void tearDown() throws Exception {
        LOG.info("======== Finished test {}", m_test.getMethodName());
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
