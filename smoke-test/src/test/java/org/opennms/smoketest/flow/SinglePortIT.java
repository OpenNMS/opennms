/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.flow;

import static com.jayway.awaitility.Awaitility.with;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.equalTo;
import static org.opennms.smoketest.flow.FlowStackIT.TEMPLATE_NAME;
import static org.opennms.smoketest.flow.FlowStackIT.sendNetflowPacket;
import static org.opennms.smoketest.flow.FlowStackIT.verify;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.utils.RestClient;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.indices.template.GetTemplate;

/**
 * Verifies that sending flow packets to a single port is dispatching the flows in the according queues.
 * See issue HZN-1270 for more details.
 */
// TODO MVR consolidate with FlowStackIT
public class SinglePortIT {

    private static final Logger LOG = LoggerFactory.getLogger(SinglePortIT.class);

    @Rule
    public TestEnvironment testEnvironment = getTestEnvironment();

    @Rule
    public Timeout timeout = new Timeout(20, TimeUnit.MINUTES);

    private final TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder().opennms().es5();
            // Enable flow adapter
            builder.withOpenNMSEnvironment().addFile(getClass().getResource("/flows/telemetryd-configuration-single-port.xml"), "etc/telemetryd-configuration.xml");
            builder.withOpenNMSEnvironment().addFile(getClass().getResource("/flows/org.opennms.features.flows.persistence.elastic.cfg"), "etc/org.opennms.features.flows.persistence.elastic.cfg");
            OpenNMSSeleniumTestCase.configureTestEnvironment(builder);
            return builder.build();
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Before
    public void checkForDocker() {
        Assume.assumeTrue(OpenNMSSeleniumTestCase.isDockerEnabled());
    }

    // Verifies that when OpenNMS and ElasticSearch is running and configured, that sending a flow packet
    // will actually be persisted in elastic
    @Test
    public void verifyFlowStack() throws Exception {
        final InetSocketAddress opennnmsSinglePortAddress = new InetSocketAddress("localhost", 50000);
        final String elasticRestUrl = "http://localhost:9200";

        // Proxy the REST service
        final RestClient restClient = new RestClient("localhost", 8980);

        final JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder(elasticRestUrl).multiThreaded(true).build());
        try (final JestClient client = factory.getObject()) {
            // Send packets
            sendNetflowPacket(opennnmsSinglePortAddress, "/flows/netflow5.dat");
            sendNetflowPacket(opennnmsSinglePortAddress, "/flows/netflow9.dat");
            sendNetflowPacket(opennnmsSinglePortAddress, "/flows/ipfix.dat");
            sendNetflowPacket(opennnmsSinglePortAddress, "/flows/sflow.dat");

            // Ensure that the template has been created
            verify(() -> {
                final JestResult result = client.execute(new GetTemplate.Builder(TEMPLATE_NAME).build());
                return result.isSucceeded() && result.getJsonObject().get(TEMPLATE_NAME) != null;
            });

            // Verify directly at elastic that the flows have been created
            verify(() -> {
                final SearchResult response = client.execute(new Search.Builder("").addIndex("netflow-*").build());
                LOG.info("Response: {} {} ", response.isSucceeded() ? "Success" : "Failure", response.getTotal());
                return response.isSucceeded() && response.getTotal() == 16;
            });

            // Verify the flow count via the REST API
            with().pollInterval(15, SECONDS).await().atMost(1, MINUTES)
                    .until(() -> restClient.getFlowCount(0L, System.currentTimeMillis()), equalTo(16L));
        }
    }

}
