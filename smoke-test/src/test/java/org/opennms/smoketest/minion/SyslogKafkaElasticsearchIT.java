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
import static com.jayway.awaitility.Awaitility.with;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

import java.util.Date;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.hibernate.MinionDaoHibernate;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.features.jest.client.SearchResultUtils;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.IpcStrategy;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.SyslogUtils;
import org.opennms.smoketest.utils.TestContainerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.RateLimiter;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

/**
 * This test will send syslog messages over the following message bus:
 * 
 * Minion -> Kafka -> OpenNMS Eventd -> Elasticsearch REST -> Elasticsearch
 * 
 * @author Seth
 */
public class SyslogKafkaElasticsearchIT {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogKafkaElasticsearchIT.class);

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withMinion()
            .withIpcStrategy(IpcStrategy.KAFKA)
            .withElasticsearch()
            .build());

    @Test
    public void testMinionSyslogsOverKafkaToEsRest() throws Exception {
        Date startOfTest = new Date();
        int numMessages = 10000;
        int packetsPerSecond = 500;

        final String sender = TestContainerUtils.getInternalIpAddress(stack.postgres());

        // Wait for the minion to show up
        await().atMost(90, SECONDS).pollInterval(5, SECONDS)
            .until(DaoUtils.countMatchingCallable(
                 stack.postgres().getDaoFactory().getDao(MinionDaoHibernate.class),
                 new CriteriaBuilder(OnmsMinion.class)
                     .gt("lastUpdated", startOfTest)
                     .eq("location", stack.minion().getLocation())
                     .toCriteria()
                 ),
                 is(1)
             );

        LOG.info("Warming up syslog routes by sending 100 packets");

        // Warm up the routes
        SyslogUtils.sendMessage(stack.minion().getSyslogAddress(), sender, 100);

        for (int i = 1; i <= 15; i++) {
            Thread.sleep(1000);
            LOG.info("Slept for " + i + " seconds");
        }

        // Make sure that this evenly divides into the numMessages
        final int chunk = 500;
        // Make sure that this is an even multiple of chunk
        final int logEvery = 1000;

        int count = 0;
        long start = System.currentTimeMillis();

        // Send ${numMessages} syslog messages
        RateLimiter limiter = RateLimiter.create(packetsPerSecond);
        for (int i = 0; i < (numMessages / chunk); i++) {
            limiter.acquire(chunk);
            SyslogUtils.sendMessage(stack.minion().getSyslogAddress(), sender, chunk);
            count += chunk;
            if (count % logEvery == 0) {
                long mid = System.currentTimeMillis();
                LOG.info(String.format("Sent %d packets in %d milliseconds", logEvery, mid - start));
                start = System.currentTimeMillis();
            }
        }

        // Wait for at least 1k messages to show up in Elastisearch
        final JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder(String.format("http://%s", stack.elastic().getHttpHostAddress()))
                .multiThreaded(true)
                .build());

        final String queryString = "{\n" +
                "    \"query\": {\n" +
                "        \"bool\" : {\n" +
                "            \"filter\" : {\n" +
                "                \"term\" : { \"eventuei\" : \"" + SyslogUtils.SYSLOG_MESSAGE_UEI + "\" }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        try (JestClient client = factory.getObject()) {
            with().pollInterval(15, SECONDS).await().atMost(5, MINUTES).until(() -> {
                    LOG.debug("SEARCH QUERY: {}", queryString);
                    SearchResult response = client.execute(
                            new Search.Builder(queryString)
                                    .addIndex("opennms*")
                                    .build()
                    );
                    LOG.debug("SEARCH RESPONSE: {}", response.toString());
                    return SearchResultUtils.getTotal(response);
            }, greaterThanOrEqualTo(1000L));
        }
    }

}
