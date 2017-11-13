/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.minion;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.hasSize;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.test.system.api.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

/**
 * Verifies the list of available detectors by parsing the output
 * of the 'provision:list-detectors' command issued in the Karaf shell.
 *
 * We try running this command several times since the feature providing
 * the 'list-detectors' command the feature(s) providing the detectors
 * may take some time to load.
 *
 * @author jwhite
 * @author chandrag
 */
public class DetectorsCommandIT {

    private static TestEnvironment m_testEnvironment;

    private static final Logger LOG = LoggerFactory.getLogger(DetectorsCommandIT.class);

    private ImmutableMap<String, String> expectedDetectors = ImmutableMap.<String, String> builder()
            .put("BGP_Session", "org.opennms.netmgt.provision.detector.snmp.BgpSessionDetector")
            .put("BSF", "org.opennms.netmgt.provision.detector.bsf.BSFDetector")
            .put("CITRIX", "org.opennms.netmgt.provision.detector.simple.CitrixDetector")
            .put("Cisco_IP_SLA", "org.opennms.netmgt.provision.detector.snmp.CiscoIpSlaDetector")
            .put("DNS", "org.opennms.netmgt.provision.detector.datagram.DnsDetector")
            .put("Dell_OpenManageChassis", "org.opennms.netmgt.provision.detector.snmp.OpenManageChassisDetector")
            .put("DiskUsage", "org.opennms.netmgt.provision.detector.snmp.DiskUsageDetector")
            .put("DominoIIOP", "org.opennms.netmgt.provision.detector.simple.DominoIIOPDetector")
            .put("FTP", "org.opennms.netmgt.provision.detector.simple.FtpDetector")
            .put("GP", "org.opennms.netmgt.provision.detector.generic.GpDetector")
            .put("HOST-RESOURCES", "org.opennms.netmgt.provision.detector.snmp.HostResourceSWRunDetector")
            .put("HTTP", "org.opennms.netmgt.provision.detector.simple.HttpDetector")
            .put("HTTPS", "org.opennms.netmgt.provision.detector.simple.HttpsDetector")
            .put("ICMP", "org.opennms.netmgt.provision.detector.icmp.IcmpDetector")
            .put("IMAP", "org.opennms.netmgt.provision.detector.simple.ImapDetector")
            .put("IMAPS", "org.opennms.netmgt.provision.detector.simple.ImapsDetector")
            .put("JDBC", "org.opennms.netmgt.provision.detector.jdbc.JdbcDetector")
            .put("JSR160", "org.opennms.netmgt.provision.detector.jmx.Jsr160Detector")
            .put("JdbcQueryDetector", "org.opennms.netmgt.provision.detector.jdbc.JdbcQueryDetector")
            .put("JdbcStoredProcedureDetector",
                    "org.opennms.netmgt.provision.detector.jdbc.JdbcStoredProcedureDetector")
            .put("LDAP", "org.opennms.netmgt.provision.detector.simple.LdapDetector")
            .put("LDAPS", "org.opennms.netmgt.provision.detector.simple.LdapsDetector")
            .put("LOOP", "org.opennms.netmgt.provision.detector.loop.LoopDetector")
            .put("MSExchange", "org.opennms.netmgt.provision.detector.msexchange.MSExchangeDetector")
            .put("Memcached", "org.opennms.netmgt.provision.detector.simple.MemcachedDetector")
            .put("NOTES", "org.opennms.netmgt.provision.detector.simple.NotesHttpDetector")
            .put("NRPE", "org.opennms.netmgt.provision.detector.simple.NrpeDetector")
            .put("NTP", "org.opennms.netmgt.provision.detector.datagram.NtpDetector")
            .put("OMSAStorage", "org.opennms.netmgt.provision.detector.snmp.OmsaStorageDetector")
            .put("PERC", "org.opennms.netmgt.provision.detector.snmp.PercDetector")
            .put("POP3", "org.opennms.netmgt.provision.detector.simple.Pop3Detector")
            .put("SMB", "org.opennms.netmgt.provision.detector.smb.SmbDetector")
            .put("SMTP", "org.opennms.netmgt.provision.detector.simple.SmtpDetector")
            .put("SNMP", "org.opennms.netmgt.provision.detector.snmp.SnmpDetector")
            .put("SSH", "org.opennms.netmgt.provision.detector.ssh.SshDetector")
            .put("TCP", "org.opennms.netmgt.provision.detector.simple.TcpDetector")
            .put("TrivialTime", "org.opennms.netmgt.provision.detector.simple.TrivialTimeDetector")
            .put("WEB", "org.opennms.netmgt.provision.detector.web.WebDetector")
            .put("WMI", "org.opennms.netmgt.provision.detector.wmi.WmiDetector")
            .put("WS-Man", "org.opennms.netmgt.provision.detector.wsman.WsManDetector")
            .put("Win32Service", "org.opennms.netmgt.provision.detector.snmp.Win32ServiceDetector").build();

    @ClassRule
    public static final TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder().all();
            OpenNMSSeleniumTestCase.configureTestEnvironment(builder);
            m_testEnvironment = builder.build();
            return m_testEnvironment;
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Before
    public void checkForDocker() {
        Assume.assumeTrue(OpenNMSSeleniumTestCase.isDockerEnabled());
    }

    @Test
    public void canLoadDetectorsOnMinion() throws Exception {
        final InetSocketAddress sshAddr = m_testEnvironment.getServiceAddress(ContainerAlias.MINION, 8201);
        await().atMost(3, MINUTES).pollInterval(15, SECONDS).pollDelay(0, SECONDS)
            .until(() -> listAndVerifyDetectors("Minion", sshAddr), hasSize(0));
    }

    @Test
    public void canLoadDetectorsOnOpenNMS() throws Exception {
        final InetSocketAddress sshAddr = m_testEnvironment.getServiceAddress(ContainerAlias.OPENNMS, 8101);
        await().atMost(3, MINUTES).pollInterval(15, SECONDS).pollDelay(0, SECONDS)
            .until(() -> listAndVerifyDetectors("OpenNMS", sshAddr), hasSize(0));
    }

    public List<String> listAndVerifyDetectors(String host, InetSocketAddress sshAddr) throws Exception {
        List<String> unmatchedDetectors = new ArrayList<>();
        try (final SshClient sshClient = new SshClient(sshAddr, "admin", "admin")) {
            // List the detectors
            PrintStream pipe = sshClient.openShell();
            pipe.println("provision:list-detectors");
            pipe.println("logout");
            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());

            // Parse the output
            String shellOutput = CommandTestUtils.stripAnsiCodes(sshClient.getStdout());

            shellOutput = StringUtils.substringAfter(shellOutput, "provision:list-detectors");
            LOG.info("Detectors output: {}", shellOutput);
            Map<String, String> detectorMap = new HashMap<String, String>();
            for (String detector : shellOutput.split("\\r?\\n")) {
                if (StringUtils.isNotBlank(detector)) {
                    String detectorSet[] = detector.split(":");
                    if (detectorSet.length >= 2) {
                        detectorMap.put(detectorSet[0], detectorSet[1]);
                    }
                }
            }
            LOG.info("Found detectors: {}",  detectorMap);

            // Verify
            for (String detectorName : expectedDetectors.keySet()) {
                if (!detectorMap.containsKey(detectorName)) {
                    unmatchedDetectors.add(detectorName);
                }
            }
        }
        return unmatchedDetectors;
    }

}
