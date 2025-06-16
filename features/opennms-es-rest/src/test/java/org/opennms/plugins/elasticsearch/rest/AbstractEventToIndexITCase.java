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
package org.opennms.plugins.elasticsearch.rest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.opennms.core.test.elastic.ElasticSearchRule;
import org.opennms.features.jest.client.JestClientWithCircuitBreaker;
import org.opennms.features.jest.client.RestClientFactory;
import org.opennms.netmgt.dao.mock.AbstractMockDao;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

public abstract class AbstractEventToIndexITCase {

    protected JestClientWithCircuitBreaker jestClient;
    protected EventToIndex eventToIndex;

    @Rule
    public ElasticSearchRule elasticServerRule = new ElasticSearchRule();

    @Before
    public void setUp() throws Exception {
        this.jestClient = new RestClientFactory(elasticServerRule.getUrl()).createClientWithCircuitBreaker(CircuitBreakerRegistry.of(
                CircuitBreakerConfig.custom().build()).circuitBreaker(AbstractEventToIndexITCase.class.getName()), new AbstractMockDao.NullEventForwarder());
        this.eventToIndex = new EventToIndex(jestClient, 3);
    }

    @After
    public void tearDown() {
        if (jestClient != null) {
            jestClient.shutdownClient();
        }
        if (eventToIndex != null) {
            eventToIndex.close();
        }
    }

    protected EventToIndex getEventToIndex() {
        return eventToIndex;
    }
}
