/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
import static org.hamcrest.Matchers.containsString;

import java.io.PrintStream;
import java.net.InetSocketAddress;

import org.apache.commons.lang.StringUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.utils.RestClient;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;
import org.opennms.test.system.api.utils.SshClient;


public class RpcOverKafkaIT {
    
    private static final String LOCALHOST = "127.0.0.1";
    private static TestEnvironment m_testEnvironment;
    private static RestClient restClient;
    private InetSocketAddress opennmsKarafSshAddr;
    
    @ClassRule
    public static final TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder().all().kafka();
            builder.withOpenNMSEnvironment().addFile(
                    RpcOverKafkaIT.class.getResource("/opennms.properties.d/kafka-rpc.properties"),
                    "etc/opennms.properties.d/kafka-rpc.properties");
            builder.withMinionEnvironment()
                   .addFile(RpcOverKafkaIT.class.getResource("/featuresBoot.d/kafka-rpc.boot"), "etc/featuresBoot.d/kafka.boot");
            OpenNMSSeleniumTestCase.configureTestEnvironment(builder);
            m_testEnvironment = builder.build();
            return m_testEnvironment;
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Before
    public void checkForDockerAndLoadExecutor() {
        Assume.assumeTrue(OpenNMSSeleniumTestCase.isDockerEnabled());
        if (m_testEnvironment == null) {
            return;
        }
        final InetSocketAddress opennmsHttp = m_testEnvironment.getServiceAddress(ContainerAlias.OPENNMS, 8980);
        restClient = new RestClient(opennmsHttp);
        opennmsKarafSshAddr = m_testEnvironment.getServiceAddress(ContainerAlias.OPENNMS, 8101);
    }
    
    @Test
    public void verifyKafkaRpcWithIcmpServiceDetection() throws Exception {
        // Add node and interface with minion location.
        DetectorsOnMinionIT.addRequisition(restClient, "MINION", LOCALHOST);
        await().atMost(3, MINUTES).pollInterval(15, SECONDS)
        .until(this::detectIcmpAtLocationMinion, containsString("'ICMP' WAS detected on 127.0.0.1"));

    }
    
    private String detectIcmpAtLocationMinion() throws Exception {
        String shellOutput;
        try (final SshClient sshClient = new SshClient(opennmsKarafSshAddr, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();
            pipe.println("detect -l MINION ICMP 127.0.0.1");
            pipe.println("logout");
            await().atMost(90, SECONDS).until(sshClient.isShellClosedCallable());
            shellOutput = CommandTestUtils.stripAnsiCodes(sshClient.getStdout());
            shellOutput = StringUtils.substringAfter(shellOutput, "detect -l MINION ICMP 127.0.0.1");
        }
        return shellOutput;
    }


}
