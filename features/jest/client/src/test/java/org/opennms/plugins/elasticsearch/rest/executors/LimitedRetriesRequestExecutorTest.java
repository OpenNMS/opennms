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

package org.opennms.plugins.elasticsearch.rest.executors;

import static org.mockito.Mockito.spy;

import java.io.IOException;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.plugins.elasticsearch.rest.OnmsJestClient;

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
        final RequestExecutor requestExecutor = spy(originalRequestExecutor);

        // Create client manually as we want to spy on it
        final JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder("http://192.168.2.0:9200").build());
        JestClient clientDelegate = spy(factory.getObject());
        final JestClient client = spy(new OnmsJestClient(clientDelegate, requestExecutor));

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