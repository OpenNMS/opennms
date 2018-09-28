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
import static com.jayway.awaitility.Awaitility.with;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.smoketest.utils.RestClient;
import org.opennms.test.system.api.NewTestEnvironment;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.test.system.api.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

/**
 * This test is used to validate that alarms are indexed into Elasticsearch
 * when both the 'alarm-change-notifier' and 'opennms-es-rest' features
 * are loaded.
 *
 * @author jwhite
 */
public class AlarmElasticsearch5IT {
    private static final Logger LOG = LoggerFactory.getLogger(AlarmElasticsearch5IT.class);

    private static TestEnvironment testEnvironment;

    @ClassRule
    public static final TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder()
                    .opennms()
                    .es5();
            OpenNMSSeleniumTestCase.configureTestEnvironment(builder);
            testEnvironment = builder.build();
            return testEnvironment;
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Test
    public void canForwardAlarmToEs() throws Exception {
        InetSocketAddress esRestAddr = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.ELASTICSEARCH_5, 9200);
        InetSocketAddress opennmsSshAddr = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.OPENNMS, 8101);
        final InetSocketAddress opennmsHttp = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.OPENNMS, 8980);
        installElasticsearchFeaturesOnOpenNMS(opennmsSshAddr);

        // There should be no alarms in ES currently
        assertThat(getNumberOfAlarmsInEsWithUei(esRestAddr, EventConstants.IMPORT_FAILED_UEI), equalTo(0L));

        // Now send some event that will in turn trigger an alarm
        final EventBuilder builder = new EventBuilder(EventConstants.IMPORT_FAILED_UEI, "test");
        builder.setParam("importResource", "foo");
        final Event ev = builder.getEvent();
        final RestClient restClient = new RestClient(opennmsHttp);
        restClient.sendEvent(ev);

        // Now wait until the alarm is available in ES
        with().pollInterval(5, SECONDS).await().atMost(2, MINUTES)
                .until(() -> getNumberOfAlarmsInEsWithUei(esRestAddr, EventConstants.IMPORT_FAILED_UEI),
                        equalTo(1L));
    }

    private static void installElasticsearchFeaturesOnOpenNMS(InetSocketAddress opennmsSshAddr) throws Exception {
        try (final SshClient sshClient = new SshClient(opennmsSshAddr, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();

            // Configure and install the Elasticsearch REST event forwarder
            pipe.println("config:edit org.opennms.plugin.elasticsearch.rest.forwarder");
            pipe.println("config:property-set elasticUrl http://elasticsearch:9200");
            pipe.println("config:property-set retries 10");
            pipe.println("config:update");
            pipe.println("feature:install opennms-es-rest");
            pipe.println("feature:install alarm-change-notifier");

            pipe.println("feature:list -i");
            // Set the log level to INFO
            pipe.println("log:set INFO");
            pipe.println("logout");
            try {
                await().atMost(2, MINUTES).until(sshClient.isShellClosedCallable());
            } finally {
                LOG.info("Karaf output:\n{}", sshClient.getStdout());
            }
        }
    }

    private static Long getNumberOfAlarmsInEsWithUei(InetSocketAddress esHttpAddr, String uei) throws IOException {
        JestClient client = null;
        try {
            JestClientFactory factory = new JestClientFactory();
            factory.setHttpClientConfig(new HttpClientConfig.Builder(String.format("http://%s:%d", esHttpAddr.getHostString(), esHttpAddr.getPort()))
                    .multiThreaded(true)
                    .build());
            client = factory.getObject();

            SearchResult response = client.execute(
                    new Search.Builder(new SearchSourceBuilder()
                            .query(QueryBuilders.matchQuery("eventuei", EventConstants.IMPORT_FAILED_UEI))
                            .toString()
                    )
                            .addIndex("opennms-alarms*")
                            .build()
            );

            LOG.debug("SEARCH RESPONSE: {}", response.toString());
            return response.getTotal();
        } finally {
            if (client != null) {
                client.shutdownClient();
            }
        }
    }

}
