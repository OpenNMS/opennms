/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.jest.client;

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
