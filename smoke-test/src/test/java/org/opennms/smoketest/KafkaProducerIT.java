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

package org.opennms.smoketest;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.containsString;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.smoketest.minion.CommandTestUtils;
import org.opennms.smoketest.utils.RestClient;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.test.system.api.utils.SshClient;

public class KafkaProducerIT extends BaseKafkaPersisterIT {

    @ClassRule
    public static final TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder().all().kafka();
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
        if (m_testEnvironment == null) {
            return;
        }
        final InetSocketAddress opennmsHttp = m_testEnvironment.getServiceAddress(ContainerAlias.OPENNMS, 8980);
        restClient = new RestClient(opennmsHttp);
        opennmsKarafSshAddr = m_testEnvironment.getServiceAddress(ContainerAlias.OPENNMS, 8101);
    }

    @Test
    public void testKafkaAlarmStoreData() throws Exception {
        // Enable and install the Kafka producer feature
        String kafkaHost = m_testEnvironment.getContainerInfo(ContainerAlias.KAFKA).networkSettings().ipAddress();
        try (final SshClient sshClient = new SshClient(opennmsKarafSshAddr, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();
            pipe.println("config:edit org.opennms.features.kafka.producer.client");
            pipe.println("config:property-set bootstrap.servers " + kafkaHost + ":9092");
            pipe.println("config:update");
            pipe.println("config:edit org.opennms.features.kafka.producer");
            // not needed for this test but since we load persister from activator, 
            // configuration needs to be on the first install.
            pipe.println("config:property-set forward.metrics true");
            pipe.println("config:property-set metricTopic  metrics");
            pipe.println("config:update");
            pipe.println("feature:install opennms-kafka-producer");
            pipe.println("logout");
            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());
        }

        await().atMost(2, MINUTES).pollInterval(15, SECONDS)
                .until(this::triggerAlarmAndListReductionKeysInKtable, containsString("uei.opennms.org/alarms/trigger:::kafka-producer-test"));
    }
    

    private String triggerAlarmAndListReductionKeysInKtable() throws Exception {
        // On every call, send another event to trigger the alarm
        Event event = new Event();
        event.setUei("uei.opennms.org/alarms/trigger");
        event.setSeverity("7");
        List<Parm> parms = new ArrayList<>();
        Parm parm = new Parm("service", "kafka-producer-test");
        parms.add(parm);
        event.setParmCollection(parms);
        restClient.sendEvent(event, true);

        // Grab the output from the
        String shellOutput;
        try (final SshClient sshClient = new SshClient(opennmsKarafSshAddr, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();
            pipe.println("kafka-producer:list-alarms");
            pipe.println("logout");
            await().atMost(30, SECONDS).until(sshClient.isShellClosedCallable());
            shellOutput = CommandTestUtils.stripAnsiCodes(sshClient.getStdout());
            shellOutput = StringUtils.substringAfter(shellOutput, "kafka-producer:list-alarms");
        }
        return shellOutput;
    }
    
    /** Most of the test code is common with KafkaPersisterIT, runKafkaPersisterIT has all the test code **/
    @Test
    public void testKafkaPersisterForMetrics() throws Exception {
        runKafkaPeristerTest();
    }


}
