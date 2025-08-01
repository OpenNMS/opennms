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
package org.opennms.features.jest.client.executors;

import static org.mockito.Mockito.spy;

import java.io.IOException;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.features.jest.client.OnmsJestClient;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;

public class LimitedRetriesRequestExecutorTest {

    @Test
    public void verifyRetryHandling() throws IOException {
        final int retryCount = 5;
        final int attempts = retryCount + 1;
        final int timeout = 1000;

        // Spy on the executor
        final LimitedRetriesRequestExecutor originalRequestExecutor = new LimitedRetriesRequestExecutor(timeout, retryCount);
        final RequestExecutor requestExecutor = Mockito.spy(originalRequestExecutor);

        // Create client manually as we want to spy on it
        final JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder("http://192.168.2.0:9200").build());
        JestClient clientDelegate = spy(factory.getObject());
        final JestClient client = Mockito.spy(new OnmsJestClient(clientDelegate, requestExecutor));

        // Verify
        final long startTime = System.currentTimeMillis();
        try {
            client.execute(new Search.Builder("").build());
            Assert.fail("Request should not have been successful");
        } catch (Exception ex) {
            final long executionTime = System.currentTimeMillis() - startTime;
            
            // Expected an exception, verify execution of methods
            Mockito.verify(requestExecutor, Mockito.times(1)).execute(Mockito.any(), Mockito.any());
            Mockito.verify(client, Mockito.times(1)).execute(Mockito.any());
            Mockito.verify(clientDelegate, Mockito.times(attempts)).execute(Mockito.any());

            // Ensure that we actually waited
            Assert.assertThat(executionTime, CoreMatchers.allOf(
                    Matchers.greaterThan(timeout * retryCount * 1L)));
        }
    }


}