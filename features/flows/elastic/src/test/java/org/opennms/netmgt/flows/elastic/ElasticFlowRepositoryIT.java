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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.plugins.elasticsearch.rest.index.IndexStrategy;

import com.codahale.metrics.MetricRegistry;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.Lists;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

public class ElasticFlowRepositoryIT {

    private static final String ERROR_RESPONSE = "{\"took\":97,\"errors\":true,\"items\":[{\"index\":{\"_index\":\"flow-2017-11\",\"_type\":\"flow\",\"_id\":\"AV_8xp7OaWbS_xJQivfD\",\"status\":400,\"error\":{\"type\":\"mapper_parsing_exception\",\"reason\":\"failed to parse [timestamp]\",\"caused_by\":{\"type\":\"number_format_exception\",\"reason\":\"For input string: \\\"XXX\\\"\"}}}}]}";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().dynamicPort());

    @Test(expected=PersistenceException.class)
    public void verifyThrowsPersistenceException() throws IOException, FlowException {
        // Stub request
        stubFor(post("/_bulk")
                    .willReturn(aResponse()
                            .withStatus(400)
                            .withHeader("Content-Type", "application/json")
                            .withBody(ERROR_RESPONSE)));

        final MockDocumentEnricherFactory mockDocumentEnricherFactory = new MockDocumentEnricherFactory();
        final DocumentEnricher documentEnricher = mockDocumentEnricherFactory.getEnricher();
        final ClassificationEngine classificationEngine = mockDocumentEnricherFactory.getClassificationEngine();

        // Verify exception is thrown
        final JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder("http://localhost:" + wireMockRule.port()).build());
        try (JestClient client = factory.getObject()) {
            final ElasticFlowRepository elasticFlowRepository = new ElasticFlowRepository(new MetricRegistry(),
                    client, IndexStrategy.MONTHLY, documentEnricher, classificationEngine, 3, 12000);

            // It does not matter what we persist here, as the response is fixed.
            // We only have to ensure that the list is not empty
            elasticFlowRepository.persist(Lists.newArrayList(FlowDocumentTest.getMockFlow()), FlowDocumentTest.getMockFlowSource());
        }
    }

}
