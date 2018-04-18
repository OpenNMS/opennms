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

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.Mockito;
import org.opennms.plugins.elasticsearch.rest.OnmsJestClient;
import org.opennms.plugins.elasticsearch.rest.index.IndexStrategy;

import com.google.common.base.Throwables;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;

public class DefaultRequestExecutorIT {

    @Rule
    public Timeout timeout = new Timeout(30, TimeUnit.SECONDS);

    @Test
    public void verifyKeepsTryingIndefinetly() throws ExecutionException {
        final ExecutorService executorService = Executors.newFixedThreadPool(1);
        final AtomicLong startTime = new AtomicLong(0);

        // Create client manually, as we want to spy on it
        final JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder("http://192.168.2.0:9200").build()); // 192.168.2.0 should not be reachable
        JestClient clientDelegate = Mockito.spy(factory.getObject());
        final JestClient client = Mockito.spy(new OnmsJestClient(clientDelegate, new DefaultRequestExecutor(1000)));

        // action to run, we need this to interrupt it after n-seconds
        final Runnable runMe = () -> {
            try {
                startTime.set(System.currentTimeMillis());
                try {
                    final Map<String, String> object = new HashMap<>();
                    object.put("name", "Ulf");
                    object.put("location", "Pittsboro");
                    final Index action = new Index.Builder(object).index(IndexStrategy.MONTHLY.getIndex("dummy", Instant.now())).type("persons").build();
                    client.execute(action);
                    Assert.fail("The execution of persistNetFlow5Packets() should not have finished. Failing.");
                } finally {
                    client.close();
                }
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        };
        final Future<?> future = executorService.submit(runMe);
        try {
            // Interrupt Thread after 10 seconds
            future.get(10, TimeUnit.SECONDS);
            Assert.fail("The test should have failed with an exception, as we interrupted it manually. Failing.");
        } catch (InterruptedException | TimeoutException e) {
            // Expected behaviour, now do some verification

            // Verify re-trying
            // Cooldown is 1 second, therefore the invocation should be ~10 times, but may be less, therefore we do not check
            // Verify that we actually cooled down between retries
            long executionTime = System.currentTimeMillis() - startTime.get();
            long expectedExecutionTime = 10 * 1000;
            Assert.assertThat(executionTime, CoreMatchers.allOf(
                    Matchers.greaterThanOrEqualTo(expectedExecutionTime)));
        }
    }
}
