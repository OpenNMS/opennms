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

package org.opennms.netmgt.flows.classification.internal;

import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRuleProvider;
import org.opennms.netmgt.flows.classification.FilterService;

public class ThreadSafeClassificationEngineIT {

    private static final int NUMBER_OF_THREADS = 10;

    private static int DELAY_IN_MS = 500;

    @Rule
    public Timeout timeout = new Timeout(DELAY_IN_MS * NUMBER_OF_THREADS * 10, TimeUnit.MILLISECONDS);

    // Spawn n threads and ensure that each reload()-invocation blocks other threads.
    // The idea is, that n threads are kicked off to reload the engine.
    // The reloading takes about a defined amount of delay in ms.
    // If thread safety is implemented properly, the execution time should be roughly delay * n in ms.
    @Test
    public void verifyThreadSafety() throws InterruptedException, ExecutionException {
        // A reload will always put one rule in place, which simulates heavy loading
        final ClassificationRuleProvider classificationRuleProvider = () -> {
            try {
                Thread.sleep(DELAY_IN_MS);
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread interrupted.", e);
            }
            return Collections.emptyList();
        };

        // Create a thread safe classification engine
        final ClassificationEngine original = new DefaultClassificationEngine(classificationRuleProvider, FilterService.NOOP);
        final ClassificationEngine classificationEngine = new ThreadSafeClassificationEngine(original);

        // Kick off the threads
        final ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        final long startTime = System.currentTimeMillis();
        final List<Future> futures = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            final Future<?> future = executor.submit(() -> classificationEngine.reload());
            futures.add(future);
        }

        // Wait for each future to be finished. Also have it throw exception on errors, because the ThreadExecutor otherwise just swallows errors
        for (Future f : futures) {
            f.get();
        }

        // Verify that we actually took as long as we expected (otherwise the threads would not have been blocked accordingly)
        long expectedExecutionTime = DELAY_IN_MS * NUMBER_OF_THREADS;
        executor.awaitTermination(expectedExecutionTime * 2, TimeUnit.MILLISECONDS);
        long executionTime = System.currentTimeMillis() - startTime;
        assertThat(executionTime, Matchers.greaterThanOrEqualTo(expectedExecutionTime));
    }

}