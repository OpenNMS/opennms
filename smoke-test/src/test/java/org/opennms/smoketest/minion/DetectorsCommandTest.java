/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2016 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C)
 * 1999-2016 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 * <p>
 * For more information contact: OpenNMS(R) Licensing
 * <license@opennms.org> http://www.opennms.org/ http://www.opennms.com/
 *******************************************************************************/

package org.opennms.smoketest.minion;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.junit.Assert.assertTrue;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.utils.SshClient;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;

public class DetectorsCommandTest {

    private static TestEnvironment m_testEnvironment;

    private static final Logger LOG = LoggerFactory.getLogger(DetectorsCommandTest.class);

    private static final String DETECTORS = "BGP_Session: org.opennms.netmgt.provision.detector.snmp.BgpSessionDetector\n"
            + "BSF: org.opennms.netmgt.provision.detector.bsf.BSFDetector\n"
            + "CITRIX: org.opennms.netmgt.provision.detector.simple.CitrixDetector\n"
            + "Cisco_IP_SLA: org.opennms.netmgt.provision.detector.snmp.CiscoIpSlaDetector\n"
            + "DHCP: org.opennms.protocols.dhcp.detector.DhcpDetector\n"
            + "DNS: org.opennms.netmgt.provision.detector.datagram.DnsDetector\n"
            + "Dell_OpenManageChassis: org.opennms.netmgt.provision.detector.snmp.OpenManageChassisDetector\n"
            + "DiskUsage: org.opennms.netmgt.provision.detector.snmp.DiskUsageDetector\n"
            + "DominoIIOP: org.opennms.netmgt.provision.detector.simple.DominoIIOPDetector\n"
            + "FTP: org.opennms.netmgt.provision.detector.simple.FtpDetector\n"
            + "GP: org.opennms.netmgt.provision.detector.generic.GpDetector\n"
            + "HOST-RESOURCES: org.opennms.netmgt.provision.detector.snmp.HostResourceSWRunDetector\n"
            + "HTTP: org.opennms.netmgt.provision.detector.simple.HttpDetector\n"
            + "HTTPS: org.opennms.netmgt.provision.detector.simple.HttpsDetector\n"
            + "ICMP: org.opennms.netmgt.provision.detector.icmp.IcmpDetector\n"
            + "IMAP: org.opennms.netmgt.provision.detector.simple.ImapDetector\n"
            + "JBoss: org.opennms.netmgt.provision.detector.jmx.JBossDetector\n"
            + "JDBC: org.opennms.netmgt.provision.detector.jdbc.JdbcDetector\n"
            + "JSR160: org.opennms.netmgt.provision.detector.jmx.Jsr160Detector\n"
            + "JdbcQueryDetector: org.opennms.netmgt.provision.detector.jdbc.JdbcQueryDetector\n"
            + "JdbcStoredProcedureDetector: org.opennms.netmgt.provision.detector.jdbc.JdbcStoredProcedureDetector\n"
            + "LDAP: org.opennms.netmgt.provision.detector.simple.LdapDetector\n"
            + "LDAPS: org.opennms.netmgt.provision.detector.simple.LdapsDetector\n"
            + "LOOP: org.opennms.netmgt.provision.detector.loop.LoopDetector\n"
            + "MSExchange: org.opennms.netmgt.provision.detector.msexchange.MSExchangeDetector\n"
            + "MX4J: org.opennms.netmgt.provision.detector.jmx.MX4JDetector\n"
            + "Memcached: org.opennms.netmgt.provision.detector.simple.MemcachedDetector\n"
            + "NOTES: org.opennms.netmgt.provision.detector.simple.NotesHttpDetector\n"
            + "NRPE: org.opennms.netmgt.provision.detector.simple.NrpeDetector\n"
            + "NSClient: org.opennms.protocols.nsclient.detector.NsclientDetector\n"
            + "NTP: org.opennms.netmgt.provision.detector.datagram.NtpDetector\n"
            + "OMSAStorage: org.opennms.netmgt.provision.detector.snmp.OmsaStorageDetector\n"
            + "PERC: org.opennms.netmgt.provision.detector.snmp.PercDetector\n"
            + "POP3: org.opennms.netmgt.provision.detector.simple.Pop3Detector\n"
            + "RadiusAuth: org.opennms.protocols.radius.detector.RadiusAuthDetector\n"
            + "SMB: org.opennms.netmgt.provision.detector.smb.SmbDetector\n"
            + "SMTP: org.opennms.netmgt.provision.detector.simple.SmtpDetector\n"
            + "SNMP: org.opennms.netmgt.provision.detector.snmp.SnmpDetector\n"
            + "SSH: org.opennms.netmgt.provision.detector.ssh.SshDetector\n"
            + "TCP: org.opennms.netmgt.provision.detector.simple.TcpDetector\n"
            + "TrivialTime: org.opennms.netmgt.provision.detector.simple.TrivialTimeDetector\n"
            + "WEB: org.opennms.netmgt.provision.detector.web.WebDetector\n"
            + "WMI: org.opennms.netmgt.provision.detector.wmi.WmiDetector\n"
            + "WS-Man: org.opennms.netmgt.provision.detector.wsman.WsManDetector\n"
            + "Win32Service: org.opennms.netmgt.provision.detector.snmp.Win32ServiceDetector\n"
            + "XMP: org.opennms.netmgt.protocols.xmp.detector.XmpDetector";

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
    public void LoadDetectorsOnMinionTest() throws Exception {

        final InetSocketAddress sshAddr = m_testEnvironment.getServiceAddress(ContainerAlias.MINION, 8201);
        try (final SshClient sshClient = new SshClient(sshAddr, "admin", "admin");) {
            PrintStream pipe = sshClient.openShell();
            pipe.println("provision:list-detectors");
            Thread.sleep(2000);
            String detectorsStdout = sshClient.getStdout();
            String detectorsOut = StringUtils.substringAfter(detectorsStdout, "provision:list-detectors");
            LOG.info("Detectors Output  {}", detectorsOut);
            String detectors[] = detectorsOut.split("\\r?\\n");
            List<String> detectorList = Arrays.asList(detectors);
            String actualdetectors[] = DETECTORS.split("\\r?\\n");
            List<String> actualDetectorList = Arrays.asList(actualdetectors);
            List<String> actualDetectorsList = new ArrayList<String>(actualDetectorList);
            actualDetectorsList.removeAll(detectorList);
            if(!actualDetectorsList.isEmpty()) {
                LOG.error("The detectors which are not loaded by Minion are : \n");
                for(String detector:actualDetectorsList) {
                    LOG.error(detector);
                }
            }
            assertTrue(actualDetectorsList.isEmpty());
            pipe.println("logout");
            try {
                await().atMost(2, MINUTES).until(sshClient.isShellClosedCallable());
            } finally {
                LOG.info("Karaf output:\n {}", sshClient.getStdout());
            }
        }

    }
}
