/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.elastic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.api.IndexStrategy;
import org.opennms.netmgt.flows.api.NetflowDocument;
import org.opennms.netmgt.flows.elastic.utils.ElasticSearchRule;
import org.opennms.netmgt.flows.elastic.utils.ElasticSearchServerConfig;
import org.opennms.netmgt.flows.elastic.utils.ExecutionTime;
import org.opennms.plugins.elasticsearch.rest.RestClientFactory;

import com.google.common.base.Throwables;

import io.searchbox.client.JestClient;

public class ElasticFlowRepositoryRetryIT {

    private static final long START_DELAY = 10000; // in ms
    private static final long EXECUTION_TIME_DIFF = 2500; // in ms
    private static final String HTTP_PORT = "9205";
    private static final String HTTP_TRANSPORT_PORT = "9305";
    private static final long RETRY_COOLDOWN = 500;

    // The timeout is needed because starting the elasticsearch server may fail, but we will not get notified
    // The timeout will ensure that the test will not block for ever, but fail eventually
    @Rule
    public Timeout timeout = new Timeout(START_DELAY * 5, TimeUnit.MILLISECONDS);

    @Rule
    public ExecutionTime executionTime = new ExecutionTime(START_DELAY, TimeUnit.MILLISECONDS, EXECUTION_TIME_DIFF);

    @Rule
    public ElasticSearchRule elasticServerRule = new ElasticSearchRule(
            new ElasticSearchServerConfig()
                .withDefaults()
                .withSetting("http.enabled", true)
                .withSetting("http.port", HTTP_PORT)
                .withSetting("http.type", "netty4")
                .withSetting("transport.type", "netty4")
                .withSetting("transport.tcp.port", HTTP_TRANSPORT_PORT)
                .withStartDelay(START_DELAY)
                .withManualStartup()
    );

    @Test
    public void verifyFindAllSucceedsWhenServerBecomesAvailable() throws Exception {
        // Try getting data
        apply((repository) -> repository.findAll(""));
    }

    @Test
    public void verifyRawQuerySucceedsWhenServerBecomesAvailable() throws Exception {
        // Try getting data
        apply((repository) -> repository.rawQuery(""));
    }

    @Test
    public void verifySaveSucceedsWhenServerBecomesAvailable() throws Exception {
        final List<NetflowDocument> documentList = new ArrayList<>();

        final NetflowDocument document = new NetflowDocument();
        document.setLocation("Default");
        document.setExporterAddress("127.0.0.1");
        document.setTimestamp(new Date().getTime());
        documentList.add(document);

        // try persisting data
        apply((repository) -> repository.save(documentList));
    }

    private void apply(FlowRepositoryConsumer consumer) throws Exception {
        Objects.requireNonNull(consumer);

        final RestClientFactory restClientFactory = new RestClientFactory("http://localhost:" + HTTP_PORT);
        restClientFactory.setRequestExecutorSupplier(() -> new FlowRequestExecutor(RETRY_COOLDOWN));
        try (JestClient client = restClientFactory.createClient()) {
            executionTime.resetStartTime();
            elasticServerRule.startServer();

            final FlowRepository elasticFlowRepository = new InitializingFlowRepository(new ElasticFlowRepository(client, IndexStrategy.MONTHLY), client);

            consumer.accept(elasticFlowRepository);

        } catch (FlowException e) {
            throw Throwables.propagate(e);
        }

    }

    @FunctionalInterface
    interface FlowRepositoryConsumer {
        void accept(FlowRepository flowRepository) throws FlowException;
    }
}