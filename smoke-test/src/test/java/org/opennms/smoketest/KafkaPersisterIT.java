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

import java.net.InetSocketAddress;

import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.utils.RestClient;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;

/* Tests kafka persister as standalone persister with osgi as timeseries strategy */
public class KafkaPersisterIT extends BaseKafkaPersisterIT {
    
    @ClassRule
    public static final TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder().all().kafka();
            builder.withOpenNMSEnvironment().addFile(
                    KafkaPersisterIT.class.getResource("/opennms.properties.d/kafka-for-metrics.properties"),
                    "etc/opennms.properties.d/kafka-for-metrics.properties");
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
    public void testKafkaPersisterForMetrics() throws Exception {
        runKafkaPeristerTest();
    }



}
