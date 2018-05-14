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

package org.opennms.plugins.elasticsearch.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Search;

public class BasicAuthTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig()
            .dynamicPort());

    @Test
    public void canPerformPreemptiveAuth() throws IOException {
        stubFor(post(urlEqualTo("/_all/_search"))
                // Require basic auth credentials
                .withBasicAuth("mi", "mimimi")
                .willReturn(aResponse()
                        // Use some valid body so that JEST treats the response
                        // as a success, aside from that, we don't care about the contents
                        .withBody("{\n" +
                                "    \"took\": 1,\n" +
                                "    \"timed_out\": false,\n" +
                                "    \"_shards\":{\n" +
                                "        \"total\" : 1,\n" +
                                "        \"successful\" : 1,\n" +
                                "        \"skipped\" : 0,\n" +
                                "        \"failed\" : 0\n" +
                                "    },\n" +
                                "    \"hits\":{\n" +
                                "        \"total\" : 0,\n" +
                                "        \"max_score\": 0,\n" +
                                "        \"hits\" : []\n" +
                                "    }\n" +
                                "}")));

        // Create the REST client factory in a fashion similar to how it's created in the Blueprint
        // and specify some basic auth credentials
        RestClientFactory restClientFactory = new RestClientFactory(wireMockRule.url(""), "mi", "mimimi");
        restClientFactory.setTimeout(3000);
        restClientFactory.setSocketTimeout(3000);
        restClientFactory.setRetries(0);

        try (JestClient client = restClientFactory.createClient()) {
            // Execute a request with our client, the contents of the search don't really matter
            // here, we just need some valid JSON
            JestResult result = client.execute(new Search.Builder("{\n" +
                    "    \"query\" : {\n" +
                    "        \"term\" : { \"user\" : \"kimchy\" }\n" +
                    "    }\n" +
                    "}").build());

            // Verify
            assertTrue("The search request using Jest should succeed.", result.isSucceeded());
        }
    }
}
